package cn.bctools.design.permission.entity.dto;

import cn.bctools.common.enums.DataScopeType;
import cn.bctools.design.data.fields.dto.DataConditionDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 */

@Data
@Accessors(chain = true)
@ApiModel("权限")
public class PermissionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("操作权限——权限标识")
    private List<String> operation;

    @ApiModelProperty("操作权限——树的权限标识")
    private List<String> treeOperation;

    @ApiModelProperty(value = "数据权限——权限类型", allowableValues =
            "  全部数据：all" +
                    "  本人提交：self" +
                    "  本部门提交：curr_dept" +
                    "  下级部门提交：curr_dept_tree" +
                    "  表单内容：form_item")
    private List<DataScopeType> scopeList;

    @ApiModelProperty("数据权限——筛选条件")
    private List<DataConditionDto> conditionList;
}
