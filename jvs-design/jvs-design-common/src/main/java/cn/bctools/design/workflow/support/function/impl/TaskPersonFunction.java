package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesEndConditionEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.Leader;
import cn.bctools.design.workflow.model.properties.Target;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 * 审批人选择
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskPersonFunction extends AbstractFunctionHandler<List<UserDto>, RuntimeData> {

    private final AuthUserServiceApi authUserServiceApi;
    private final AuthDeptServiceApi authDeptServiceApi;
    private final FlowDynamicDataService flowDynamicDataService;

    @Override
    public List<UserDto> invoke(Node node, RuntimeData runtimeData) {
        switch (node.getProps().getType()) {
            case ASSIGN_USER:
            case SELF_SELECT:
                return getPersonnelUsers(node);
            case LEADER_TOP:
                Leader leader = node.getProps().getLeader();
                int leaderLevel = NodePropertiesEndConditionEnum.TOP.equals(leader.getEndCondition()) ? -1 : leader.getLeaderLevel();
                return getLeaderUsers(leaderLevel, runtimeData, Boolean.FALSE);
            case LEADER:
                return getLeaderUsers(node.getProps().getLeader().getLeaderLevel(), runtimeData, Boolean.TRUE);
            case ROLE:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                return getPersonnelRoleUsers(node, runtimeData.getFlowTask().getCreateById(), runtimeData.getData());
            case SELF:
                return getSelf(runtimeData);
            case JOB:
                return getPersonnelJobUsers(node);
            case USER_FIELD:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                return getPersonnelFieldUsers(node, runtimeData.getData());
            default:
                log.info("待审批人类型[{}]不存在", node.getProps().getType());
                break;
        }
        return Collections.emptyList();
    }

    /**
     * 没有审批数据时，查询审批数据
     *
     * @param runtimeData 运行时数据
     */
    private void initData(RuntimeData runtimeData) {
        // 没有数据时，需要查询数据（批量审批没有传递数据）
        if (ObjectNull.isNotNull(runtimeData.getData())) {
            return;
        }
        FlowTask flowTask = runtimeData.getFlowTask();
        runtimeData.setData(Convert.convert(JSONObject.class, flowDynamicDataService.querySingle(flowTask.getDataModelId(), flowTask.getDataId())));
    }

    /**
     * 从节点设计中获取配置好的用户信息
     *
     * @param node 节点
     * @return 用户集合
     */
    private List<UserDto> getPersonnelUsers(Node node) {
        List<UserDto> users = new ArrayList<>();
        List<PersonnelDto> personnels = node.getProps().getTargetObj().getPersonnelByType(TargetObjectTypeEnum.user);
        if (CollectionUtils.isEmpty(personnels)) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        personnels.forEach(u -> users.add(new UserDto().setId(u.getId()).setRealName(u.getName())));
        return users;
    }

    /**
     * 查询角色对应的所有人
     *
     * @param node         节点
     * @param createUserId 发起人id
     * @return 用户集合
     */
    private List<UserDto> getPersonnelRoleUsers(Node node, String createUserId, JSONObject data) {
        Target targetObj = node.getProps().getTargetObj();
        // 查询角色对应的用户
        List<String> roleIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.role);
        if (ObjectNull.isNull(roleIds)) {
            String msg = NodeTypeEnum.SP.equals(node.getType()) ? "节点未设置审批人" : "环节未设置抄送人请检查设计";
            throw new BusinessException(msg, node.getName());
        }
        // 得到用以确定角色管理范围的部门id（优先使用节点配置的部门id作为筛选条件）
        String roleScopeConditionDeptIdKey = targetObj.getRoleScopeConditionDeptIdKey();
        BiFunction<JSONObject, String, String> getFilterScopeDeptIdFunction = (dataObj, deptFieldKey) -> {
            // 未显示配置部门，默认使用发起人所属部门作为筛选条件
            if (ObjectNull.isNull(deptFieldKey)) {
                return authUserServiceApi.getById(createUserId).getData().getDeptId();
            }
            // 配置了部门组件key，则使用部门组件key获取数据中的部门id
            Object dataDeptIdObj = Optional.ofNullable(dataObj).orElseGet(JSONObject::new).get(deptFieldKey);
            if (ObjectNull.isNull(dataDeptIdObj)) {
                return null;
            }
            // 部门组件的数据可能是多选(数据结构为：数组)，可能是单选(数据结构为：字符串)
            if (dataDeptIdObj instanceof Collection) {
                return Convert.toList(String.class, dataDeptIdObj).get(0);
            } else {
                return String.valueOf(dataDeptIdObj);
            }
        };
        String filterScopeDeptId = getFilterScopeDeptIdFunction.apply(data, roleScopeConditionDeptIdKey);
        SearchUserDto search = new SearchUserDto()
                .setRoleIds(roleIds)
                // 若角色配置了权限范围，则会筛选出权限范围内包括发起人所属部门的用户id
                .setFilterScopeRoleUser(Boolean.TRUE)
                .setFilterScopeDeptId(filterScopeDeptId);
        return authUserServiceApi.userSearch(search).getData();
    }

    /**
     * 发起人
     *
     * @param runtimeData 运行时信息
     * @return 用户集合
     */
    private List<UserDto> getSelf(RuntimeData runtimeData) {
        return Collections.singletonList(new UserDto()
                .setId(runtimeData.getFlowTask().getCreateById())
                .setRealName(runtimeData.getFlowTask().getCreateBy()));
    }

    /**
     * 获取主管集合
     *
     * @param leaderLevel 发起人的几级主管
     * @param runtimeData 运行时参数
     * @param assignLevel true-获取指定级别主管，false-获取连续多级部门主管
     * @return 主管集合
     */
    private List<UserDto> getLeaderUsers(Integer leaderLevel, RuntimeData runtimeData, Boolean assignLevel) {
        // 查询发起人所属部门
        UserDto sendUserDto = authUserServiceApi.getBasicInfoById(runtimeData.getFlowTask().getCreateById()).getData();
        if (ObjectUtil.isNull(sendUserDto)) {
            throw new BusinessException("未找到发起人的信息");
        }
        String deptId = sendUserDto.getDeptId();
        if (StringUtils.isBlank(sendUserDto.getDeptId())) {
            return Collections.emptyList();
        }
        // 根据”发起人的几级主管“，获取相应的主管信息
        List<SysDeptDto> sysDeptDtos = authDeptServiceApi.getAll().getData();
        if (CollectionUtils.isEmpty(sysDeptDtos)) {
            return Collections.emptyList();
        }
        // 上级部门集合
        LinkedList<SysDeptDto> parentDepts = new LinkedList<>();
        SysDeptDto deptDto = sysDeptDtos.stream().filter(d -> d.getId().equals(deptId)).findFirst().get();
        parentDepts.addLast(deptDto);
        findParentDept(parentDepts, deptDto, sysDeptDtos, leaderLevel, runtimeData.getUser());
        // 按部门顺序获取部门主管信息
        return queryLeaderUsers(parentDepts, leaderLevel, assignLevel);
    }

    /**
     * 查找上级部门
     *
     * @param parentDepts 上级部门有序集合
     * @param deptDto     部门
     * @param sysDeptDtos 所有部门集合
     * @param leaderLevel 查找发起人的第N级主管（-1表示查找到最上层部门）
     * @param userDto     登录用户信息
     */
    private void findParentDept(LinkedList<SysDeptDto> parentDepts, SysDeptDto deptDto, List<SysDeptDto> sysDeptDtos, Integer leaderLevel, UserDto userDto) {
        if (ObjectUtil.isNull(deptDto)) {
            return;
        }
        // 循环查找上级部门。true-继续找上级部门，false-结束
        boolean hasParent = true;
        do {
            SysDeptDto finalDeptDto = deptDto;
            String parentId = finalDeptDto.getParentId();
            // true：有上级部门，false：无上级部门
            hasParent = StringUtils.isNotBlank(parentId) && !userDto.getTenantId().equals(parentId);
            // 不超过发起人的第N级主管
            if (leaderLevel > 0 && hasParent) {
                // true-继续找上级部门，false-结束
                hasParent = parentDepts.size() < leaderLevel;
            }
            if (Boolean.FALSE.equals(hasParent)) {
                continue;
            }
            deptDto = sysDeptDtos.stream().filter(d -> d.getId().equals(parentId)).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(deptDto)) {
                parentDepts.addLast(deptDto);
            }
        } while (ObjectUtil.isNotNull(deptDto) && hasParent);
    }

    /**
     * 查询主管集合
     *
     * @param parentDepts 上级部门集合
     * @param leaderLevel 发起人的几级主管
     * @param assignLevel true-获取指定级别主管，false-获取连续多级部门主管
     * @return 主管集合
     */
    private List<UserDto> queryLeaderUsers(LinkedList<SysDeptDto> parentDepts, Integer leaderLevel, Boolean assignLevel) {
        List<String> leaderIds = parentDepts.stream()
                .map(SysDeptDto::getLeaderId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(leaderIds)) {
            return Collections.emptyList();
        }
        List<UserDto> userDtos = authUserServiceApi.getByIds(leaderIds).getData();
        if (CollectionUtils.isEmpty(userDtos)) {
            return Collections.emptyList();
        }
        // 按部门层级顺序排序
        List<UserDto> sortUserDto = new ArrayList<>();
        if (assignLevel) {
            // 获取指定级别主管
            int i = 0;
            for (SysDeptDto d : parentDepts) {
                i++;
                if (leaderLevel == i && StringUtils.isNotBlank(d.getLeaderId())) {
                    sortUserDto.add(userDtos.stream().filter(u -> u.getId().equals(d.getLeaderId())).findFirst().get());
                }
            }
        } else {
            // 获取连续多级部门主管
            parentDepts.forEach(d -> {
                if (StringUtils.isNotBlank(d.getLeaderId())) {
                    sortUserDto.add(userDtos.stream().filter(u -> u.getId().equals(d.getLeaderId())).findFirst().get());
                }
            });
        }

        return sortUserDto;
    }

    /**
     * 查询岗位对应的所有人
     * @param node 节点
     * @return 用户集合
     */
    private List<UserDto> getPersonnelJobUsers(Node node) {
        // 查询岗位对应的所有人
        List<String> jobIds = node.getProps().getTargetObj().getPersonnelIdByType(TargetObjectTypeEnum.job);
        if (ObjectNull.isNull(jobIds)) {
            throw new BusinessException("节点未设置审批人", node.getName());
        }
        SearchUserDto search = new SearchUserDto();
        search.setJobIds(jobIds);
        return authUserServiceApi.userSearch(search).getData();
    }

    /**
     * 查询成员字段对应的人员
     *
     * @param node 节点
     * @param data 数据
     * @return 用户集合
     */
    private List<UserDto> getPersonnelFieldUsers(Node node, JSONObject data) {
        if (ObjectNull.isNull(data)) {
            return Collections.emptyList();
        }
        // 查询成员字段对应的配置
        List<PersonnelDto> personnels = node.getProps().getTargetObj().getPersonnelByType(TargetObjectTypeEnum.user_field);
        if (CollectionUtils.isEmpty(personnels)) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        // 支持用户组件
        List<String> userFieldKeys = personnels.stream().filter(p -> PersonnelTypeEnum.user.equals(p.getType())).map(PersonnelDto::getId).collect(Collectors.toList());
        if (ObjectNull.isNull(userFieldKeys)) {
            return Collections.emptyList();
        }
        List<String> userIds = userFieldKeys.stream().map(data::get).filter(ObjectNull::isNotNull)
                .flatMap(uidObj -> {
                    // 用户组件的数据可能是多选(数据结构为：数组)，可能是单选(数据结构为：字符串)
                    if (uidObj instanceof Collection) {
                        return Convert.toList(String.class, uidObj).stream();
                    } else {
                        return Stream.of((String) uidObj);
                    }
                })
                .collect(Collectors.toList());
        if (ObjectNull.isNull(userIds)) {
            return Collections.emptyList();
        }
        SearchUserDto search = new SearchUserDto();
        search.setUserIds(userIds);
        Map<String, UserDto> userMap = authUserServiceApi.userSearch(search).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return userIds.stream()
                .map(userMap::get)
                .collect(Collectors.toList());
    }
}
