package cn.bctools.design.notice.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.entity.enums.NoticeTemplateTypeEnum;
import cn.bctools.design.notice.entity.handler.NoticeExtendHandler;
import cn.bctools.design.notice.entity.handler.ReceiverTypeHandler;
import cn.bctools.design.notice.handler.bo.ReceiverBo;
import cn.bctools.design.notice.handler.bo.TriggerSettingBo;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 消息通知
 */

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "jvs_data_notice", autoResultMap = true)
public class DataNoticePo  extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("模板类型")
    @TableField("template_type")
    private NoticeTemplateTypeEnum templateType;

    @ApiModelProperty("数据模型id")
    @TableField("model_id")
    private String modelId;

    @ApiModelProperty("通知名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("触发设置")
    @TableField(value = "trigger_setting", typeHandler = Fastjson2TypeHandler.class)
    private TriggerSettingBo triggerSetting;

    @ApiModelProperty("接收者")
    @TableField(value = "receiver", typeHandler = ReceiverTypeHandler.class)
    private List<ReceiverBo> receiver;

    @ApiModelProperty("扩展配置")
    @TableField(value = "extend", typeHandler = NoticeExtendHandler.class)
    List<NoticeExtendTemplateDto> extend;

    /**
     * key：平台类型
     * value：变量
     *        key: 三方平台模板变量
     *        value: jvs变量
     */
    @ApiModelProperty("模板变量")
    @TableField(value = "variable", typeHandler = Fastjson2TypeHandler.class)
    Map<String, Map<String, String>> variable;

    /**
     * 2.1.6兼容2.1.5的消息配置
     */
    @ApiModelProperty(value = "标题html")
    @TableField(value = "title_html", updateStrategy = FieldStrategy.IGNORED)
    private String titleHtml;

    /**
     * 2.1.6兼容2.1.5的消息配置
     */
    @ApiModelProperty(value = "内容html")
    @TableField(value = "content_html", updateStrategy = FieldStrategy.IGNORED)
    private String contentHtml;

    @ApiModelProperty("应用id")
    @TableField("jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty("false-禁用，true-启用")
    @TableField("enabled")
    private Boolean enabled;
}
