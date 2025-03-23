package cn.bctools.common.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xh
 */
@Data
@Accessors(chain = true)
public class SysConfigEnterriseWeChat extends SysConfigBase<SysConfigEnterriseWeChat> {

    @ApiModelProperty(value = "企业ID", notes = "在此页面查看：https://work.weixin.qq.com/wework_admin/frame#profile")
    private String appId;
    @ApiModelProperty(value = "企业微信地址")
    private String baseApiUrl = "https://qyapi.weixin.qq.com";
    @ApiModelProperty(value = "开放平台地址")
    private String openUrl = "https://open.weixin.qq.com";
    @ApiModelProperty(value = "扫码登录地址")
    private String scanUrl = "https://open.work.weixin.qq.com/";
    @ApiModelProperty(value = "应用Secret")
    private String appSecret;
    @ApiModelProperty(value = "应用agentId")
    private Integer agentId;
    @ApiModelProperty(value = "是否开启企微扫码登陆")
    private Boolean enableScan;
    @ApiModelProperty(value = "webhook地址")
    private String webhook;

}
