package cn.bctools.design.workflow.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("保存工作流设计入参")
public class SaveFlowReqDesign extends UpdateFlowDesignReqDto {

    @ApiModelProperty(value = "工作流设计JSON", required = true)
    @NotNull(message = "工作流设计不能为空")
    private JSONObject designBody;

    @ApiModelProperty(value = "自定义表单节点id")
    private String nodeId;
}
