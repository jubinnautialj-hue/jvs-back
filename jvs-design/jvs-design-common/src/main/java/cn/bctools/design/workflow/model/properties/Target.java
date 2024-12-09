package cn.bctools.design.workflow.model.properties;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 处理人
 */
@Data
public class Target {

    /**
     * 可审批人对象集合
     */
    private List<PersonnelDto> personnels;

    /**
     * 审批人确定逻辑配置
     */
    private ApprovalPersonResolver personnelResolver;


    /**
     * 根据人员类型获取id集合
     *
     * @param type 类型
     * @return id集合
     */
    public List<String> getPersonnelIdByType(TargetObjectTypeEnum type) {
        if (TargetObjectTypeEnum.user_field.equals(type)) {
            return this.getPersonnels().stream()
                    .map(PersonnelDto::getId)
                    .collect(Collectors.toList());
        } else {
            return this.getPersonnels().stream().filter(r -> type.getValue().equals(r.getType().getValue()))
                    .map(PersonnelDto::getId)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 根据人员类型获取集合
     *
     * @param type 类型
     * @return id集合
     */
    public List<PersonnelDto> getPersonnelByType(TargetObjectTypeEnum type) {
        if (CollectionUtils.isEmpty(this.getPersonnels())) {
            return Collections.emptyList();
        }
        if (TargetObjectTypeEnum.user_field.equals(type)) {
            return this.getPersonnels();
        } else {
            return this.getPersonnels().stream().filter(r -> type.getValue().equals(r.getType().getValue())).collect(Collectors.toList());
        }
    }

    /**
     * 校验审批人类型的审批人配置
     *
     * @param propType 审批人类型
     */
    public void checkPersonType(NodePropertiesTypeEnum propType) {
        Boolean result = Boolean.TRUE;
        switch (propType) {
            case ASSIGN_USER:
            case SELF_SELECT:
                result = checkPersonType(PersonnelTypeEnum.user);
                break;
            case ROLE:
                result = checkPersonType(PersonnelTypeEnum.role);
                break;
            case JOB:
                result = checkPersonType(PersonnelTypeEnum.job);
                break;
            case SELF:
            case LEADER_TOP:
            case LEADER:
            case USER_FIELD:
            default:
                // 不需要校验类型
                break;
        }
        if (Boolean.FALSE.equals(result)) {
            throw new BusinessException("的审批人类型配置错误", propType.getDesc());
        }
    }

    /**
     * 校验审批人类型
     *
     * @param personnelType
     * @return true-校验通过，false-校验不通过
     */
    private Boolean checkPersonType(PersonnelTypeEnum personnelType) {
        return this.getPersonnels().stream().allMatch(person -> personnelType.equals(person.getType()));
    }

    /**
     * 获取用以确定角色管理范围内的人员作为审批人的条件配置：部门id字段key
     *
     * @return 未配置——以发起人所属部门作为条件; 已配置——以表单中的部门组件的值作为条件
     */
    public String getRoleScopeConditionDeptIdKey() {
        // 没配置审批人确定逻辑，直接返回null
        if (ObjectNull.isNull(this.getPersonnelResolver())) {
            return null;
        }
        List<PersonnelDto> roleScopeConditions = this.getPersonnelResolver().getRoleScopeConditions();
        if (ObjectNull.isNull(roleScopeConditions)) {
            return null;
        }
        return roleScopeConditions.get(0).getId();
    }
}
