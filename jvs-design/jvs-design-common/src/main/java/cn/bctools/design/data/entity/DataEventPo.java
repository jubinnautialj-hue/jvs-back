package cn.bctools.design.data.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.data.fields.enums.DataEventType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 数据事件
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("jvs_data_event")
public class DataEventPo extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("事件id")
    private String id;

    @ApiModelProperty("应用id")
    private String appId;

    @ApiModelProperty("事件名称")
    private String eventName;

    @ApiModelProperty("数据模型id")
    private String modelId;

    @ApiModelProperty("套件设计id")
    private String designId;

    @ApiModelProperty("逻辑设计id(前置)")
    private String beforeRuleId;
    private Boolean beforeRuleEnable;
    @ApiModelProperty("逻辑设计id(后置)")
    private String afterRuleId;
    private Boolean afterRuleEnable;
    @ApiModelProperty("事件类型")
    private DataEventType eventType;
    @ApiModelProperty("轻应用版本号")
    private String appVersion;

}
