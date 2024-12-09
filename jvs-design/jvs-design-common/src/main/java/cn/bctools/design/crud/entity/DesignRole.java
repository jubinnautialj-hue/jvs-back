package cn.bctools.design.crud.entity;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.enums.DataScopeType;
import cn.bctools.design.data.fields.dto.DataConditionDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DesignRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("权限人员/角色")
    private List<PersonnelDto> personnels;
    @ApiModelProperty("自定义人物，或所有人")
    private PersonnelTypeEnum personType;
    @ApiModelProperty("操作权限")
    private List<String> operation;
    @ApiModelProperty("树的操作权限")
    private List<String> treeOperation;

    @ApiModelProperty(value = "数据权限", allowableValues =
            "  全部数据：all" +
                    "  本人提交：self" +
                    "  本部门提交：curr_dept" +
                    "  下级部门提交：curr_dept_tree" +
                    "  表单内容：form_item")
    private List<DataScopeType> scopeList;

    @ApiModelProperty("筛选条件")
    private List<DataConditionDto> conditionList;

    /**
     * 根据人员类型获取id集合
     *
     * @param type 类型
     * @return id集合
     */
    public List<String> getPersonnelIdByType(PersonnelTypeEnum type) {
        if (CollectionUtils.isEmpty(this.getPersonnels())) {
            return Collections.emptyList();
        }
        return this.getPersonnels().stream().filter(r -> type.equals(r.getType()))
                .map(PersonnelDto::getId)
                .collect(Collectors.toList());
    }

}
