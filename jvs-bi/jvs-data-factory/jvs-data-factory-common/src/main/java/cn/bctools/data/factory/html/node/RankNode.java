package cn.bctools.data.factory.html.node;


import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.dto.FieldSortDto;
import cn.bctools.data.factory.html.node.params.RankParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.query.QueryExecuteFactory;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据排名
 *
 * @author guojing
 */

@Data
@Service
@AllArgsConstructor
public class RankNode implements Frun<RankParams> {

    private final QueryExecuteFactory queryExecuteFactory;

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, RankParams rankParams) {
        FData fData = new FData();
        String next = linkBody.keySet().iterator().next();
        List<DataSourceField> title = JSONArray.parseArray(JSONObject.toJSONString(linkBody.get(next).getTitle()), DataSourceField.class);
        //上一个节点的数据库名称
        String documentName = linkBody.get(next).getDocumentName();
        //判断配置是否完整
        RankParams sourceData = rankParams.getSourceData();
        if (!sourceData.check()) {
            return fData.setTitle(title).setDocumentName(documentName);
        }
        //中间表名称
        String nodeDocumentName = rankParams.getTableName();
        fData.setDocumentName(nodeDocumentName);
        StringBuffer sql = new StringBuffer();
        List<DataSourceField> sourceFields = JSONArray.parseArray(JSONArray.toJSONString(title), DataSourceField.class);
        //添加排名字段
        DataSourceField rankField = new DataSourceField()
                .setFieldKey(rankParams.getId())
                .setDorisType("DECIMAL(10,0)")
                .setIsShow(Boolean.TRUE)
                .setFieldType(DataFieldTypeEnum.DECIMAL)
                .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.数字)
                .setDataId(rankParams.getDataId())
                .setFieldName("排名");
        title.add(rankField);
        //需要判断是否存在分组排序 如果存在 排序逻辑需要加入分组
        FieldSortDto fieldSortDto = JSONObject.parseObject(JSONObject.toJSONString(rankField), FieldSortDto.class);
        fieldSortDto.setSortType(sourceData.getRankObj().getSortType());
        List<Object> objects = createSql(sourceFields, title, sql, documentName, nodeDocumentName, Boolean.FALSE, Boolean.FALSE, sourceData);
        this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        return fData.setTitle(title);
    }

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, RankParams rankParams) {
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
        String sourceColumn = sourceFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        sourceColumn = "`" + sourceColumn + "`";
        sql.append("SELECT ")
                .append(sourceColumn);
        //添加排名函数
        RankParams.RankObj rankObj = rankParams.getRankObj();
        String dorisFunction = rankObj.getRankRule().getDorisFunction();
        sql.append(",").append(dorisFunction).append(" over( ");
        List<String> groupKey = Optional.ofNullable(rankObj.getGroupKey()).orElse(new ArrayList<>());
        if (!groupKey.isEmpty()) {
            String string = groupKey.stream().map(e -> "`" + e + "`").collect(Collectors.joining(","));
            sql.append("partition by ").append(string);
        }
        sql.append(" order by ").append("`").append(rankObj.getRankKey()).append("` ").append(rankObj.getSortType()).append(") AS rank");
        sql.append(" FROM ")
                .append(bean.getLibraryName())
                .append(".")
                .append(sourceTable)
                .append(" order by `").append(rankObj.getRankKey()).append("` ").append(rankObj.getSortType());
        return new ArrayList<>();
    }
}
