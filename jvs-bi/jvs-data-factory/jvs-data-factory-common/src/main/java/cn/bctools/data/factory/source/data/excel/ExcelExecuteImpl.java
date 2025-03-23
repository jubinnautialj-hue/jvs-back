package cn.bctools.data.factory.source.data.excel;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.source.config.DataSourceCommonConfig;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import cn.bctools.data.factory.source.entity.ExcelOperationLog;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.ExcelCommitLogService;
import cn.bctools.data.factory.source.service.ExcelOperationLogService;
import cn.bctools.data.factory.util.ExcelThreadTool;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel 数据实现类
 *
 * @author xiaohui
 */
@Component(value = "excelDataSource")
@Slf4j
public class ExcelExecuteImpl implements DataSourceExecuteInterface<ExcelReadDataPo> {
    @Autowired
    DataSourceCommonConfig commonConfig;
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;
    @Autowired
    ExcelCommitLogService excelUpdateLogService;
    @Autowired
    ExcelOperationLogService excelOperationLogService;
    @Autowired
    ExcelCommitLogService excelCommitLogService;

    @Override
    public void up(DataSource dataSource, UserDto userDto) {
        DataSourceExecuteInterface.super.up(dataSource, userDto);
        //处理其他逻辑
        JSONObject exportData = dataSource.getExportData();
        //直接删除表 重新创建
        String executeName = dataSource.getChildren().get(0).getExecuteName();
        dorisJdbcTemplate.dropForce(executeName);
        String createTableSql = exportData.getString("createTableSql");
        dorisJdbcTemplate.execute(createTableSql);
        //写入数据
        List<Map<String, Object>> list = exportData.getJSONArray("data").stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e))).collect(Collectors.toList());
        List<DataSourceField> sourceFields = dataSource.getChildren().get(0).getStructure()
                .stream()
                .map(e -> new DataSourceField().setLength(e.getLength())
                        .setFieldType(e.getDataFieldTypeEnum())
                        .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                        .setFieldKey(e.getColumnName())
                        .setDorisType(e.getDorisType())
                        .setPrecision(e.getPrecision()))
                .collect(Collectors.toList());
        dorisJdbcTemplate.insert(list, sourceFields, executeName);
        //写入日志文件
        List<ExcelCommitLog> excelCommitLog = exportData.getJSONArray("excelCommitLog")
                .stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e), ExcelCommitLog.class))
                .collect(Collectors.toList());
        List<String> ids = excelCommitLog.stream().map(ExcelCommitLog::getId).collect(Collectors.toList());
        excelCommitLogService.removeByIds(ids);
        excelCommitLogService.saveBatch(excelCommitLog);
    }

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        return null;
    }

    @Override
    public void removeDataSource(String id) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        List<DataSourceStructure> list = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, id));
        if (!list.isEmpty()) {
            //删除doris的数据
            list.forEach(e -> dorisJdbcTemplate.dropForce(e.getExecuteName()));
        }
        DataSourceExecuteInterface.super.removeDataSource(id);
        //删除历史上传记录
        excelUpdateLogService.remove(Wrappers.lambdaQuery(ExcelCommitLog.class).eq(ExcelCommitLog::getDataSourceId, id));
        //删除操作记录
        excelOperationLogService.remove(Wrappers.lambdaQuery(ExcelOperationLog.class).eq(ExcelOperationLog::getDataSourceId, id));
    }

    @Override
    public void removeDataSourceStructure(String id) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(id);
        dorisJdbcTemplate.dropForce(dataSourceStructure.getExecuteName());
        DataSourceExecuteInterface.super.removeDataSourceStructure(id);
    }

    @Override
    @SneakyThrows
    public void read(ExcelReadDataPo readData) {
        //生成批次号
        String lotNo = ExcelThreadTool.generate();
        log.info("======批次号 {}", lotNo);
        ReadDataListener readDataListener = new ReadDataListener();
        ExcelReader excelReader = EasyExcel.read(new URL(readData.getUrl()).openStream(), readDataListener).build();

        List<ReadSheet> sheetList = excelReader.excelExecutor().sheetList();
        if (CollectionUtil.isEmpty(sheetList)) {
            throw new BusinessException("sheet页为空");
        }
        ReadSheet sheet0 = CollectionUtil.getFirst(sheetList);

        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);

        //功能变动 默认只读第一个sheet页 不支持读多个sheet页
        LambdaQueryWrapper<DataSourceStructure> structureWrapper = new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, readData.getDataSourceId());
        List<DataSourceStructure> list = dataSourceStructureService.list(structureWrapper);

        //如果是覆盖
        if (!readData.getAddIs()) {
            //获取数据 删除doris中的数据
            if (!list.isEmpty()) {
                list.forEach(e -> dorisJdbcTemplate.dropForce(e.getExecuteName()));
                dataSourceStructureService.remove(structureWrapper);
                list = new ArrayList<>();
            }
        }

        //如果当前数据源的表数据不为空 则判断表结构
        if (!list.isEmpty()) {
            //校验sheet 是否跟上次一致
            TitleReadDataListener titleReadDataListener = new TitleReadDataListener();
            titleReadDataListener.setStructures(list);
            ExcelReader build = EasyExcel.read(new URL(readData.getUrl()).openStream(), titleReadDataListener).build();
            build.read(sheet0);
            build.finish();
        }

        //覆盖且已生成过表的情况 继承上次的表名 其他情况生成
        if (list.isEmpty()) {
            //生成表名称
            DataSourceStructure structure = new DataSourceStructure()
                    .setName(sheet0.getSheetName())
                    .setCheckIs(Boolean.TRUE)
                    .setDataSourceId(readData.getDataSourceId())
                    .setTableNameDesc(sheet0.getSheetName())
                    .setExecuteName("excel_" + IdGenerator.getIdStr());
            list.add(structure);
        }
        readDataListener.setStructures(list);
        readDataListener.setAddIs(readData.getAddIs());
        readDataListener.setBatchCount(commonConfig.getExcelReadNumber());
        try {
            //只读取第一个sheet
            excelReader.read(sheet0);
            excelReader.finish();
            excelUpdateLogService.save(readData, lotNo);
        } catch (Exception exception) {
            log.info("读取内容失败", exception);
            if (exception instanceof BusinessException || exception instanceof ExcelAnalysisException) {
                String error = findError(exception);
                throw new BusinessException(error);
            } else {
                throw new BusinessException("读取内容失败");
            }
        }
    }

    private String findError(Throwable exception) {
        if (exception.getCause() != null) {
            Throwable cause = exception.getCause();
            return findError(cause);
        }
        return exception.getMessage();
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        long total = dorisJdbcTemplate.getCount(dataSourceStructure.getExecuteName());
        List<Map<String, Object>> list;
        Page mapPage = new Page()
                .setCurrent(current)
                .setSize(size);
        // 查询数据
        if (size > BigDecimal.ROUND_UP) {
            list = dorisJdbcTemplate.page(size, current, dataSourceStructure.getExecuteName());
        } else {
            list = dorisJdbcTemplate.getData(dataSourceStructure.getExecuteName());
        }
        mapPage.setRecords(list);
        mapPage.setTotal(total);
        return mapPage;
    }

    /**
     * 按批次号分页查询
     *
     * @param page        分页
     * @param executeName
     * @param lotNo
     * @return
     */
    public Page<Map<String, Object>> pageByLotNo(Page<Map<String, Object>> page, String executeName, String lotNo) {

        String sqlFormat = "SELECT * FROM {} WHERE `{}`= {} ORDER bY `{}` LIMIT {},{}";
        long skip = page.getSize() * (page.getCurrent() - 1);
        String sql = StrUtil.format(sqlFormat, executeName, ExcelSysFieldEnum.批次号.getFieldName(), lotNo, ExcelSysFieldEnum.唯一标识.getFieldName(), skip, page.getSize());
        List<Map<String, Object>> records = dorisJdbcTemplate.queryForList(sql);

        String sqlCountFormat = "SELECT count(1) FROM {} WHERE `{}`= {}";
        String countSql = StrUtil.format(sqlCountFormat, executeName, ExcelSysFieldEnum.批次号.getFieldName(), lotNo);
        Long total = dorisJdbcTemplate.queryForObject(countSql, Long.class);

        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        return dorisJdbcTemplate.getCount(dataSourceStructure.getExecuteName());
    }

    @Override
    public void check(String json) {

    }

    /**
     * 验证title是否存在为空的数据
     */
    public void checkTitleIsNotBlank(String filePath) throws Exception {
        EasyExcel.read(new URL(filePath).openStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map data, AnalysisContext context) {
            }

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                List<Integer> list = headMap.keySet().stream().filter(e -> StrUtil.isBlank(headMap.get(e))).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    String string = list.stream().map(e -> String.format("第%s列表头为空", e + 1)).collect(Collectors.joining(","));
                    throw new BusinessException(string);
                }
                Map<String, Long> map = headMap.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
                String string = map.keySet().stream().filter(e -> map.get(e) > 1).collect(Collectors.joining(","));
                if (!string.isEmpty()) {
                    throw new BusinessException(string + "。存在重复的title。");
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {

            }
        }).sheet(0).doRead();
    }
}
