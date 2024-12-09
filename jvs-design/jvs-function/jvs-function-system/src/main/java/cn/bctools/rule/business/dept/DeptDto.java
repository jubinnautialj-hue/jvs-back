package cn.bctools.rule.business.dept;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DeptDto {
    @ParameterValue(info = "选择部门", type = InputType.dept)
    public String dept;
    @ParameterValue(info = "获取类型", explain = "指定所属公司或不指定类型时返回详细信息，其它类型时返回对应部门的数组id", necessity = false, type = InputType.selected, cls = DeptSelected.class)
    public DeptType type;

}
