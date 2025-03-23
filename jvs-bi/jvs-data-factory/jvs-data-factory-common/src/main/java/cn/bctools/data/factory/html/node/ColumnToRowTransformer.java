package cn.bctools.data.factory.html.node;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.ColumnToRowParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 列转行
 *
 * @author xiaohui
 * <a href="https://ask.selectdb.com/questions/D15v1/zui-jia-shi-jian-doris-zhong-shi-xian-xing-lie-zhuan-huan">sql参考资料</a>
 */
@Service
public class ColumnToRowTransformer implements Frun<ColumnToRowParams> {
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, ColumnToRowParams columnToRowParams) {
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
        //添加没有进行列转行的字段
        List<DataSourceField> columnToRowList = columnToRowParams.getColumnToRowObj().getColumnToRowList();
        String noTransformer = sourceFieldList.stream().map(DataSourceField::getFieldKey).filter(e -> columnToRowList.stream().noneMatch(v -> v.getFieldKey().equals(e))).collect(Collectors.joining("`,`"));
        if (StrUtil.isNotBlank(noTransformer)) {
            noTransformer = "`" + noTransformer + "`,";
        }
        sql.append(noTransformer);
        //核心sql生成 具体实现方式 查看上面的参考资料
        sql.append("element_at(arr,1) as typeValue,")
                .append("element_at ( arr, 2 ) AS rowValue ")
                .append("FROM (SELECT")
                .append(noTransformer)
                .append(" SPLIT_BY_STRING ( sub, ',' ) AS arr ")
                .append("FROM ( SELECT ")
                .append(noTransformer);
        //生成 拼接函数
        String funStr = columnToRowList.stream().map(e -> {
            String functionStr = "concat('" + e.getFieldName() + "',',',";
            if (ObjectUtil.isNotEmpty(e.getDefaultValue())) {
                functionStr = functionStr + "IF(`" + e.getFieldKey() + "` is NOT NULL,`" + e.getFieldKey() + "`,";
                if (e.getDataFieldTypeClassify().equals(DataFieldTypeClassifyEnum.数字)) {
                    functionStr = functionStr + e.getDefaultValue();
                } else {
                    functionStr = functionStr + "'" + e.getDefaultValue() + "'";
                }
                functionStr = functionStr + ")";
            } else {
                functionStr = functionStr + "`" + e.getFieldKey() + "`";
            }
            functionStr = functionStr + ")";
            return functionStr;
        }).collect(Collectors.joining(","));
        sql.append("array(").append(funStr).append(") as scores FROM ").append(sourceTable).append(") t LATERAL VIEW explode ( scores ) tbl1 AS sub) a");
        return new ArrayList<>();
    }

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, ColumnToRowParams columnToRowParams) {
        String nodeDocumentName = columnToRowParams.getTableName();
        ColumnToRowParams sourceData = columnToRowParams.getSourceData();
        //获取原有的结构
        //title 需要重新定义
        String next = linkBody.keySet().iterator().next();
        FData fData1 = linkBody.get(next);
        String sourceTableName = fData1.getDocumentName();
        List<DataSourceField> title = fData1.getTitle();
        ColumnToRowParams.ColumnToRowObj columnToRowObj = sourceData.getColumnToRowObj();
        List<DataSourceField> newTitle = title.stream()
                .filter(e -> columnToRowObj.getColumnToRowList().stream().noneMatch(v -> v.getFieldKey().equals(e.getFieldKey())))
                .collect(Collectors.toList());
        //添加新增的值
        newTitle.add(new DataSourceField()
                .setDataId(title.get(0).getDataId())
                .setDorisType("STRING")
                .setFieldType(DataFieldTypeEnum.STRING)
                .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串)
                .setFieldName(columnToRowObj.getColumnToRowKeyNewName())
                .setFieldKey(columnToRowObj.getColumnToRowKeyNewKey())
                .setIsShow(Boolean.TRUE));
        //值
        DataSourceField sourceField = columnToRowObj.getColumnToRowList().get(0);
        DataSourceField newFieldValue = new DataSourceField()
                .setDataId(title.get(0).getDataId())
                .setFieldName(columnToRowObj.getColumnToRowValueNewName())
                .setFieldKey(columnToRowObj.getColumnToRowValueNewKey())
                .setIsShow(Boolean.TRUE);
        switch (sourceField.getDataFieldTypeClassify()) {
            case 数字:
                newFieldValue.setFieldType(DataFieldTypeEnum.DECIMAL)
                        .setLength(18)
                        .setPrecision(5)
                        .setDorisType(DataFieldTypeEnum.DECIMAL.getCreateTable())
                        .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.数字);
                break;
            case 字符串:
                newFieldValue.setFieldType(DataFieldTypeEnum.STRING)
                        .setDorisType(DataFieldTypeEnum.STRING.getCreateTable())
                        .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串);
                break;
            default:
        }
        newTitle.add(newFieldValue);
        //写入数据与创建表
        StringBuffer sql = new StringBuffer();
        List<Object> objects = this.createSql(title, newTitle, sql, sourceTableName, nodeDocumentName, Boolean.FALSE, Boolean.FALSE, sourceData);
        this.save(sql.toString(), nodeDocumentName, newTitle, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        //获取列数据
        return new FData().setTitle(newTitle).setDocumentName(nodeDocumentName);
    }


}
