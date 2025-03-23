package cn.bctools.design.rule.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.rule.entity.enums.RunType;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
import java.util.Map;

/**
 * @author guojing
 * @describe 逻辑运行时的Po的日志信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_rule_log", autoResultMap = true)
public class RunLogPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("是否是异步执行")
    private Boolean sync;
    @ApiModelProperty("逻辑运行环境(定时任务,测试执行,正式执行)")
    private RunType runType;

    @ApiModelProperty("逻辑标识")
    private String reqRunKey;

    @ApiModelProperty("打印的日志")
    private String logs;

    @ApiModelProperty("打印的日志")
    private String jvsAppId;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("执行总耗时")
    private Long totalExecutionTime;

    @ApiModelProperty("请求参数(json)")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> variableMap;
    /**
     * 返回值
     */
    @TableField(value = "log_result", typeHandler = Fastjson2TypeHandler.class)
    private Object result;
    /**
     * 执行状态
     */
    private Boolean status;
    /**
     * 索引号，第几条，主要用于子画布的日志信息展示。
     */
    private Integer sort;
    /**
     * 上级画布的id .如果为第二层，则表示。画布Id 为逻辑的key
     */
    private String parentId;
    /**
     * 路径
     */
    private String path;
    /**
     * 日志执行Id
     */
    private String tid;
    /**
     * 请求数据值
     */
    private String reqData;
    /**
     * 错误信息
     */
    private String errorMsg;

    @ApiModelProperty("租户")
    private String tenantId;

}
