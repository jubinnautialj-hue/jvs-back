package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Retrieval option html.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class RetrievalOptionHtml {
    /**
     * The All label.
     */
    @ApiModelProperty("顶部显示的名称")
    String allLabel;
    /**
     * The Close enable.
     */
    @ApiModelProperty("是否所有收起")
    Boolean closeEnable;
    /**
     * The Expand all.
     */
    @ApiModelProperty("是否全部展开")
    Boolean expandAll;
    /**
     * The All children.
     */
    @ApiModelProperty("查询所有子节点")
    Boolean allChildren;
    /**
     * The Filter list.
     */
    @ApiModelProperty("过滤条件")
    List filterList;
}
