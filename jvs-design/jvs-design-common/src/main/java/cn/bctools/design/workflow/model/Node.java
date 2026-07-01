package cn.bctools.design.workflow.model;

import cn.bctools.design.workflow.model.enums.ConditionOperatorEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流节点
 */

@Data
@Accessors(chain = true)
public class Node {
    /**
     * 基本属性
     */
    @ApiModelProperty(value = "节点id")
    private String id;
    @ApiModelProperty(value = "节点名称")
    private String name;
    @ApiModelProperty(value = "节点类型")
    private NodeTypeEnum type;
    @ApiModelProperty(value = "上级节点id")
    private String pid;
    @ApiModelProperty(value = "下级节点")
    private Node node;
    @ApiModelProperty(value = "节点配置")
    private NodeProperties props;

    /**
     * 条件节点
     */
    @ApiModelProperty(value = "条件组运算关系")
    private ConditionOperatorEnum connection;
    @ApiModelProperty(value = "条件分支集合")
    private List<Condition> conditions;

    /**
     * 并行节点
     */
    @ApiModelProperty(value = "并行分支集合")
    private List<Parallel> parallels;
    @ApiModelProperty(value = "并行节点标识", notes = "当前节点属于那个并行节点. 目前使用并行节点的id作为标识")
    private String parallelFlag;

    /**
     * 表单配置
     */
    private NodeForm nodeForm;

    /**
     * 多分支节点的下级节点id
     * （工作流模板JSON中不存在此字段，扩展用以在分支节点（如条件节点、并行节点）结束后，确定后续节点）
     */
    private String branchNextNodeId;
}
