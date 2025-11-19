package cn.bctools.rule.business.dept;

import cn.bctools.auth.api.enums.DeptEnum;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author czy
 */
@Data
@Accessors(chain = true)
public class AddOrUpdateDeptDto {

    @ParameterValue(info = "部门 id", necessity = false)
    public String id;
    @ParameterValue(info = "部门名称")
    public String name;
    @ParameterValue(info = "部门编码", necessity = false)
    public String deptCode;
    @ParameterValue(info = "部门类型，部门，子公司", type = InputType.selected, necessity = false, explain = "默认是部门", cls = DeptEnumUpdateSelected.class)
    public DeptEnum type;
    @ParameterValue(info = "顶级部门的上级id默认为当前租户id", necessity = false)
    public String parentId;
    @ParameterValue(info = "排序号", type = InputType.number, defaultValue = "0", necessity = false)
    public Integer sort;
    @ParameterValue(info = "部门负责人Id", type = InputType.user, necessity = false)
    public String leaderId;
    @ParameterValue(info = "是否删除", type = InputType.onOff, necessity = false)
    private Boolean delFlag;

}
