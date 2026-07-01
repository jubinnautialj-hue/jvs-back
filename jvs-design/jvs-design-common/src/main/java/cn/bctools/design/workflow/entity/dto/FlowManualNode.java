package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 流转的人工节点
 */

@Data
@Accessors(chain = true)
@ApiModel("流转的人工节点")
public class FlowManualNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "节点id")
    private String id;

    @ApiModelProperty(value = "节点名称")
    private String name;
}
