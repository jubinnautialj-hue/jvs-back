package cn.bctools.data.factory.html.node;


import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.FilterParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.query.QueryExecuteFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据过滤
 *
 * @author guojing
 */

@Data
@Service
@AllArgsConstructor
public class FilterNode implements Frun<FilterParams> {

    private final QueryExecuteFactory queryExecuteFactory;

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, FilterParams filterParams) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        //中间表名称
        String nodeDocumentName = filterParams.getTableName();
        fData.setDocumentName(nodeDocumentName);
        //上一个节点的数据库名称
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        List<DataSourceField> title = linkBody.get(next).getTitle();
        List<Object> objects = createSql(new ArrayList<>(), title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, filterParams.getSourceData());
        this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
        return fData.setTitle(title);
    }

    @Override
    public List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, FilterParams filterParams) {
        Frun.super.createSql(sourceFieldList, targetFieldList, sql, sourceTable, targetTableName, sourceFieldEqTargetField, Boolean.FALSE, filterParams);
        return queryExecuteFactory.execute(filterParams.getRuleObj(), sql,Boolean.TRUE);
    }
}
