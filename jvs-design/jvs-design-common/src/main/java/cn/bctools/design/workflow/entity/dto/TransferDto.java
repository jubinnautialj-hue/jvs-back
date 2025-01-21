package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 任务移交记录
 */
@Data
@ApiModel("任务移交记录")
public class TransferDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "被代理用户id")
    private String userId;
    @ApiModelProperty(value = "被代理用户名")
    private String userName;
    @ApiModelProperty(value = "代理用id")
    private String proxyUserId;
    @ApiModelProperty(value = "代理用户名")
    private String proxyUserName;
    @ApiModelProperty(value = "说明")
    private String directions;
    @ApiModelProperty(value = "移交时间")
    private String time;
}
