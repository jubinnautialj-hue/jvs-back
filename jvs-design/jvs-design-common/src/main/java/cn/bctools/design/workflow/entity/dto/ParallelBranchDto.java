package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 并行分支完成状态
 */
@Data
@Accessors(chain = true)
@ApiModel("并行分支")
public class ParallelBranchDto implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "节点id")
    private String branchId;

    @ApiModelProperty(value = "TRUE-完成，FALSE-未完成")
    private Boolean complete;
}
