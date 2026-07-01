package cn.bctools.design.workflow.dto.testflow;

import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 发起工作流测试准备数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("发起工作流测试准备数据响应")
public class FlowTestPrepareDto extends FlowDesign {

    @ApiModelProperty("人工节点集合")
    private List<Node> manualNodes;

    @ApiModelProperty(value = "工作流设计JSON")
    private String designBody;
}
