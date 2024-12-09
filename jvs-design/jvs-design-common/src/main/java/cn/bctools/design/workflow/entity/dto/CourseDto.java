package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.entity.enums.TerminatedTypeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 * 处理过程
 */
@Data
@Accessors(chain = true)
@ApiModel("处理过程")
public class CourseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("true-终止任务，false-审批任务")
    private Boolean terminated = Boolean.FALSE;

    @ApiModelProperty("终止任务类型")
    private TerminatedTypeEnum terminatedType;

    @ApiModelProperty("终止任务原因")
    private String terminatedReason;

    @ApiModelProperty(value = "节点id")
    private String nodeId;

    @ApiModelProperty(value = "节点名")
    private String nodeName;

    @ApiModelProperty(value = "节点类型")
    private NodeTypeEnum nodeType;

    @ApiModelProperty(value = "审批结果集合")
    private List<ApproveResultDto> approveResultDtos;

    @ApiModelProperty(value = "通过率", notes = "可能是数字，可能是百分比。根据节点配置的审批模式计算")
    private Long passRate;

    @ApiModelProperty(value = "数据版本")
    private String dataVersion;

    @ApiModelProperty(value = "节点最后处理时间")
    private String time;

    @ApiModelProperty(value = "审批模式：AND-会签，NEXT-依次，OR-或签", notes = "增加、移除审批人，可能会改变当前节点的审批模式")
    private NodePropertiesModeEnum mode;
}
