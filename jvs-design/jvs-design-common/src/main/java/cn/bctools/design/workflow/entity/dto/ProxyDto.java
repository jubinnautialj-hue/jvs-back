package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 * 任务代理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("任务代理")
public class ProxyDto extends TransferDto {
    @ApiModelProperty(value = "TRUE-根据代理设置转交，FALSE-手动转交")
    private Boolean proxy;
}
