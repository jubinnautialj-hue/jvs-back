package cn.bctools.data.factory.html.node;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.enums.FieldSetupReplaceEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.FieldSetupParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.service.FieldSettingFunctionService;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 字段设置
 *
 * @author guojing
 */
@Service
public class FieldSetupNode implements Frun<FieldSetupParams> {

    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;
    @Autowired
    FieldSettingFunctionService fieldSettingFunctionService;

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, FieldSetupParams fieldSetupParams) {
        //通过部分的数据只会生成基础的
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        //就如果存在类型变更需要使用函数
        StringBuilder sourceColumn = new StringBuilder();
        for (FieldSetupParams.FieldSetup fieldSetup : fieldSetupParams.getFieldObj()) {
            if (fieldSetup.getIsShow()) {
                if (Optional.ofNullable(fieldSetup.getPropertyIsChange()).orElse(Boolean.FALSE)) {
                    String function = fieldAttributeChange(fieldSetup);
                    sourceColumn.append(function);
                } else {
                    sourceColumn.append("`").append(fieldSetup.getFieldKey()).append("`");
                }
                sourceColumn.append(",");
            }
        }
        //添加uuid字段
        targetColumn = "`" + Constant.DORIS_UNIQUE_FIELD_KEY + "`," + targetColumn;
        sql.append("INSERT INTO ").append(bean.getLibraryName()).append(".").append(targetTableName).append(" (").append(targetColumn).append(") ");
        //添加uuid
        sql.append("SELECT ").append("uuid(),").append(sourceColumn.substring(0, sourceColumn.length() - 1)).append(" FROM ").append(bean.getLibraryName()).append(".").append(sourceTable);
        return new ArrayList<>();
    }


    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, FieldSetupParams fieldSetupParams) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        //中间表名称
        String nodeDocumentName = fieldSetupParams.getTableName();
        fData.setDocumentName(nodeDocumentName);
        //上一个节点的数据库名称
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        List<FieldSetupParams.FieldSetup> fieldObj = fieldSetupParams.getSourceData().getFieldObj();
        List<DataSourceField> title = fieldObj.stream()
                .map(e -> JSONObject.parseObject(JSONObject.toJSONString(e), DataSourceField.class))
                .filter(DataSourceField::getIsShow).collect(Collectors.toList());
        List<Object> objects = createSql(new ArrayList<>(), title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, fieldSetupParams.getSourceData());
        this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.TRUE, new ArrayList<>(), objects.toArray());
        //执行值替换
        replaceValue(fieldObj, nodeDocumentName);
        return fData.setTitle(title);
    }


    /***
     * 值替换
     * @param tableName 表名称
     * @param list 字段属性
     * */
    private void replaceValue(List<FieldSetupParams.FieldSetup> list, String tableName) {
        //先替换值 因为可能存在排序值替换只支持 字符串 数字 时间
        List<FieldSetupParams.FieldSetup> replaceField = list.stream().filter(e -> e.getReplaceList() != null && !e.getReplaceList().isEmpty()).collect(Collectors.toList());
        if (replaceField.isEmpty()) {
            return;
        }
        replaceField.forEach(e ->
                e.getReplaceList().forEach(v -> {
                    List<Object> objects = new ArrayList<>();
                    StringBuffer whereSql = new StringBuffer();
                    whereSql.append("UPDATE ").append(tableName);
                    whereSql.append(" set `").append(e.getFieldKey()).append("` = ");
                    whereSql.append("?");
                    objects.add(v.getReplaceValue());
                    whereSql.append(" where `").append(e.getFieldKey()).append("`");
                    if (v.getReplaceEnum().equals(FieldSetupReplaceEnum.replaceNull)) {
                        whereSql.append(" IS NULL ");
                    } else {
                        whereSql.append(" = ?");
                        objects.add(v.getReplaceOriginalValue());
                    }
                    dorisJdbcTemplate.update(whereSql.toString(), objects.toArray());
                }));
    }

    /**
     * 类型转换 函数拼接
     *
     * @param fieldSetup 字段设置的属性值
     */
    private String fieldAttributeChange(FieldSetupParams.FieldSetup fieldSetup) {
        String functionStr = fieldSetup.getFunctionStr();
        //根据key映射关系 替换
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(fieldSetup));
        functionStr = String.format(functionStr, fieldSetup.getKeyFillingSequence().stream().map(jsonObject::get).toArray());
        return functionStr;
    }
}
