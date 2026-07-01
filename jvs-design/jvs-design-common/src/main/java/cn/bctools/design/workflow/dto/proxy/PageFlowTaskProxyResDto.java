package cn.bctools.design.workflow.dto.proxy;

import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.design.workflow.enums.FlowTaskProxyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("工作流代理分页查询响应")
public class PageFlowTaskProxyResDto extends FlowTaskProxy {

    @ApiModelProperty(value = "1-待生效,2-代理中,3-已过期,4-已撤销")
    private FlowTaskProxyStatusEnum status;
}
