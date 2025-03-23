package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.GroupParams;
import cn.bctools.data.factory.html.run.Frun;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分组节点
 *
 * @author guojing
 */
@Service
public class GroupNode implements Frun<GroupParams> {
    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, GroupParams groupParams) {
        FData fData = new FData();
        //查询条件
        GroupParams sourceData = groupParams.getSourceData();
        GroupParams.GroupObj groupObj = sourceData.getGroupObj();
        //title 需要重新定义
        String next = linkBody.keySet().iterator().next();
        List<DataSourceField> title = linkBody.get(next).getTitle();
        //根据分组与计算字段重新生成
        List<String> groupKey = groupObj.getGroupList().stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        List<DataSourceField> newTitle = new ArrayList<>();
        List<DataSourceField> list = groupObj.getSumList().stream()
                .map(e -> {
                    DataSourceField dataSourceField = title.stream().filter(v -> v.getFieldKey().equals(e.getFieldKey())).findFirst().orElseThrow(() -> new BusinessException("分组汇总获取计算字段错误"));
                    Integer decimalPlace = Optional.ofNullable(e.getDecimalPlace()).orElse(2);
                    DataSourceField newDataSourceField = JSONObject.parseObject(JSONObject.toJSONString(dataSourceField), DataSourceField.class);
                    newDataSourceField.setFieldName(e.getFieldName());
                    //防止原数据被修改
                    return SpringContextUtil.getBean(e.getGroupMethodVal().getDorisClass()).fieldGenerate(newDataSourceField, decimalPlace);
                })
                .collect(Collectors.toList());
        //这里需要根据用户选择的顺序进行排序
        for (String string : groupKey) {
            DataSourceField dataSourceField = title.stream().filter(e -> string.contains(e.getFieldKey())).findFirst().orElseThrow(() -> new BusinessException("没有找到分组源字段"));
            //设置分组字段长度与精度
            newTitle.add(dataSourceField);
        }
        newTitle.addAll(list);
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        String nodeDocumentName = groupParams.getTableName();
        fData.setDocumentName(nodeDocumentName)
                .setTitle(newTitle);
        List<Object> objects = createSql(new ArrayList<>(), newTitle, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, sourceData);
        this.save(sql.toString(), nodeDocumentName, newTitle, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        return fData;
    }

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, GroupParams groupParams) {
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
        GroupParams.GroupObj groupObj = groupParams.getGroupObj();
        sql.append("SELECT ");
        //汇总字段
        String collect = groupObj.getSumList().stream()
                .map(e -> {
                    Boolean truncation = Optional.ofNullable(e.getTruncation()).orElse(Boolean.TRUE);
                    Integer decimalPlace = Optional.ofNullable(e.getDecimalPlace()).orElse(2);
                    return SpringContextUtil.getBean(e.getGroupMethodVal().getDorisClass()).addCondition(e.getFieldKey(), decimalPlace, truncation, e.getFieldKey());
                })
                .collect(Collectors.joining(","));
        //添加分组字段
        String groupField = groupObj.getGroupList().stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        groupField = "`" + groupField + "`";
        sql.append(groupField).append(",");
        sql.append(collect);
        sql.append(" FROM ").append(sourceTable);
        sql.append(" GROUP BY ").append(groupField);
        return new ArrayList<>();
    }

}
