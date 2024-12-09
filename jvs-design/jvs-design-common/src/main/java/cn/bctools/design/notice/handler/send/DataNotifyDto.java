package cn.bctools.design.notice.handler.send;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.handler.bo.NoticeDataExtendBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 消息通知
 */
@Data
@Accessors(chain = true)
@ApiModel("消息通知")
public class DataNotifyDto {

    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @ApiModelProperty(value = "应用id")
    private String clientId;

    @ApiModelProperty(value = "用户集合")
    private List<UserDto> users;

    @ApiModelProperty(value = "通知标题")
    private String title;

    @ApiModelProperty(value = "图片地址")
    private String imgUrl;

    @ApiModelProperty(value = "通知内容")
    private String content;

    @ApiModelProperty(value = "内容链接")
    private String messageUrl;

    @ApiModelProperty("扩展配置")
    private NoticeExtendTemplateDto template;

    @ApiModelProperty(value = "模板变量")
    private Map<String, Map<String, String>> templateVariable;

    @ApiModelProperty(value = "扩展数据")
    private NoticeDataExtendBo noticeDataExtend;

}
