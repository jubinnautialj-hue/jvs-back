package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.DataExtractingParams;
import cn.bctools.data.factory.html.run.Frun;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author guojing
 * 结构解析
 */
@Data
@Service
public class DataExtractingNode implements Frun<DataExtractingParams> {
    private final static String FIELD_KEY = "jvs_data_extracting_data";


    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, DataExtractingParams dataExtractingParams) {
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
        String sourceColumn;
        //如果输入源 与 目标源字段一致就直接使用输入源的 减少计算
        if (sourceFieldEqTargetField) {
            sourceColumn = targetColumn;
        } else {
            sourceColumn = sourceFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
            sourceColumn = "`" + sourceColumn + "`";
        }
        DataExtractingParams.DataExtractingDataField dataExtractingNode = dataExtractingParams.getDataExtractingNode();
        sql.append("SELECT ")
                .append(sourceColumn)
                .append(" FROM (")
                .append("SELECT ")
                .append(sourceColumn)
                .append(" FROM ")
                .append(bean.getLibraryName())
                .append(".")
                .append(sourceTable);
        // lateral view explode_json_array_json(score) tmp1 as score2
        sql.append(" lateral view ");
        switch (dataExtractingNode.getFieldType()) {
            case JSON:
                sql.append("explode_json_array_json_outer");
                break;
            case INT:
                sql.append("explode_json_array_int_outer");
                break;
            case DOUBLE:
                sql.append("explode_json_array_double_outer");
                break;
            case STRING:
            case VARCHAR:
            case CHAR:
                sql.append("explode_json_array_string_outer");
                break;
            default:
                throw new BusinessException("转换类型未知");
        }
        sql.append("(`").append(dataExtractingNode.getFieldKey())
                .append("`) `").append(dataExtractingNode.getFieldKeyNew()).append("` as ")
                .append("`")
                .append(dataExtractingNode.getFieldKeyNew())
                .append("`");
        sql.append(") as a ");
        ;
        if (addOrderBy) {
            sql.append(" ORDER BY ").append(Constant.DORIS_ODS_KEY).append(" desc");
        }
        return new ArrayList<>();
    }

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, DataExtractingParams nodeHtml) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        //中间表名称
        String nodeDocumentName = nodeHtml.getTableName();
        fData.setDocumentName(nodeDocumentName);
        //上一个节点的数据库名称
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        List<DataSourceField> title = linkBody.get(next).getTitle();
        DataExtractingParams.DataExtractingDataField dataExtractingNode = nodeHtml.getSourceData().getDataExtractingNode();
        if (!Optional.ofNullable(dataExtractingNode.getShowHistoryData()).orElse(Boolean.FALSE)) {
            title = title.stream().filter(e -> !dataExtractingNode.getFieldKey().equals(e.getFieldKey())).collect(Collectors.toList());
        }
        DataSourceField sourceField = JSONObject.parseObject(JSONObject.toJSONString(dataExtractingNode), DataSourceField.class);
        sourceField.setFieldKey(dataExtractingNode.getFieldKeyNew());
        sourceField.setDorisType(dataExtractingNode.getFieldType().getCreateTable());
        sourceField.setDataFieldTypeClassify(dataExtractingNode.getFieldType().getClassifyEnum());
        title.add(sourceField);
        List<Object> objects = createSql(title, title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, nodeHtml.getSourceData());
        this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        return fData.setTitle(title);
    }

}
