package cn.bctools.common.enums;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 通知配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysNoticeConfig extends SysConfigBase<SysNoticeConfig> {
    @ApiModelProperty("false-禁用，true-启用")
    private Boolean enabled;
    @ApiModelProperty("数据模型id")
    private String modelId;
    @ApiModelProperty("通知名称")
    private String name;
    @ApiModelProperty("触发设置")
    private JSONObject triggerSetting;
    @ApiModelProperty("接收者")
    private List<JSONObject> receiver;
    @ApiModelProperty("扩展配置")
    private List<JSONObject> extend;
    @ApiModelProperty("模板变量")
    Map<String, Map<String, String>> variable;
    @ApiModelProperty(value = "标题html")
    private String titleHtml;
    @ApiModelProperty(value = "内容html")
    private String contentHtml;
    @ApiModelProperty(value = "发送消息间隔")
    private SendMessageTimeLimit timeLimit;
}
