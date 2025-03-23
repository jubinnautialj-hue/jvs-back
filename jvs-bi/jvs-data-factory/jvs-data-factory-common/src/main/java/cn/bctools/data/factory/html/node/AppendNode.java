package cn.bctools.data.factory.html.node;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.dto.AppendParam;
import cn.bctools.data.factory.html.node.params.AppendParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据追加
 *
 * @author guojing
 */
@Service
public class AppendNode implements Frun<AppendParams> {


    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, AppendParams appendParams) {
        AppendParams sourceData = appendParams.getSourceData();
        List<AppendParam> fieldList = sourceData.getAppendObj().getFields();
        List<DataSourceField> title = BeanUtil.copyToList(fieldList, DataSourceField.class);
        String nodeDocumentName = appendParams.getTableName();
        //先创建表 因为这里需要创建全量表
        this.createTable(nodeDocumentName, title, null, Boolean.FALSE, new ArrayList<>());
        FData fData = new FData().setTitle(new ArrayList<>()).setDocumentName(nodeDocumentName);
        //循环
        linkBody.keySet().forEach(nodeId -> {
            List<AppendParam> appendParamList = fieldList.stream().filter(e -> e.getStageFieldMap().containsKey(nodeId)).collect(Collectors.toList());
            //通过不同的节点生成不同的sql
            if (!appendParamList.isEmpty()) {
                List<DataSourceField> sourceField = appendParamList.stream().map(e -> e.getStageFieldMap().get(nodeId)).collect(Collectors.toList());
                List<DataSourceField> targetFieldList = appendParamList.stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e), DataSourceField.class)).collect(Collectors.toList());
                StringBuffer sql = new StringBuffer();
                List<Object> objects = createSql(sourceField, targetFieldList, sql, linkBody.get(nodeId).getDocumentName(), nodeDocumentName, Boolean.FALSE, Boolean.FALSE, sourceData);
                //执行入库
                this.save(sql.toString(), nodeDocumentName, sourceField, Boolean.FALSE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
            }
        });
        return fData.setTitle(title);
    }
}
