package cn.bctools.rule.business.user;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DeleteUserDto {

    @ParameterValue(info = "用户id", explain = "需要删除的用户 id", type = InputType.input)
    public String userId;
}
