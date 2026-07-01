package cn.bctools.design.workflow.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.handler.CoursesTypeHandler;
import cn.bctools.design.workflow.entity.handler.FlowManualNodeTypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedList;


/**
 * @author zhuxiaokang
 * 工作流任务
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("工作流任务")
@TableName(value = "jvs_flow_task", autoResultMap = true)
public class FlowTask extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "流程编号")
    private String taskCode;

    @ApiModelProperty(value = "任务名称")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "任务标题")
    private String title;

    @ApiModelProperty(value = "工作流id")
    @TableField("flow_design_id")
    private String flowDesignId;

    @ApiModelProperty(value = "工作流设计版本id", notes = "增加工作流版本功能后，新创建的任务都有工作流设计版本id")
    @TableField("flow_version_id")
    private String flowVersionId;

    @ApiModelProperty(value = "工作流流程设计JSON")
    @TableField("flow_design")
    private String flowDesign;

    @ApiModelProperty(value = "数据模型id")
    @TableField("data_model_id")
    private String dataModelId;

    @ApiModelProperty(value = "工作流设计的数据模型id")
    @TableField("design_model_id")
    private String designModelId;

    @ApiModelProperty(value = "发起人表单id")
    @TableField("form_id")
    private String formId;

    @ApiModelProperty(value = "发起人表单版本")
    @TableField("form_version")
    private String formVersion;

    @ApiModelProperty(value = "内容id")
    @TableField("data_id")
    private String dataId;

    @ApiModelProperty(value = "处理过程数组")
    @TableField(value = "courses", typeHandler = CoursesTypeHandler.class)
    private LinkedList<CourseDto> courses;

    @ApiModelProperty(value = "状态：1-待审批，2-已通过，3-已拒绝，4-已终止")
    @TableField("task_status")
    private FlowTaskStatusEnum taskStatus;

    @ApiModelProperty(value = "流转的人工节点", notes = "已处理过的人工节点")
    @TableField(value = "flow_manual_nodes", typeHandler = FlowManualNodeTypeHandler.class)
    private LinkedList<FlowManualNode> flowManualNodes;

    @ApiModelProperty(value = "终止任务原由")
    @TableField("stop_reason")
    private String stopReason;

    @ApiModelProperty(value = "是否是测试任务 0否  1是")
    @TableField("test")
    private Boolean test = Boolean.FALSE;

    @ApiModelProperty(value = "是否删除 0未删除  1已删除")
    @TableField("del_flag")
    @TableLogic
    private Boolean delFlag;

    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;

    /**
     * 工作流任务使用的工作流设计，工作流任务设计相关功能统一使用此字段
     * （此字段的数据来源可能是根据工作流设计版本获取，也可能是自定义的设计保存在{@link flowDesign}）
     */
    @ApiModelProperty(value = "工作流设计")
    @TableField(exist = false)
    private String designBody;
}
