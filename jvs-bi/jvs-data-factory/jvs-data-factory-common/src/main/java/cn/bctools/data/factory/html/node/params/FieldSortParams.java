package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.node.dto.FieldSortDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class FieldSortParams extends NodeHtml<FieldSortParams> {

    private List<FieldSortDto> fieldSortList;

}
