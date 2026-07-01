package cn.bctools.rule.business.user;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class UserSearchDto {

    @ParameterValue(info = "用户搜索", explain = "如果使用逗号,分割则搜索多个")
    public String searchUser;
    @ParameterValue(info = "类型", type = InputType.selected, cls = SearchUserSelected.class)
    public SearchType type;
}

