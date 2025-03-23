package cn.bctools.data.factory.html.node;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.ConditionGroupParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.query.QueryExecDto;
import cn.bctools.data.factory.util.FieldUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件分组
 *
 * @author guojing
 */
@Service
public class ConditionGroupNode implements Frun<ConditionGroupParams> {

    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, ConditionGroupParams conditionGroupParams) {
        //中间表名称
        String nodeDocumentName = conditionGroupParams.getTableName();
        FData data = new FData().setDocumentName(nodeDocumentName);
        String next = linkBody.keySet().iterator().next();
        //上一个节点的数据库名称
        String documentName = linkBody.get(next).getDocumentName();
        FData fData = linkBody.getOrDefault(next, new FData()).setDocumentName(nodeDocumentName);
        ConditionGroupParams sourceData = conditionGroupParams.getSourceData();
        List<ConditionGroupParams.ConditionGroupObj> groupObj = sourceData.getConditionGroupNode();
        if (groupObj == null || groupObj.isEmpty()) {
            return fData;
        }
        //过滤需要添加值的
        groupObj = groupObj.stream().filter(e -> ObjectUtil.isNotNull(e.getConditionGroups()) && !e.getConditionGroups().isEmpty() && StrUtil.isNotBlank(e.getDissatisfyFieldValue())).collect(Collectors.toList());
        //为空终止计算
        if (groupObj.isEmpty()) {
            return fData;
        }
        //sql创建需要 在生成新字段之前
        StringBuffer sql = new StringBuffer();
        //重新生成一个title对象 防止源对象被修改
        List<DataSourceField> title = fData.getTitle();
        List<DataSourceField> copyTitle = new ArrayList<>(title);
        //如果key值已经存在 直接覆盖
        List<DataSourceField> newTitle = groupObj.stream()
                .map(ConditionGroupParams.ConditionGroupObj::getFieldName)
                .map(e ->
                        new DataSourceField()
                                .setFieldType(DataFieldTypeEnum.VARCHAR)
                                .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串)
                                .setFormatDefault("255")
                                .setDorisType("varchar(%s)")
                                .setFieldKey(FieldUtil.getFieldName(e, title, Boolean.FALSE))
                                .setFieldName(e)
                                .setLength(300)
                                .setItems(new ArrayList<>())
                                .setDataId(title.get(0).getDataId())
                                .setIsShow(Boolean.TRUE))
                .collect(Collectors.toList());
        copyTitle.addAll(newTitle);
        List<Object> objects = createSql(title, title, sql, documentName, nodeDocumentName, Boolean.FALSE, Boolean.FALSE, sourceData);
        //同步数据
        this.save(sql.toString(), nodeDocumentName, copyTitle, Boolean.TRUE, null, Boolean.TRUE, new ArrayList<>(), objects.toArray());
        //开始执行修改sql
        this.update(sourceData, title, nodeDocumentName);
        return data.setTitle(copyTitle);
    }


    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, ConditionGroupParams conditionGroupParams) {
        //通过部分的数据只会生成基础的
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        //添加uuid字段
        targetColumn = "`" + Constant.DORIS_UNIQUE_FIELD_KEY + "`," + targetColumn;
        sql.append("INSERT INTO ")
                .append(bean.getLibraryName())
                .append(".")
                .append(targetTableName)
                .append(" (")
                .append(targetColumn)
                .append(") ");
        String sourceColumn;
        //如果输入源 与 目标源字段一致就直接使用输入源的 减少计算
        sourceColumn = sourceFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        sourceColumn = "`" + sourceColumn + "`";
        //添加uuid
        sourceColumn = "uuid()," + sourceColumn;
        sql.append("SELECT ")
                .append(sourceColumn)
                .append(" FROM ")
                .append(bean.getLibraryName())
                .append(".")
                .append(sourceTable);
        if (addOrderBy) {
            sql.append(" ORDER BY ").append(Constant.DORIS_ODS_KEY).append(" desc");
        }
        return new ArrayList<>();
    }

    /**
     * 生成update sql 语句
     *
     * @param conditionGroupParams 设计数据
     * @param title                字段源字段 主要永远生成新的字段时判断是否存在重复的key
     * @param nodeDocumentName     表名称
     */
    private void update(ConditionGroupParams conditionGroupParams, List<DataSourceField> title, String nodeDocumentName) {
        conditionGroupParams.getConditionGroupNode().forEach(e -> {
            String fieldName = FieldUtil.getFieldName(e.getFieldName(), title, Boolean.FALSE);
            DataSourceField datasourceField = e.getDatasourceField();
            List<String> whereList = new ArrayList<>();
            StringBuffer whereSql = new StringBuffer();
            whereSql.append("UPDATE ")
                    .append(nodeDocumentName)
                    .append(" set `")
                    .append(fieldName)
                    .append("`= ");
            //最终入参
            List<Object> endObj = new ArrayList<>();
            e.getConditionGroups().forEach(v -> {
                StringBuffer execSql = new StringBuffer();
                List<Object> objects = new ArrayList<>();
                v.getConditions().forEach(query -> {
                    QueryExecDto queryExecDto = new QueryExecDto()
                            .setFormat(datasourceField.getFormat())
                            .setMethodValue(query.getMethodValue())
                            .setFieldType(datasourceField.getFieldType())
                            .setFieldKey(datasourceField.getFieldKey());
                    List<Object> exec = SpringContextUtil.getBean(query.getMethod().getCls()).exec(queryExecDto, execSql);
                    execSql.append(" and ");
                    objects.addAll(exec);
                });
                //删除结尾and
                execSql.delete(execSql.length() - 4, execSql.length());
                //添加添加sql
                String execSqlStr = execSql.toString();
                whereList.add(execSqlStr);
                //添加前置
                execSqlStr = "'" + v.getFieldValue() + "'" + " where " + execSqlStr;
                //执行sql
                String sql = whereSql + execSqlStr;
                dorisJdbcTemplate.update(sql, objects.toArray());
                endObj.addAll(objects);
            });
            //判断是否需要设置不满足条件的数据
            if (StrUtil.isNotBlank(e.getDissatisfyFieldValue())) {
                String sql = whereList.stream().collect(Collectors.joining(") and !("));
                sql = "!(" + sql + ")";
                whereSql.append("'").append(e.getDissatisfyFieldValue()).append("'")
                        .append(" where ")
                        .append(sql);
                dorisJdbcTemplate.update(whereSql.toString(), endObj.toArray());
            }
        });
    }


}
