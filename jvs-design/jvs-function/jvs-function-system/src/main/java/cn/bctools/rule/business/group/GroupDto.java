package cn.bctools.rule.business.group;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class GroupDto {

    @ParameterValue(info = "用户团队", necessity = false, type = InputType.group, explain = "选择一个用户团队")
    public String group;

}
