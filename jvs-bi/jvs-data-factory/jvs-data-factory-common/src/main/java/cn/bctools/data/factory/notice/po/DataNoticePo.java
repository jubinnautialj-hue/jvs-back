package cn.bctools.data.factory.notice.po;

import cn.bctools.data.factory.notice.bo.ReceiverBo;
import cn.bctools.data.factory.notice.bo.TriggerSettingBo;
import cn.bctools.data.factory.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.data.factory.notice.handler.NoticeExtendHandler;
import cn.bctools.data.factory.notice.handler.ReceiverTypeHandler;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "jvs_data_notice", autoResultMap = true)
public class DataNoticePo extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("数据集id")
    @TableField("data_factory_id")
    private String dataFactoryId;

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
     * key: 三方平台模板变量
     * value: jvs变量
     */
    @ApiModelProperty("模板变量")
    @TableField(value = "variable", typeHandler = Fastjson2TypeHandler.class)
    Map<String, Map<String, String>> variable;

    @ApiModelProperty("false-禁用，true-启用")
    @TableField("enabled")
    private Boolean enabled;
}
