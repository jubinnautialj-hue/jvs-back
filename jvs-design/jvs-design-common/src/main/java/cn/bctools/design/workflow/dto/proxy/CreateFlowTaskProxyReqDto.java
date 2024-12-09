package cn.bctools.design.workflow.dto.proxy;

import cn.bctools.design.workflow.entity.enums.ProxyTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author zhuxiaokang 
 */
@Data
@Accessors(chain = true)
@ApiModel("新增工作流任务代理入参")
public class CreateFlowTaskProxyReqDto {

    @ApiModelProperty(value = "代理类型", required = true)
    private ProxyTypeEnum proxyType;

    @ApiModelProperty(value = "被代理用户id", required = true)
    @NotBlank(message = "请选择被代理人")
    private String userId;

    @ApiModelProperty(value = "被代理用户姓名", required = true)
    @NotBlank(message = "请选择被代理人")
    private String userName;

    @ApiModelProperty(value = "代理用户id", required = true)
    @NotBlank(message = "请选择代理人")
    private String proxyUserId;

    @ApiModelProperty(value = "代理用户姓名", required = true)
    @NotBlank(message = "请选择代理人")
    private String proxyUserName;

    @ApiModelProperty(value = "代理开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "代理结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "说明")
    @TableField("description")
    private String description;
}
