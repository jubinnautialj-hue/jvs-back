package cn.bctools.design.workflow.model.properties;

import cn.bctools.auth.api.dto.PersonnelDto;
import lombok.Data;

import java.util.List;

/**
 * @author zhuxiaokang
 * 审批节点上的审批人确定逻辑配置
 */
@Data
public class ApprovalPersonResolver {

    /**
     * 确定角色管理范围内的人员作为审批人的条件配置
     *  - 默认（无配置时）以发起人所属部门作为条件
     *  - 可指定表单字段(当前仅支持部门组件，且只能选择一个部门)作为条件
     */
    private List<PersonnelDto> roleScopeConditions;

}
