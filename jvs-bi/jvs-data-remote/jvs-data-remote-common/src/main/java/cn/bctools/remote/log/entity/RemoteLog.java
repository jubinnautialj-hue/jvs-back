package cn.bctools.remote.log.entity;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel("数据服务调用日志")
public class RemoteLog {

    @ApiModelProperty("id")
    private String id;

    /**
     * 服务id
     */
    @ApiModelProperty("服务id")
    private String serverId;

    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serverName;

    /**
     * 调用时使用的凭证
     */
    @ApiModelProperty("调用时使用的凭证")
    private String secret;

    /**
     * 凭证说明
     */
    @ApiModelProperty("凭证说明")
    private String secretRemark;

    /**
     * 服务详情
     */
    @ApiModelProperty("服务详情")
    private String serverAttr;

    /**
     * 调用人
     */
    @ApiModelProperty("调用人")
    private String invoker;

    /**
     * ip
     */
    @ApiModelProperty("调用ip")
    private String ip;

    /**
     * 调用时间
     */
    @ApiModelProperty("调用时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime callDate;

    /**
     * 调用状态 true调用成功 false调用失败
     */
    @ApiModelProperty("调用状态")
    private Boolean callStatus;

    /**
     * 数据获取状态 true有数据 false无数据
     */
    @ApiModelProperty("数据获取状态")
    private Boolean dataGetStatus;
}
