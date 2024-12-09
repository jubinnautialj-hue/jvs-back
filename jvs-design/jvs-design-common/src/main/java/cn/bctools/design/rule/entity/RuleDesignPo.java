package cn.bctools.design.rule.entity;

import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.handler.Design;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.*;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 逻辑定义数据
 */
@Design(DesignType.rule)
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "jvs_rule_design", autoResultMap = true)
public class RuleDesignPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("应用id")
    private String jvsAppId;

    @ApiModelProperty("逻辑标识")
    private String secret;

    @ApiModelProperty("逻辑类型")
    private RuleType reqType;

    @ApiModelProperty("逻辑设计名称")
    private String name;

    @ApiModelProperty("逻辑设计介绍")
    private String description;

    @ApiModelProperty("是否启用(默认false)")
    private Boolean enable;

    @ApiModelProperty("前端html渲染Json数据")
    private String designDrawingJson;

    @ApiModelProperty("定时任务逻辑")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private TaskCronDto task;
    @ApiModelProperty("mqttDto")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private MqttDto mqttDto;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    @ApiModelProperty("apiCheckDto")
    private ApiCheckDto apiCheckDto;
    @ApiModelProperty("是否开启定时任务")
    private Boolean onTask;

    @ApiModelProperty("接口参数集合")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> parameterPos;

    @ApiModelProperty("参数接口信息")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private RuleParameterInDto parameterIn;

    @ApiModelProperty("参数接口输出信息")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private RuleParameterInDto parameterOut;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("修改人")
    private String updateBy;

    @ApiModelProperty("组件ID")
    private String componentId;

    @ApiModelProperty("组件设计ID")
    private String componentDesignId;
    @ApiModelProperty(value = "缓存时间，默认分钟", notes = "用于逻辑执行缓存，如果大余0，相同请求入参如果有缓存，直接返回上一次的执行结果")
    private Integer cacheMinute;

    @ApiModelProperty(value = "组件类型", notes = "图表、表单、列表页、工作流")
    private DesignType componentType;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除字段")
    private Boolean delFlag;

    /**
     * 一些特殊场景需要异步执行其它情况不建议使用异步
     */
    @ApiModelProperty("是否是异步执行")
    private Boolean sync;

    @ApiModelProperty("租户")
    private String tenantId;
    /**
     * 是否开启日志记录
     * The Open log recording.
     */
    @ApiModelProperty("是否开启日志记录")
    private Boolean openLogRecording;

    @ApiModelProperty("这个逻辑使用到了哪些方法，获取 方法图片")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> icons;

    @ApiModelProperty("引用了工作流的ID集")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> flowDesignIds;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;
    @ApiModelProperty("应用的")
    @TableField(exist = false)
    private String appIdentifier;
    @ApiModelProperty("api文档的地址")
    @TableField(exist = false)
    private String api;

}
