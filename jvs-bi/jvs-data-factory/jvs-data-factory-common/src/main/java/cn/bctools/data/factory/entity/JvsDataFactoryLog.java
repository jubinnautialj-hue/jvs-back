package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.handler.NodeLogHandler;
import cn.bctools.data.factory.html.FNodeType;
import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 数据工厂-记录
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_data_factory_log", autoResultMap = true)
@Data
@Accessors(chain = true)
@ApiModel("数据工厂-记录")
public class JvsDataFactoryLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    /**
     * dataId
     */
    @ApiModelProperty("dataId")
    @TableField("data_id")
    private String dataId;

    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    @TableField("start_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    @TableField("end_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 时长
     */
    @ApiModelProperty("时长")
    @TableField("duration")
    private Long duration;

    /**
     * 逻辑删除
     */
    @ApiModelProperty("逻辑删除")
    @TableField("del_flag")
    private Boolean delFlag;


    /**
     * 更新方式 auto 自动 manual 手动
     */
    @ApiModelProperty("更新方式 auto 自动 manual 手动")
    @TableField("operate_method")
    private OperateMethodEnum operateMethod;

    /**
     * 操作人id
     */
    @ApiModelProperty("操作人id")
    @TableField("operator_id")
    private String operatorId;
    /**
     * 输出条数
     */
    @ApiModelProperty("输出条数")
    @TableField("out_number")
    private String outNumber;
    /**
     * 执行条数
     */
    @ApiModelProperty("执行条数")
    @TableField("exec_number")
    private Long execNumber;

    /**
     * 操作人名称
     */
    @ApiModelProperty("操作人名称")
    @TableField("operator_name")
    private String operatorName;

    /**
     * 执行结果 1成功 0失败
     */
    @ApiModelProperty("执行结果 1成功 0失败")
    @TableField("execution_results")
    private Boolean executionResults;

    /**
     * 失败原因
     */
    @ApiModelProperty("失败原因")
    @TableField("failure_reason")
    private String failureReason;

    /**
     * 节点日志
     */
    @ApiModelProperty("节点日志")
    @TableField(value = "node_log", typeHandler = NodeLogHandler.class)
    private List<NodeLog> nodeLog;

    @Data
    @Accessors(chain = true)
    @ApiModel("节点日志")
    public static class NodeLog {

        @ApiModelProperty("节点id")
        private String nodeId;

        @ApiModelProperty("当前节点处理完成后的数据量")
        private Long count;

        @ApiModelProperty("当前节点的类型")
        private FNodeType type;

        @ApiModelProperty("当前节点名称")
        private String nodeName;

        @ApiModelProperty("时长")
        private Long duration;
    }

}
