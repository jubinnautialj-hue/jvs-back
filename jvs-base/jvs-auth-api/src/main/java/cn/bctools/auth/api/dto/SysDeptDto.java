package cn.bctools.auth.api.dto;

import cn.bctools.common.enums.DeptEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 系统部门信息
 *
 * @author: GuoZi
 */
@Data
@ApiModel("系统部门信息")
@Accessors(chain = true)
public class SysDeptDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "部门名称")
    private String name;
    @ApiModelProperty(value = "部门类型，部门，子公司")
    private DeptEnum type;
    @ApiModelProperty(value = "部门编码")
    private String deptCode;
    @ApiModelProperty(value = "顶级部门的上级id默认为当前租户id")
    private String parentId;
    @ApiModelProperty(value = "排序号")
    private Integer sort;
    @ApiModelProperty(value = "下级部门")
    List<SysDeptDto> childList;

    @ApiModelProperty(value = "部门负责人Id")
    private String leaderId;
    @ApiModelProperty(value = "是否删除")
    private Boolean delFlag;

}
