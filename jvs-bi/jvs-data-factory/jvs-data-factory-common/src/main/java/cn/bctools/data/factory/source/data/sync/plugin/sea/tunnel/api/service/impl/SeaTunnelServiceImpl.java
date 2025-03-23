package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateSeaTunnelJsonParameterPo;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelJobInfoReturnDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.enums.JobInfoStatusEnums;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelApiService;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelService;
import cn.bctools.data.factory.source.dto.RealTimeSettingDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.util.DorisUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
@Slf4j
@AllArgsConstructor
public class SeaTunnelServiceImpl implements SeaTunnelService {
    private final Map<String, DataSourceExecuteInterface> dataSourceExecuteInterfaceMap;
    private final DataSourceStructureService dataSourceStructureService;
    private final DataSourceService dataSourceService;
    private final RedisUtils redisUtils;
    private final SeaTunnelApiService seaTunnelApiService;
    private final DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public void syncData(String structureId, List<DataSourceField> dataSourceFields, Long size, String dorisTableName, RealTimeSettingDto incrementalSetting) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        DataSourceExecuteInterface dataSourceExecuteInterface = dataSourceExecuteInterfaceMap.get(dataSource.getSourceType().value);
        try {
            //防止存在此表
            dorisJdbcTemplate.dropForce(dorisTableName);
            //创建
            String tableSql = DorisUtil.getTableSql(dorisTableName, dataSourceFields, Boolean.FALSE, new ArrayList<>());
            dorisJdbcTemplate.execute(tableSql);
        } catch (Exception exception) {
            log.error("生成ods表失败", exception);
            throw new BusinessException("生成doris表结构错误" + exception.getMessage());
        }
        Function<CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> seaTunnelJsonFunction = dataSourceExecuteInterface.createSeaTunnelJsonFunction();
        CreateSeaTunnelJsonParameterPo createSeaTunnelJsonParameterPo = new CreateSeaTunnelJsonParameterPo().setSize(size).setDataSourceStructure(dataSourceStructure).setCustomJson(dataSource.getCustomJson());
        SeaTunnelSubmitJobDto submitJobDto = seaTunnelJsonFunction.apply(createSeaTunnelJsonParameterPo);
        List<String> dorisColumn = dataSourceFields.stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        //重置字段 如果存在函数需要修改要字段名称 统一修改 originalColumnName 使用dorisColumn 为主要来源是防止字段顺序变更
        //添加映射关系
        JSONObject transformFieldMapper = new JSONObject();
        List<DataSourceStructure.Structure> structureList = dorisColumn.stream().map(e -> {
            DataSourceStructure.Structure structure = dataSourceStructure.getStructure().stream().filter(v -> v.getColumnName().equals(e)).findFirst().orElseThrow(() -> new BusinessException("最新数据源中未找到此字段:" + e));
            String function = structure.getFunction();
            if (StrUtil.isNotBlank(function)) {
                Map<String, String> map = new HashMap<>(1);
                map.put("columnName", structure.getOriginalColumnName());
                String columnName = StrUtil.format(function, map);
                structure.setOriginalColumnName(columnName);
            }
            transformFieldMapper.put(structure.getOriginalColumnName(), e);
            return structure;
        }).collect(Collectors.toList());
        dataSourceStructure.setStructure(structureList);
        //添加数据加工
        JSONObject transform = new JSONObject();
        transform.put("plugin_name", "FieldMapper");
        transform.put("field_mapper", transformFieldMapper);
        submitJobDto.setTransform(Collections.singletonList(transform));
        //添加去向配置
        JSONObject sinkObj = JSONObject.parseObject(redisUtils.get(Constant.SEA_TUNNEL_JSON_KEY).toString());
        sinkObj.put("table", dorisTableName);
        submitJobDto.setSink(Collections.singletonList(sinkObj));
        //添加env配置
        JSONObject env = new JSONObject();
        env.put("job.mode", "batch");
        env.put("parallelism", Optional.ofNullable(incrementalSetting.getParallelism()).orElse(5));
        submitJobDto.setEnv(env);
        //执行任务
        String jobId = seaTunnelApiService.submitJob(submitJobDto);
        SeaTunnelJobInfoReturnDto jobInfo = seaTunnelApiService.getJobInfo(jobId);
        //循环查询任务是否执行完成 注意此处的状态 在任务 提交成功的第一次 可能会返回null
        while (jobInfo.getJobStatus() == null || jobInfo.getJobStatus().equals(JobInfoStatusEnums.RUNNING)) {
            try {
                Thread.sleep(1000);
            } catch (Exception exception) {
                log.info("睡眠失败", exception);
            }
            jobInfo = seaTunnelApiService.getJobInfo(jobId);
            //返回值存在有错误信息但是没有状态
            if (StrUtil.isNotBlank(jobInfo.getErrorMsg())) {
                jobInfo.setJobStatus(JobInfoStatusEnums.CANCEL);
            }
        }
        log.info("=========seaTunnel同步数据完成==========");
        log.info("本次执行的结果为:{}", JSONObject.toJSONString(jobInfo));
        if (jobInfo.getJobStatus().equals(JobInfoStatusEnums.CANCEL)) {
            throw new BusinessException("数据同步失败,任务被取消。seaTunnel执行的错误日志为:" + jobInfo.getErrorMsg());
        }
    }

    @Override
    public String syncDataCDC(String structureId, String dorisTableName) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        RealTimeSettingDto realTimeSettingDto = dataSourceStructure.getRealTimeSetting();
        //数据映射
        List<DataSourceField> dataSourceFields = dataSourceStructure.getStructure().stream()
                .filter(e->!e.getPrimaryIs())
                .map(e -> new DataSourceField()
                        .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                        .setFieldType(e.getDataFieldTypeEnum())
                        .setLength(e.getLength())
                        .setPrecision(e.getPrecision())
                        .setFormat(e.getFormat())
                        .setDorisType(e.getDorisType())
                        .setIsShow(Boolean.TRUE)
                        .setFieldKey(e.getColumnName())
                        .setFieldName(e.getOriginalColumnName())
                )
                .collect(Collectors.toList());
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        DataSourceExecuteInterface dataSourceExecuteInterface = dataSourceExecuteInterfaceMap.get(dataSource.getSourceType().value);
        try {
            //防止存在此表
            dorisJdbcTemplate.dropForce(dorisTableName);
            //cdc 模式下需要使用主键模型 才能实现 delete update
            //获取主键key
            List<DataSourceField> uniqueKey = dataSourceStructure.getStructure().stream().filter(DataSourceStructure.Structure::getPrimaryIs).map(e ->
                    new DataSourceField()
                            .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                            .setFieldType(e.getDataFieldTypeEnum())
                            .setLength(e.getLength())
                            .setPrecision(e.getPrecision())
                            .setFormat(e.getFormat())
                            .setDorisType(e.getDorisType())
                            .setIsShow(Boolean.TRUE)
                            .setFieldKey(e.getColumnName())
                            .setFieldName(e.getOriginalColumnName())).collect(Collectors.toList());
            dataSourceFields.addAll(0,uniqueKey);
            //创建
            String tableSql = DorisUtil.getTableSql(dorisTableName, dataSourceFields, Boolean.TRUE, uniqueKey);
            //删除
            dorisJdbcTemplate.dropForce(dorisTableName);
            dorisJdbcTemplate.execute(tableSql);
        } catch (Exception exception) {
            log.error("生成ods表失败", exception);
            throw new BusinessException("生成doris表结构错误" + exception.getMessage());
        }
        Function<CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> seaTunnelJsonFunction = dataSourceExecuteInterface.createSeaTunnelJsonCDCFunction();
        CreateSeaTunnelJsonParameterPo createSeaTunnelJsonParameterPo = new CreateSeaTunnelJsonParameterPo().setDataSourceStructure(dataSourceStructure).setCustomJson(dataSource.getCustomJson());
        SeaTunnelSubmitJobDto submitJobDto = seaTunnelJsonFunction.apply(createSeaTunnelJsonParameterPo);
        //重置字段 如果存在函数需要修改要字段名称 统一修改 originalColumnName 使用dorisColumn 为主要来源是防止字段顺序变更
        //添加映射关系
        JSONObject transformFieldMapper = new JSONObject();
        dataSourceStructure.getStructure().forEach(e->transformFieldMapper.put(e.getOriginalColumnName(),e.getColumnName()));
        //添加数据加工
        JSONObject transform = new JSONObject();
        transform.put("plugin_name", "FieldMapper");
        transform.put("field_mapper", transformFieldMapper);
        submitJobDto.setTransform(Collections.singletonList(transform));
        //添加去向配置
        JSONObject sinkObj = JSONObject.parseObject(redisUtils.get(Constant.SEA_TUNNEL_JSON_KEY).toString());
        sinkObj.put("table", dorisTableName);
        submitJobDto.setSink(Collections.singletonList(sinkObj));
        //添加env配置
        JSONObject env = new JSONObject();
        env.put("job.mode", "STREAMING");
        env.put("parallelism", Optional.ofNullable(realTimeSettingDto.getParallelism()).orElse(5));
        submitJobDto.setEnv(env);
        //执行任务
        return seaTunnelApiService.submitJob(submitJobDto);
    }
}
