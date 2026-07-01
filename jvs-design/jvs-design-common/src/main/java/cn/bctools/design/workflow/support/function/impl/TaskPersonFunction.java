package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowApprovalUserDTO;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesEndConditionEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.Leader;
import cn.bctools.design.workflow.model.properties.LeaderFlowNodeSource;
import cn.bctools.design.workflow.model.properties.Target;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskPathService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
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
public class TaskPersonFunction extends AbstractFunctionHandler<List<FlowApprovalUserDTO>, RuntimeData> {

    private final AuthUserServiceApi authUserServiceApi;
    private final AuthDeptServiceApi authDeptServiceApi;
    private final FlowDynamicDataService flowDynamicDataService;
    private final FlowTaskPathService flowTaskPathService;

    @Override
    public List<FlowApprovalUserDTO> invoke(Node node, RuntimeData runtimeData) {
        List<FlowApprovalUserDTO> userList = Collections.emptyList();
        switch (node.getProps().getType()) {
            case ASSIGN_USER:
            case SELF_SELECT:
                userList = getPersonnelUsers(node);
                break;
            case LEADER_TOP:
                Leader leader = node.getProps().getLeader();
                int leaderLevel = NodePropertiesEndConditionEnum.TOP.equals(leader.getEndCondition()) ? -1 : leader.getLeaderLevel();
                userList = getLeaderUsers(leader, leaderLevel, node, runtimeData, Boolean.FALSE);
                break;
            case LEADER:
                Leader leaderProp = node.getProps().getLeader();
                userList = getLeaderUsers(leaderProp, leaderProp.getLeaderLevel(), node, runtimeData, Boolean.TRUE);
                break;
            case ROLE:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                userList = getPersonnelRoleUsers(node, runtimeData.getFlowTask().getCreateById(), runtimeData.getData());
                break;
            case SELF:
                userList = getSelf(runtimeData);
                break;
            case JOB:
                userList = getPersonnelJobUsers(node);
                break;
            case USER_FIELD:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                userList = getPersonnelFieldUsers(node, runtimeData.getData());
                break;
            case DEPT:
                // 根据指定的单个、多个部门查找部门主管
                userList = getDeptUsers(node);
                break;
            case DEPT_FIELD:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                userList = getPersonnelFieldDeptUsers(node, runtimeData.getData());
            default:
                log.info("待审批人类型[{}]不存在", node.getProps().getType());
                break;
        }
        return userList.stream().filter(ObjectNull::isNotNull).collect(Collectors.toList());
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
    private List<FlowApprovalUserDTO> getPersonnelUsers(Node node) {
        List<FlowApprovalUserDTO> users = new ArrayList<>();
        List<PersonnelDto> personnels = node.getProps().getTargetObj().getPersonnelByType(TargetObjectTypeEnum.user);
        if (CollectionUtils.isEmpty(personnels)) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        personnels.forEach(u -> users.add(BeanCopyUtil.copy(new UserDto().setId(u.getId()).setRealName(u.getName()), FlowApprovalUserDTO.class)));
        return users;
    }

    /**
     * 查询角色对应的所有人
     *
     * @param node         节点
     * @param createUserId 发起人id
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getPersonnelRoleUsers(Node node, String createUserId, JSONObject data) {
        Target targetObj = node.getProps().getTargetObj();
        // 查询角色对应的用户
        List<String> roleIds = targetObj.getPersonnelIdByType(TargetObjectTypeEnum.role);
        if (ObjectNull.isNull(roleIds)) {
            String msg = NodeTypeEnum.SP.equals(node.getType()) ? "节点未设置审批人" : "环节未设置抄送人请检查设计";
            throw new BusinessException(msg, node.getName());
        }
        // 得到用以确定角色管理范围的部门id（优先使用节点配置的部门id作为筛选条件）
        String roleScopeConditionDeptIdKey = targetObj.getRoleScopeConditionDeptIdKey();
        BiFunction<JSONObject, String, List<String>> getFilterScopeDeptIdFunction = (dataObj, deptFieldKey) -> {
            // 未显示配置部门，默认使用发起人所属部门作为筛选条件
            if (ObjectNull.isNull(deptFieldKey)) {
                return Optional.ofNullable(authUserServiceApi.getById(createUserId).getData().getDept())
                        .orElseGet(ArrayList::new)
                        .stream()
                        .map(DeptDto::getDeptId)
                        .collect(Collectors.toList());
            }
            // 配置了部门组件key，则使用部门组件key获取数据中的部门id
            Object dataDeptIdObj = Optional.ofNullable(dataObj).orElseGet(JSONObject::new).get(deptFieldKey);
            if (ObjectNull.isNull(dataDeptIdObj)) {
                return null;
            }
            // 部门组件的数据可能是多选(数据结构为：数组)，可能是单选(数据结构为：字符串)
            if (dataDeptIdObj instanceof Collection) {
                return Convert.toList(String.class, dataDeptIdObj);
            } else {
                return Collections.singletonList(String.valueOf(dataDeptIdObj));
            }
        };
        List<String> filterScopeDeptIds = getFilterScopeDeptIdFunction.apply(data, roleScopeConditionDeptIdKey);
        SearchUserDto search = new SearchUserDto()
                .setRoleIds(roleIds)
                // 若角色配置了权限范围，则会筛选出权限范围内包括发起人所属部门的用户id
                .setFilterScopeRoleUser(Boolean.TRUE)
                .setFilterScopeDeptIds(filterScopeDeptIds);
        List<UserDto> userDtos = authUserServiceApi.userSearch(search).getData();
        if (ObjectNull.isNull(userDtos)) {
            return Collections.emptyList();
        }
        return BeanCopyUtil.copys(userDtos, FlowApprovalUserDTO.class);
    }

    /**
     * 发起人
     *
     * @param runtimeData 运行时信息
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getSelf(RuntimeData runtimeData) {
        return Collections.singletonList(BeanCopyUtil.copy(new UserDto()
                .setId(runtimeData.getFlowTask().getCreateById())
                .setRealName(runtimeData.getFlowTask().getCreateBy()), FlowApprovalUserDTO.class));
    }

    /**
     * 获取主管集合
     *
     * @param leader 审批主管配置
     * @param runtimeData 运行时参数
     * @param node 节点
     * @param assignLevel true-获取指定级别主管，false-获取连续多级部门主管
     * @return 主管集合
     */
    private List<FlowApprovalUserDTO> getLeaderUsers(Leader leader, Integer leaderLevel, Node node, RuntimeData runtimeData, Boolean assignLevel) {
        List<String> userIds = new ArrayList<>();
        switch (leader.getLeaderSource()) {
            case FLOW_NODE:
                userIds = getPersonnelFlowNodeUserIds(leader.getFlowNodeConfig(), node, runtimeData.getFlowTask());
                break;
            case USER_FIELD:
                // 没有数据时，需要查询数据（批量审批没有传递数据）
                initData(runtimeData);
                userIds = getPersonnelFieldUserIds(leader.getUserFieldConfig(), node, runtimeData.getData());
                break;
            case SEND_USER:
            default:
                userIds.add(runtimeData.getFlowTask().getCreateById());
                break;
        }
        if (ObjectNull.isNull(userIds)) {
            return Collections.emptyList();
        }
        // 查询用户
        List<UserDto> userList = authUserServiceApi.getByIds(userIds).getData();
        if (ObjectUtil.isNull(userList)) {
            throw new BusinessException("未找到发起人的信息");
        }
        // 筛选有部门的用户，没有部门的用户一定找不到主管，所以需要排除
        userList = userList.stream().filter(user -> ObjectNull.isNotNull(user.getDept())).collect(Collectors.toList());
        if (ObjectUtil.isNull(userList)) {
            return Collections.emptyList();
        }
        // 根据”发起人的几级主管“，获取相应的主管信息
        List<SysDeptDto> sysDeptDtoList = authDeptServiceApi.getAll().getData();
        if (CollectionUtils.isEmpty(sysDeptDtoList)) {
            return Collections.emptyList();
        }

        // 查找上级部门层级 Map<层级, 同层部门集合>
        Map<Integer, Set<SysDeptDto>> parentLevelDeptMap = new HashMap<>();
        // 找每个用户的主管
        userList.forEach(user -> {
            Set<String> deptIds = user.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toSet());
            Map<Integer, Set<SysDeptDto>> currentuserParentLevelDeptMap = findLevelDept(deptIds, sysDeptDtoList, leaderLevel, user);
            currentuserParentLevelDeptMap.forEach((deptLevel, deptList) -> {
                if (parentLevelDeptMap.containsKey(deptLevel)) {
                    parentLevelDeptMap.get(deptLevel).addAll(deptList);
                } else {
                    parentLevelDeptMap.put(deptLevel, deptList);
                }
            });
        });

        // 按部门顺序获取部门主管信息
        return queryLevelDeptLeaderUsers(parentLevelDeptMap, leaderLevel, assignLevel);
    }


    /**
     * 获取主管集合
     *
     * @param leaderLevel 发起人的几级主管
     * @param runtimeData 运行时参数
     * @param assignLevel true-获取指定级别主管，false-获取连续多级部门主管
     * @return 主管集合
     */
    private List<FlowApprovalUserDTO> doGetLeaderUsers(Integer leaderLevel, RuntimeData runtimeData, Boolean assignLevel) {
        // 查询发起人所属部门
        UserDto sendUserDto = authUserServiceApi.getById(runtimeData.getFlowTask().getCreateById()).getData();
        if (ObjectUtil.isNull(sendUserDto)) {
            throw new BusinessException("未找到发起人的信息");
        }
        Set<String> deptIds = sendUserDto.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toSet());
        if (ObjectNull.isNull(deptIds)) {
            return Collections.emptyList();
        }
        // 根据”发起人的几级主管“，获取相应的主管信息
        List<SysDeptDto> sysDeptDtoList = authDeptServiceApi.getAll().getData();
        if (CollectionUtils.isEmpty(sysDeptDtoList)) {
            return Collections.emptyList();
        }
        // 上级部门集合
        Map<Integer, Set<SysDeptDto>> parentLevelDeptMap = findLevelDept(deptIds, sysDeptDtoList, leaderLevel, sendUserDto);
        // 按部门顺序获取部门主管信息
        return queryLevelDeptLeaderUsers(parentLevelDeptMap, leaderLevel, assignLevel);
    }

    /**
     * 查找上级部门层级
     *
     * @param deptIds     部门id集合
     * @param sysDeptDtoList 所有部门集合
     * @param leaderLevel 查找发起人的第N级主管（-1表示查找到最上层部门）
     * @param userDto     登录用户信息
     * @return  Map<层级, 同层部门集合>
     */
    private Map<Integer, Set<SysDeptDto>> findLevelDept(Set<String> deptIds, List<SysDeptDto> sysDeptDtoList, Integer leaderLevel, UserDto userDto) {
        List<SysDeptDto> deptList = sysDeptDtoList.stream()
                .filter(d -> deptIds.contains(d.getId()))
                .collect(Collectors.toList());
        if (ObjectUtil.isNull(deptList)) {
            return Collections.emptyMap();
        }
        // 多部门层级
        List<LinkedList<SysDeptDto>> multipleLevelDeptList = new ArrayList<>();
        deptList.forEach(deptDto -> {
            LinkedList<SysDeptDto> levelDeptList = new LinkedList<>();
            levelDeptList.addLast(deptDto);
            // 循环查找上级部门。true-继续找上级部门，false-结束
            boolean hasParent = true;
            do {
                SysDeptDto finalDeptDto = deptDto;
                String parentId = finalDeptDto.getParentId();
                // true：有上级部门，false：无上级部门
                hasParent = StringUtils.isNotBlank(parentId) && !userDto.getTenantId().equals(parentId);
                if (Boolean.FALSE.equals(hasParent)) {
                    continue;
                }
                deptDto = sysDeptDtoList.stream().filter(d -> d.getId().equals(parentId)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(deptDto)) {
                    levelDeptList.addLast(deptDto);
                }
            } while (ObjectUtil.isNotNull(deptDto) && hasParent);
            multipleLevelDeptList.add(levelDeptList);
        });

        // 筛选部门层级，合并为一个层级路径。 Map<层级, 同层部门集合>
        Map<Integer, Set<SysDeptDto>> levelDeptMap = new TreeMap<>();
        multipleLevelDeptList.forEach(levelDeptList -> {
            for (int i = 0; i < levelDeptList.size(); i++) {
                // 不超过发起人的第N级主管
                if (leaderLevel > 0 && i + 1 > leaderLevel) {
                    break;
                }
                int deptLevel = levelDeptList.size() - i;
                Set<SysDeptDto> levelDeptSet = Optional.ofNullable(levelDeptMap.get(deptLevel)).orElseGet(HashSet::new);
                levelDeptSet.add(levelDeptList.get(i));
                levelDeptMap.put(deptLevel, levelDeptSet);
            }
        });
        return levelDeptMap;
    }

    /**
     * 查询主管集合
     *
     * @param parentLevelDeptMap 上级部门集合
     * @param leaderLevel 发起人的几级主管
     * @param assignLevel true-获取指定级别主管，false-获取连续多级部门主管
     * @return 主管集合
     */
    private List<FlowApprovalUserDTO> queryLevelDeptLeaderUsers(Map<Integer, Set<SysDeptDto>> parentLevelDeptMap, Integer leaderLevel, Boolean assignLevel) {
        List<String> leaderIds = parentLevelDeptMap.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .map(SysDeptDto::getLeaderId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (ObjectNull.isNull(leaderIds)) {
            return Collections.emptyList();
        }
        List<UserDto> userDtos = authUserServiceApi.getByIds(leaderIds).getData();
        if (CollectionUtils.isEmpty(userDtos)) {
            return Collections.emptyList();
        }
        // 按部门层级顺序排序
        List<FlowApprovalUserDTO> sortUserDto = new ArrayList<>();
        BiConsumer<Integer, Set<SysDeptDto>> deptLeaderConsumer = (level, levelDeptSet) -> {
            Set<String> deptLeaderIds = levelDeptSet.stream().map(SysDeptDto::getLeaderId).collect(Collectors.toSet());
            if (ObjectNull.isNotNull(deptLeaderIds)) {
                List<FlowApprovalUserDTO> currentLevelUserList = userDtos.stream()
                        .filter(u -> deptLeaderIds.contains(u.getId()))
                        .map(u -> BeanCopyUtil.copy(u, FlowApprovalUserDTO.class)
                                .setApprovalSequence(level))
                        .collect(Collectors.toList());
                sortUserDto.addAll(currentLevelUserList);
            }
        };
        if (assignLevel) {
            // 获取指定级别主管
            List<Integer> deptLevels = parentLevelDeptMap.keySet()
                    .stream()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            Integer leaderDeptLevel = null;
            for (int i = 1; i <= deptLevels.size(); i++) {
                if (i == leaderLevel) {
                    leaderDeptLevel = deptLevels.get(i - 1);
                    break;
                }
            }
            if (ObjectNull.isNotNull(leaderDeptLevel)) {
                deptLeaderConsumer.accept(leaderDeptLevel, parentLevelDeptMap.get(leaderDeptLevel));
            }
        } else {
            // 获取连续多级部门主管
            List<Integer> deptLevels = parentLevelDeptMap.keySet()
                    .stream()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            for (int i = 0; i < deptLevels.size(); i++) {
                Integer leaderDeptLevel = deptLevels.get(i);
                deptLeaderConsumer.accept(i + 1, parentLevelDeptMap.get(leaderDeptLevel));
            }
        }
        return sortUserDto;
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
     *
     * @param node 节点
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getPersonnelJobUsers(Node node) {
        // 查询岗位对应的所有人
        List<String> jobIds = node.getProps().getTargetObj().getPersonnelIdByType(TargetObjectTypeEnum.job);
        if (ObjectNull.isNull(jobIds)) {
            throw new BusinessException("节点未设置审批人", node.getName());
        }
        SearchUserDto search = new SearchUserDto();
        search.setJobIds(jobIds);

        List<UserDto> userDtos = authUserServiceApi.userSearch(search).getData();
        if (ObjectNull.isNull(userDtos)) {
            return Collections.emptyList();
        }
        return BeanCopyUtil.copys(userDtos, FlowApprovalUserDTO.class);
    }


    /**
     * 查询成员字段对应的人员
     *
     * @param node 节点
     * @param data 数据
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getPersonnelFieldUsers(Node node, JSONObject data) {
        List<String> userIds = getPersonnelFieldUserIds(node.getProps().getTargetObj(), node, data);
        if (ObjectNull.isNull(userIds)) {
            return Collections.emptyList();
        }
        SearchUserDto search = new SearchUserDto();
        search.setUserIds(userIds);
        Map<String, UserDto> userMap = authUserServiceApi.userSearch(search).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return userIds.stream()
                .map(userMap::get)
                .map(u -> BeanCopyUtil.copy(u, FlowApprovalUserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 查询成员字段对应的人员id集合
     *
     * @param target 审批人配置
     * @param node  节点
     * @param data 数据
     * @return 用户id集合集合
     */
    private List<String> getPersonnelFieldUserIds(Target target, Node node, JSONObject data) {
        if (ObjectNull.isNull(data)) {
            return Collections.emptyList();
        }
        // 查询成员字段对应的配置
        List<PersonnelDto> personnelList = target.getPersonnelByType(TargetObjectTypeEnum.user_field);
        if (CollectionUtils.isEmpty(personnelList)) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        // 支持用户组件
        List<String> userFieldKeys = personnelList.stream().filter(p -> PersonnelTypeEnum.user.equals(p.getType())).map(PersonnelDto::getId).collect(Collectors.toList());
        if (ObjectNull.isNull(userFieldKeys)) {
            return Collections.emptyList();
        }
        return userFieldKeys.stream().map(data::get).filter(ObjectNull::isNotNull)
                .flatMap(uidObj -> {
                    // 用户组件的数据可能是多选(数据结构为：数组)，可能是单选(数据结构为：字符串)
                    if (uidObj instanceof Collection) {
                        return Convert.toList(String.class, uidObj).stream();
                    } else {
                        return Stream.of((String) uidObj);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询部门字段对应的人员
     *
     * <p>
     * 只返回部门主管
     *
     * @param node 节点
     * @param data 数据
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getPersonnelFieldDeptUsers(Node node, JSONObject data) {
        if (ObjectNull.isNull(data)) {
            return Collections.emptyList();
        }
        // 查询部门字段对应的配置
        List<PersonnelDto> personnelList = node.getProps().getTargetObj().getPersonnelByType(TargetObjectTypeEnum.dept_field);
        if (CollectionUtils.isEmpty(personnelList)) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        // 支持部门组件
        List<String> deptFieldKeys = personnelList.stream().filter(p -> PersonnelTypeEnum.dept.equals(p.getType())).map(PersonnelDto::getId).collect(Collectors.toList());
        if (ObjectNull.isNull(deptFieldKeys)) {
            return Collections.emptyList();
        }
        List<String> deptIds =  deptFieldKeys.stream().map(data::get).filter(ObjectNull::isNotNull)
                .flatMap(deptIdObj -> {
                    // 部门组件的数据可能是多选(数据结构为：数组)，可能是单选(数据结构为：字符串)
                    if (deptIdObj instanceof Collection) {
                        return Convert.toList(String.class, deptIdObj).stream();
                    } else {
                        return Stream.of((String) deptIdObj);
                    }
                })
                .collect(Collectors.toList());
        if (ObjectNull.isNull(deptIds)) {
            return Collections.emptyList();
        }
        // 查询部门主管
        return getDeptLeader(deptIds);
    }


    /**
     * 查询指定流程节点最终审批人id集合
     *
     * @param flowNodeConfig 主管来源——流程节点配置
     * @param node 节点
     * @return 指定流程节点最终审批人id集合
     */
    private List<String> getPersonnelFlowNodeUserIds(LeaderFlowNodeSource flowNodeConfig, Node node, FlowTask flowTask) {
        if (ObjectNull.isNull(flowNodeConfig.getNodeIds())) {
            throw new BusinessException("未完成审批人节点设置", node.getProps().getType().getDesc());
        }
        List<List<Node>> nodePaths = flowTaskPathService.getNodePaths(flowTask);
        List<CourseDto> finalCourseList = FlowUtil.extractFinalTaskCourse(flowTask, nodePaths);
        if (ObjectNull.isNull(finalCourseList)) {
            return Collections.emptyList();
        }
        return finalCourseList.stream()
                // 筛选指定节点的审批记录
                .filter(course -> flowNodeConfig.getNodeIds().contains(course.getNodeId()))
                .filter(course -> ObjectNull.isNotNull(course.getApproveResultDtos()))
                .flatMap(course ->
                        course.getApproveResultDtos()
                                .stream()
                                // 筛选同意、拒绝操作的审批人
                                .filter(approve -> NodeOperationTypeEnum.PASS.equals(approve.getNodeOperationTypeEnum())
                                        || NodeOperationTypeEnum.REFUSE.equals(approve.getNodeOperationTypeEnum()))
                                .map(ApproveResultDto::getUserId)
                )
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取指定部门人员
     *
     * <p>>
     * 默认只获取指定部门的主管
     *
     * @param node 节点
     * @return 用户集合
     */
    private List<FlowApprovalUserDTO> getDeptUsers(Node node) {
        // 查询所有指定部门的主管
        List<String> deptIds = node.getProps().getTargetObj().getPersonnelIdByType(TargetObjectTypeEnum.dept);
        if (ObjectNull.isNull(deptIds)) {
            throw new BusinessException("节点未设置审批人", node.getName());
        }
        // 根据部门id集合查询部门
        return getDeptLeader(deptIds);
    }

    /**
     * 查询部门主管
     *
     * @param deptIds 部门id集合
     * @return 部门主管
     */
    private List<FlowApprovalUserDTO> getDeptLeader(List<String> deptIds) {
        SearchUserDto search = new SearchUserDto();
        search.setDeptLeaderIds(deptIds);

        List<UserDto> userDtos = authUserServiceApi.userSearch(search).getData();
        if (ObjectNull.isNull(userDtos)) {
            return Collections.emptyList();
        }
        return BeanCopyUtil.copys(userDtos, FlowApprovalUserDTO.class);
    }
}
