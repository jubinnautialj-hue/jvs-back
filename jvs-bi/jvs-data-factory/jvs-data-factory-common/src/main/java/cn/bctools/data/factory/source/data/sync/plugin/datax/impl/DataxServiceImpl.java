package cn.bctools.data.factory.source.data.sync.plugin.datax.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.CommonConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.source.data.sync.plugin.datax.DataxService;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.util.DorisUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Service
@Slf4j
@AllArgsConstructor
public class DataxServiceImpl implements DataxService {
    private final DataSourceService dataSourceService;
    private final Map<String, DataSourceExecuteInterface> dataSourceExecuteInterfaceMap;
    private final RedisUtils redisUtils;
    private final DataSourceStructureService dataSourceStructureService;
    private final DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public void syncData(String structureId, List<DataSourceField> dataSourceFields, Long size, String dorisTableName, InputParams.IncrementalSetting incrementalSetting) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        DataSourceExecuteInterface dataSourceExecuteInterface = dataSourceExecuteInterfaceMap.get(dataSource.getSourceType().value);
        Object incrementalModeValue;
        try {
            //获取创建sql
            incrementalModeValue = this.createTable(dataSourceFields, dorisTableName, incrementalSetting);
        } catch (Exception exception) {
            log.error("生成ods表失败", exception);
            throw new BusinessException("生成doris表结构错误" + exception.getMessage());
        }
        //如果是增量模式 但是没有获取到最大值 将更改为 非增量模式  存在 上次同步数据时 没有同步成功 或者第一次同步数据
        if (incrementalSetting.getIncrementalMode() && ObjectUtil.isNull(incrementalModeValue)) {
            incrementalSetting.setIncrementalMode(Boolean.FALSE);
        }
        List<String> dorisColumn = dataSourceFields.stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        //重置字段 如果存在函数需要修改要字段名称 统一修改 originalColumnName 使用dorisColumn 为主要来源是防止字段顺序变更
        List<DataSourceStructure.Structure> structureList = dorisColumn.stream().map(e -> {
            DataSourceStructure.Structure structure = dataSourceStructure.getStructure().stream().filter(v -> v.getColumnName().equals(e)).findFirst().orElseThrow(() -> new BusinessException("最新数据源中未找到此字段:" + e));
            String function = structure.getFunction();
            if (StrUtil.isNotBlank(function)) {
                Map<String, String> map = new HashMap<>(1);
                map.put("columnName", structure.getOriginalColumnName());
                String columnName = StrUtil.format(function, map);
                structure.setOriginalColumnName(columnName);
            }
            return structure;
        }).collect(Collectors.toList());
        dataSourceStructure.setStructure(structureList);
        Function<CreateDataXJsonParameterPo, JSONObject> dataxFileJsonFunction = dataSourceExecuteInterface.createDataxFileJsonFunction();
        CreateDataXJsonParameterPo createDataXJsonParameterPo = new CreateDataXJsonParameterPo()
                .setCustomJson(dataSource.getCustomJson())
                .setSize(size)
                .setIncrementalMode(incrementalSetting.getIncrementalMode())
                .setIncrementalModeKey(incrementalSetting.getIncrementalModeKey())
                .setIncrementalModeKeyType(incrementalSetting.getIncrementalModeKeyType())
                .setIncrementalModeKeyValue(incrementalModeValue)
                .setDataSourceStructure(dataSourceStructure);
        JSONObject reader = dataxFileJsonFunction.apply(createDataXJsonParameterPo);
        String dataxJson = (String) redisUtils.get(Constant.DATAX_JSON_KEY);
        //替换同步表
        dataxJson = StrUtil.replace(dataxJson, "{tableName}", dorisTableName);
        //写入读取
        dataxJson = StrUtil.replace(dataxJson, "{readerValue}", reader.toJSONString());
        //替换字段
        dataxJson = StrUtil.replace(dataxJson, "{dorisColumn}", JSONObject.toJSONString(dorisColumn));
        //并发数量
        String channel = "1";
        //如果存在数量限制就不需要开启多线程执行同步
        if (size == 0L) {
            channel = SpringContextUtil.getBean(CommonConfig.class).getDataxChannel();
        }
        dataxJson = StrUtil.replace(dataxJson, "{channel}", channel);
        //写入磁盘
        //生成datax json文件
        String dataxPath = SpringContextUtil.getBean(CommonConfig.class).getDataxPath();
        //判断是否为当前操作系统的文件路径符结束如果不是就增加一个
        if (!StrUtil.endWith(dataxPath, File.separator)) {
            dataxPath += File.separator;
        }
        String dataxJsonPath = dataxPath + "json" + File.separator;
        dataxJsonPath += IdGenerator.getIdStr() + "_" + dataSourceStructure.getId() + "-" + dataSourceStructure.getExecuteName().trim() + ".json";
        FileUtil.writeUtf8String(dataxJson, dataxJsonPath);
        //执行datax 逻辑
        try {
            String dataxJvm = SpringContextUtil.getBean(CommonConfig.class).getDataxJvm();
            String pythonScriptPath = dataxPath + "bin" + File.separator + "datax.py";
//            String executeCmd = "python " + pythonScriptPath + " --jvm=\"-Xms2G -Xmx32G\" " + dataxJsonPath;
            String executeCmd = "python3 " + pythonScriptPath;
            if (StrUtil.isNotBlank(dataxJvm)) {
                executeCmd += " --jvm=\"" + dataxJvm + "\"";
            }
            executeCmd += " " + dataxJsonPath;
            log.info("执行的命令为:{}", executeCmd);
            Process process = Runtime.getRuntime().exec(executeCmd);
            ReadStream readStream = new ReadStream(process.getInputStream());
            readStream.run();
            ReadStream readStreamError = new ReadStream(process.getErrorStream());
            readStreamError.run();
            // 明确等待进程结束并获取退出码
            int exitCode = process.waitFor();
            log.info("执行python进程后返回的code码为:{}", exitCode);
            if (exitCode != 0) {
                throw new BusinessException("调用datax同步数据，发生错误");
            }
        } catch (Throwable e) {
            log.error("\n\n经DataX智能分析,该任务最可能的错误原因是:\n", e);
            log.info("执行的datax配置为:{}", dataxJson);
            throw new BusinessException("同步数据错误：" + e);
        } finally {
            boolean del = FileUtil.del(dataxJsonPath);
            log.info("datax文件是否删除成功{}", del);
        }
    }


    /**
     * @param dataSourceFields   字段数据
     * @param dorisTableName     表名称
     * @param incrementalSetting 增量配置信息
     */
    private Object createTable(List<DataSourceField> dataSourceFields, String dorisTableName, InputParams.IncrementalSetting incrementalSetting) {
        if (incrementalSetting.getIncrementalMode()) {
            boolean b = dorisJdbcTemplate.ifNotExistsTable(dorisTableName);
            if (b) {
                StringBuilder sql = new StringBuilder("SELECT ");
                //判断是否为日期类型如果是  日期类型需要 进行转换
                if (DataFieldTypeEnum.isNormalDate(incrementalSetting.getIncrementalModeKeyType())) {
                    sql.append("CAST(`").append(incrementalSetting.getFieldKey()).append("` AS STRING ")
                            .append(") as `").append(incrementalSetting.getFieldKey()).append("`");
                } else {
                    sql.append("`").append(incrementalSetting.getFieldKey()).append("` ");
                }
                sql.append("FROM ").append(dorisTableName).append(" ORDER BY `").append(incrementalSetting.getFieldKey()).append("` DESC LIMIT 0,1");
                //获取最新的数据
                List<Map<String, Object>> list = dorisJdbcTemplate.queryForList(sql.toString());
                if (!list.isEmpty()) {
                    return list.get(0).get(incrementalSetting.getFieldKey());
                }
                return null;
            }
        } else {
            //防止存在此表
            dorisJdbcTemplate.dropForce(dorisTableName);
        }
        //创建
        String tableSql = DorisUtil.getTableSql(dorisTableName, dataSourceFields, Boolean.FALSE, new ArrayList<>());
        dorisJdbcTemplate.execute(tableSql);
        return null;
    }

    @Slf4j
    public static class ReadStream extends Thread {
        private InputStream inputStream;

        public ReadStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("{}", line);
                }
            } catch (Exception e) {
                log.error("读取日志错误", e);
            }
        }

    }
}
