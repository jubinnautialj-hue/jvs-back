package cn.bctools.rule.business.dept;

import cn.bctools.rule.annotations.ParameterValue;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DeleteDeptDto {

    @ParameterValue(info = "部门 id")
    public String id;
}
