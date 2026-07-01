package cn.bctools.rule.business.role;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class RoleDto {

    @ParameterValue(info = "选择角色", necessity = false, type = InputType.role, explain = "选择一个角色信息,返回角色ID值")
    public String role;

}
