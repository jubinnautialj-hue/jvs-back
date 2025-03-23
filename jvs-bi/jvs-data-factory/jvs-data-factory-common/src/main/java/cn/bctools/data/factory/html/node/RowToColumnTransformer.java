package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.Polymerization;
import cn.bctools.data.factory.html.node.params.RowToColumnParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.util.FieldUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 行转列
 *
 * @author xiaohui
 */
@Service
public class RowToColumnTransformer implements Frun<RowToColumnParams> {
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, RowToColumnParams rowToColumnParams) {
        //设计数据
        RowToColumnParams.RowToColumnObj tableObj = rowToColumnParams.getRowToColumnObj();
        DataSourceField columnField = tableObj.getColumnField();
        boolean isNumber = columnField.getDataFieldTypeClassify().equals(DataFieldTypeClassifyEnum.数字);
        //通过部分的数据只会生成基础的
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        sql.append("INSERT INTO ")
                .append(bean.getLibraryName())
                .append(".")
                .append(targetTableName)
                .append(" (")
                .append(targetColumn)
                .append(") ");
        sql.append("SELECT ");
        //添加分组与转换逻辑
        List<String> groupList = tableObj.getGroupList().stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        String groupKey = String.join("`,`", groupList);
        groupKey = "`" + groupKey + "`";
        sql.append(groupKey).append(",");
        //转换后生成新字段
        List<DataSourceField> lineRoll = targetFieldList.stream().filter(e -> !groupList.contains(e.getFieldKey())).collect(Collectors.toList());
        //添加转换字段逻辑
        String lineRollSelectSql = lineRoll.stream().map(e -> {
            StringBuffer selectSql = new StringBuffer();
            selectSql.append("map[");
            if (!isNumber) {
                selectSql.append("'").append(e.getFieldName()).append("'");
            } else {
                selectSql.append(e.getFieldName());
            }
            selectSql.append("] as `").append(e.getFieldKey()).append("`");
            return selectSql;
        }).collect(Collectors.joining(","));
        Polymerization polymerization = tableObj.getPolymerization();
        sql.append(lineRollSelectSql)
                .append(" from ( select ")
                .append(groupKey)
                .append(",")
                .append("map_agg(`")
                .append(columnField.getFieldKey())
                .append("`,`")
                .append(polymerization.getFieldKey()).append("`) as map FROM ");
        //判断是否存在汇总
        if (polymerization.getGroupMethodVal().equals(CollectTypeEnum.notCalculate)) {
            sql.append(sourceTable);
        } else {
            //获取汇总sql
            String addCondition = SpringContextUtil.getBean(polymerization.getGroupMethodVal().getDorisClass()).addCondition(polymerization.getFieldKey(), polymerization.getDecimalPlace(), polymerization.getTruncation(), polymerization.getFieldKey());
            sql.append(" ( select ")
                    .append(groupKey)
                    .append(",`")
                    .append(columnField.getFieldKey())
                    .append("`,")
                    .append(addCondition)
                    .append(" from ")
                    .append(sourceTable)
                    .append(" group by ")
                    .append(groupKey)
                    .append(",`")
                    .append(columnField.getFieldKey())
                    .append("`) as a");
        }
        sql.append(" group by ").append(groupKey).append(") t");
        return new ArrayList<>();

    }

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, RowToColumnParams rowToColumnParams) {
        String nodeDocumentName = rowToColumnParams.getTableName();
        RowToColumnParams sourceData = rowToColumnParams.getSourceData();
        //获取原有的结构
        //title 需要重新定义
        String next = linkBody.keySet().iterator().next();
        FData fData1 = linkBody.get(next);
        String sourceTableName = fData1.getDocumentName();
        List<DataSourceField> title = fData1.getTitle();
        List<DataSourceField> newTitle = this.getColumn(sourceData, sourceTableName, title);
        //写入数据与创建表
        StringBuffer sql = new StringBuffer();
        List<Object> objects = this.createSql(title, newTitle, sql, sourceTableName, nodeDocumentName, Boolean.FALSE, Boolean.FALSE, sourceData);
        this.save(sql.toString(), nodeDocumentName, newTitle, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        //获取列数据
        return new FData().setTitle(newTitle).setDocumentName(nodeDocumentName);
    }

    /**
     * 构建表结构数据
     *
     * @param rowToColumnParams 设计数据
     * @param nodeDocumentName  表名称
     * @return 列字段
     */
    private List<DataSourceField> getColumn(RowToColumnParams rowToColumnParams, String nodeDocumentName, List<DataSourceField> title) {
        //查询语句
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        //需要注意如果列字段是一个时间需要使用时间函数进行转换
        DataSourceField columnField = rowToColumnParams.getRowToColumnObj().getColumnField();
        if (DataFieldTypeEnum.isNormalDate(columnField.getFieldType())) {
            sql.append("CAST(`")
                    .append(columnField.getFieldKey())
                    .append("` AS STRING)");
        } else {
            sql.append("`").append(rowToColumnParams.getRowToColumnObj().getColumnField().getFieldKey()).append("`");
        }
        sql.append(" FROM ").append(nodeDocumentName).append(" GROUP BY ").append("`").append(rowToColumnParams.getRowToColumnObj().getColumnField().getFieldKey()).append("`");
        List<Object> list = dorisJdbcTemplate.queryForList(sql.toString())
                .stream().flatMap(e -> e.values().stream()).collect(Collectors.toList());
        //获取字段类型
        Polymerization polymerization = rowToColumnParams.getRowToColumnObj().getPolymerization();
        DataSourceField dataSourceField = title.stream()
                .filter(e -> e.getFieldKey().equals(polymerization.getFieldKey()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到计算字段"));
        //防止原始数据被修改
        DataSourceField newDataSourceField = JSONObject.parseObject(JSONObject.toJSONString(dataSourceField), DataSourceField.class);
        newDataSourceField = SpringContextUtil.getBean(polymerization.getGroupMethodVal().getDorisClass()).fieldGenerate(newDataSourceField, Optional.ofNullable(polymerization.getDecimalPlace()).orElse(2));
        //构建字段
        DataSourceField finalNewDataSourceField = newDataSourceField;
        //添加分组字段
        List<DataSourceField> newTitle = new ArrayList<>(rowToColumnParams.getRowToColumnObj().getGroupList());
        list.forEach(e -> {
            DataSourceField sourceField = JSONObject.parseObject(JSONObject.toJSONString(finalNewDataSourceField), DataSourceField.class);
            String fieldName = Optional.ofNullable(e).orElse("").toString();
            sourceField.setFieldName(fieldName)
                    .setFieldKey(FieldUtil.getFieldName(fieldName, newTitle, Boolean.FALSE));
            newTitle.add(sourceField);
        });
        return newTitle;
    }


}
