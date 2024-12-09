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
public class UserDto {

    @ParameterValue(info = "用户对象", necessity = false, type = InputType.user, explain = "获取一个用户,需要参数为用户id值")
    public String user;

}
