package cn.bctools.design.crud.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 树形字典操作
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("树形分类字典")
@Accessors(chain = true)
public class JvsTreeSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("字典id")
    private String id;

    @ApiModelProperty("字典名称")
    private String name;

    @ApiModelProperty("显示值")
    private String value;

    @ApiModelProperty("备注信息")
    private String remarks;

    @ApiModelProperty("上级id, 根节点为-1")
    private String parentId;

}
