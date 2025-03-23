package cn.bctools.data.factory.html.node;

import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.dto.FieldSortDto;
import cn.bctools.data.factory.html.node.params.FieldSortParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.util.SystemTool;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 字段排序
 *
 * @author guojing
 */
@Service
@AllArgsConstructor
public class FieldSortNode implements Frun<FieldSortParams> {


    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, FieldSortParams fieldSortParams) {
        String next = linkBody.keySet().iterator().next();
        String documentName = linkBody.get(next).getDocumentName();
        //防止没有数据
        FData fData = new FData().setDocumentName(documentName);
        SystemTool<List<FieldSortDto>> tool = new SystemTool<>();
        List<FieldSortDto> sortList = fieldSortParams.getSourceData().getFieldSortList();
        sortList = Optional.ofNullable(sortList).orElse(new ArrayList<>());
        tool.set(Constant.SORT_FIELD, sortList);
        //上一个节点的数据库名称
        List<DataSourceField> title = linkBody.get(next).getTitle();
        return fData.setTitle(title);
    }
}
