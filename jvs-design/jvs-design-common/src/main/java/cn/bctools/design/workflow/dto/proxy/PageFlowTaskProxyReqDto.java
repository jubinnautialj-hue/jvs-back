package cn.bctools.design.workflow.dto.proxy;

import cn.bctools.design.workflow.enums.FlowTaskProxyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流代理分页查询入参")
public class PageFlowTaskProxyReqDto {

    @ApiModelProperty(value = "被代理人名称")
    private String userName;

    @ApiModelProperty(value = "状态")
    private FlowTaskProxyStatusEnum status;
}
