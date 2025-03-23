package cn.bctools.data.factory.html.node;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.DataDeduplicationParams;
import cn.bctools.data.factory.html.run.Frun;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件分组
 *
 * @author guojing
 */
@Service
public class DataDeduplicationNode implements Frun<DataDeduplicationParams> {

    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, DataDeduplicationParams dataDeduplicationParams) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        //中间表名称
        String nodeDocumentName = dataDeduplicationParams.getTableName();
        fData.setDocumentName(nodeDocumentName);
        //上一个节点的数据库名称
        StringBuffer sql = new StringBuffer();
        String documentName = linkBody.get(next).getDocumentName();
        List<DataSourceField> title = linkBody.get(next).getTitle();
        List<DataSourceField> sourceFieldList = JSONArray.parseArray(JSONObject.toJSONString(title), DataSourceField.class);
        List<DataSourceField> list = dataDeduplicationParams.getSourceData().getDataDeduplicationObj();
        title = title.stream().filter(e -> list.stream().noneMatch(v -> v.getFieldKey().equals(e.getFieldKey()))).collect(Collectors.toList());
        title.addAll(0, list);
        if (!list.isEmpty()) {
            List<Object> objects = createSql(sourceFieldList, title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, dataDeduplicationParams.getSourceData());
            this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.TRUE, list, objects.toArray());
        } else {
            fData.setDocumentName(documentName);
        }
        return fData.setTitle(title);
    }


}
