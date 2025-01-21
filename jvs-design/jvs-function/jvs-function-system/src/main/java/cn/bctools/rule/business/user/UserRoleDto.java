package cn.bctools.rule.business.user;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class UserRoleDto {

    @ParameterValue(info = "角色id", necessity = false, type = InputType.role)
    public String roleId;
    @ParameterValue(info = "用户id", type = InputType.userList, explain = "获取多个用户,需要参数为用户id值")
    public List<String> userIds;


}
