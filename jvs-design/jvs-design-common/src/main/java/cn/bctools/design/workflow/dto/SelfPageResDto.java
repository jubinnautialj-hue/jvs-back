package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("自己发起的任务列表响应")
public class SelfPageResDto extends FlowTask {

    @ApiModelProperty("扩展信息")
    private FlowExtendDto extend;

    @ApiModelProperty("人工节点集合")
    private List<Node> manualNodes;

    @ApiModelProperty(value = "发起人头像")
    private String headImg;

}
