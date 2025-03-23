package cn.bctools.design.workflow.support.condition.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.model.ConditionProperties;
import cn.bctools.design.workflow.model.condition.ConditionOrganizer;
import cn.bctools.design.workflow.model.enums.ConditionPropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.condition.ConditionInterface;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 发起人条件判断
 */
@Component
@AllArgsConstructor
public class ConditionSendUserImpl implements ConditionInterface {

    private final AuthUserServiceApi authUserServiceApi;

    @Override
    public String getType() {
        return ConditionPropertiesTypeEnum.ORG.getValue();
    }

    @Override
    public Boolean verify(ConditionProperties conditionProperties, RuntimeData runtimeData) {
        List<ConditionOrganizer> conditionOrganizers = JSON.parseArray(JSON.toJSONString(conditionProperties.getValues()), ConditionOrganizer.class);
        String sendUserId = runtimeData.getFlowTask().getCreateById();
        // 优先校验发起人id，若匹配上了，就不再校验角色等
        boolean userContains = conditionOrganizers.stream()
                .anyMatch(organizer -> TargetObjectTypeEnum.user.equals(organizer.getType()) && organizer.getId().equals(sendUserId));
        if (userContains) {
            return Boolean.TRUE;
        }

        // 发起人id未找到匹配的条件 && 有配置部门 || 角色 || 岗位条件，则查询发起人的具体信息
        boolean checkOther = conditionOrganizers.stream().anyMatch(organizer ->
                                TargetObjectTypeEnum.dept.equals(organizer.getType())
                                ||
                                TargetObjectTypeEnum.role.equals(organizer.getType())
                                ||
                                TargetObjectTypeEnum.job.equals(organizer.getType()));
        if (checkOther) {
            // 查询发起人信息
            SearchUserDto search = new SearchUserDto();
            search.setUserIds(Collections.singletonList(runtimeData.getFlowTask().getCreateById()));
            UserDto user = Optional.ofNullable(authUserServiceApi.userSearch(search).getData().get(0)).orElseThrow(() -> new BusinessException("未找到发起人信息"));
            return conditionOrganizers.stream().anyMatch(organizer ->
                            // 部门id匹配条件 OR 角色id匹配条件 OR 岗位id匹配条件
                            (TargetObjectTypeEnum.dept.equals(organizer.getType()) && user.getDept().stream().map(DeptDto::getDeptId).anyMatch(e->e.equals(organizer.getId())))
                            ||
                            (TargetObjectTypeEnum.role.equals(organizer.getType()) && CollectionUtils.isNotEmpty(user.getRoleIds()) && user.getRoleIds().contains(organizer.getId()))
                            ||
                            (TargetObjectTypeEnum.job.equals(organizer.getType()) && organizer.getId().equals(user.getJobId()))
            );
        }

        // 所有条件都不匹配，返回false
        return Boolean.FALSE;
    }
}
