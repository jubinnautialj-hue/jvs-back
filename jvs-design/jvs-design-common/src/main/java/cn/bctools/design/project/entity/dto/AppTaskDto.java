package cn.bctools.design.project.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wayne
 * 应用待办提醒设置
 */
@Data
@ApiModel("应用待办提醒设置")
@Accessors(chain = true)
public class AppTaskDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "是否开启待办提醒")
    private Boolean enableTask;
    @ApiModelProperty(value = "待办提醒应用ID")
    private String taskAppId;
    @ApiModelProperty(value = "待办提醒应用Secret")
    private String taskAppSecret;
    @ApiModelProperty(value = "待办提醒创建单据Api")
    private String taskPushApi;
    @ApiModelProperty(value = "待办提醒关闭单据Api")
    private String taskCloseApi;
    @ApiModelProperty(value = "待办提醒撤回单据Api")
    private String taskRecallApi;
    @ApiModelProperty(value = "待办提醒更新单据Api")
    private String taskUpdateApi;
    @ApiModelProperty(value = "默认跳转地址")
    private String taskFormUrl;

}
