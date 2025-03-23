package cn.bctools.data.factory.source.data.excel;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.util.DorisUtil;
import cn.bctools.data.factory.util.ExcelThreadTool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
public class ReadDataListener extends AnalysisEventListener<Map<Integer, String>> {
    private final static String MAX_UNIQUE_KEY = "SELECT MAX(`{}`) AS max FROM {}.`{}`;";

    /**
     * 每批次的数据量
     */
    private int batchCount = 10000;
    private List<Map<Integer, String>> cachedDataList;
    private List<String> headList = new ArrayList<>();
    private Boolean addIs;

    List<DataSourceStructure> structures = new ArrayList<>();

    public void setStructures(List<DataSourceStructure> structures) {
        this.structures = structures;
    }

    public void setAddIs(Boolean addIs) {
        this.addIs = addIs;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    private int currentSheetNo = -1;
    private AtomicLong index = new AtomicLong(1);

    private final static List<DataSourceStructure.Structure> SYS_FIELD = Arrays.stream(ExcelSysFieldEnum.values()).map(e -> new DataSourceStructure.Structure()
            .setColumnName(e.getFieldName())
            .setOriginalColumnName(e.getFieldName())
            .setColumnCount(e.name())
            .setLength(e.getLength())
            .setDataFieldTypeClassify(e.getDataFieldType().getClassifyEnum())
            .setDataFieldTypeEnum(e.getDataFieldType())
            .setDataType(e.getFieldType())
            .setDorisType(e.getFieldType())
    ).collect(Collectors.toList());

    private String tableSql = null;

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
        //当前行
        int currentRow = analysisContext.readRowHolder().getRowIndex();
        //大致总行数
        int totalRows = analysisContext.readSheetHolder().getApproximateTotalRowNumber();
        log.info("解析到一条数据:{}", JSON.toJSONString(data));
        cachedDataList.add(data);
        if (cachedDataList.size() >= batchCount) {
            saveData(currentRow, totalRows, analysisContext.readSheetHolder().getSheetNo());
            cachedDataList = new ArrayList<>(batchCount);
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        //默认增加批次号
        headList = new ArrayList<>();
        //去除前后的空格
        List<String> list = headMap.values().stream().map(StrUtil::trim).collect(Collectors.toList());
        headList.addAll(list);
        //初始化
        cachedDataList = new ArrayList<>(batchCount);
        Integer sheetNo = context.readSheetHolder().getSheetNo();
        //表结构入库
        DataSourceStructure dataSourceStructure = this.structures.get(sheetNo);
        boolean empty = dataSourceStructure.getStructure() == null || dataSourceStructure.getStructure().isEmpty();
        //如果表结构为空 或为覆盖 则重置数据结构
        if (empty || !this.addIs) {
            List<DataSourceStructure.Structure> structures = this.headList.stream().map(e -> new DataSourceStructure.Structure()
                    .setDataFieldTypeEnum(DataFieldTypeEnum.STRING)
                    .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串)
                    .setColumnName(SecureUtil.md5(e)).setDorisType("STRING").setDataType("STRING").setOriginalColumnName(e).setColumnCount(e)).collect(Collectors.toList());

            structures.addAll(0, SYS_FIELD);

            dataSourceStructure.setStructure(structures).setFieldCount(structures.size());
            //保存当前sheet 信息
            DataSourceStructureService bean = SpringContextUtil.getBean(DataSourceStructureService.class);
            bean.saveOrUpdate(dataSourceStructure);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        int totalRows = analysisContext.readSheetHolder().getApproximateTotalRowNumber();
        Integer sheetNo = analysisContext.readSheetHolder().getSheetNo();
        saveData(totalRows, totalRows, sheetNo);
    }

    /**
     * 入库
     * 这里只做插入 如果是覆盖需要调用方执行清空数据操作
     *
     * @param currentRow 当前条数
     * @param totalRows  总条数
     * @param sheetNo    当前sheet编号 用于生成表名称
     */
    private void saveData(int currentRow, int totalRows, int sheetNo) {

        //获取表名称
        DataSourceStructure dataSourceStructure = this.structures.get(sheetNo);
        String executeName = dataSourceStructure.getExecuteName();

        //创建表
        List<DataSourceField> list = dataSourceStructure.getStructure().stream().map(e ->
                new DataSourceField()
                        .setFormat(e.getFormat())
                        .setDorisType(e.getDorisType())
                        .setFieldType(e.getDataFieldTypeEnum())
                        .setLength(e.getLength())
                        .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                        .setFieldName(e.getOriginalColumnName())
                        .setDataId(e.getDataType())
                        .setFieldKey(e.getColumnName())).collect(Collectors.toList());
        //建表
        String tableSql = getTableSql(executeName, list);
        DorisJdbcTemplate jdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        jdbcTemplate.execute(tableSql);

        //设置起始数 如果当前页变动 则重新查询起始数
        if (this.currentSheetNo != sheetNo) {
            this.currentSheetNo = sheetNo;
            Long maxIndex = getMaxIndex(executeName);
            maxIndex = Optional.ofNullable(maxIndex).orElse(0L);
            this.index = new AtomicLong(maxIndex + 1);
        }

        double progress = (double) currentRow / totalRows * 100;
        log.info("解析进度：{}%", progress);
        //重新生成数据 改变key
        List<Map<String, Object>> mapList = cachedDataList.stream().map(e -> {
            HashMap<String, Object> map = new HashMap<>(e.size());

            map.put(ExcelSysFieldEnum.唯一标识.getFieldName(), index.getAndIncrement());
            //设置批次号 保留关键字jvs_bi_lot_no
            map.put(ExcelSysFieldEnum.批次号.getFieldName(), ExcelThreadTool.get());
            //key 生成规则为 当前head的md5值
            e.keySet().forEach(v -> map.put(SecureUtil.md5(headList.get(v)), e.get(v)));

            return map;
        }).collect(Collectors.toList());

        jdbcTemplate.insert(mapList, list, executeName);
        log.info("入库的数据为:{}", JSONObject.toJSONString(mapList));
    }

    /**
     * 获取建表sql
     *
     * @param tableName 表名称
     * @param list      字段
     * @return
     */
    private String getTableSql(String tableName, List<DataSourceField> list) {
        if (this.tableSql != null) {
            return this.tableSql;
        }
        List<DataSourceField> uniqueKey = SYS_FIELD.stream().map(e ->
                new DataSourceField()
                        .setFormat(e.getFormat())
                        .setDorisType(e.getDorisType())
                        .setFieldType(e.getDataFieldTypeEnum())
                        .setLength(e.getLength())
                        .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                        .setFieldName(e.getOriginalColumnName())
                        .setDataId(e.getDataType())
                        .setFieldKey(e.getColumnName())).collect(Collectors.toList());
        this.tableSql = DorisUtil.getTableSql(tableName, list, Boolean.TRUE, uniqueKey);
        return this.tableSql;
    }

    private Long getMaxIndex(String executeName) {
        //获取库名
        String libraryName = SpringContextUtil.getBean(DorisConfig.class).getLibraryName();
        String sql = StrUtil.format(MAX_UNIQUE_KEY, ExcelSysFieldEnum.唯一标识.getFieldName(), libraryName, executeName);
        DorisJdbcTemplate jdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}