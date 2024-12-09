package cn.bctools.design.permission.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.permission.dto.PermissionSettingDto;
import cn.bctools.design.permission.entity.PermissionGroup;
import cn.bctools.design.permission.entity.PermissionSetting;
import cn.bctools.design.permission.entity.dto.PermissionDto;
import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import cn.bctools.design.permission.entity.enums.PermissionType;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionGroupService;
import cn.bctools.design.permission.service.PermissionSettingService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 权限服务
 */
@Service
@AllArgsConstructor
public class DesignPermissionServiceImpl implements DesignPermissionService {

    private final JvsAppService appService;
    private final DataModelService dataModelService;
    private final PermissionGroupService permissionGroupService;
    private final PermissionSettingService permissionSettingService;

    @Override
    public void savePermission(String appId, String groupId, PermissionMemberDto member, List<PermissionSettingDto> permissions) {
        // 设置权限成员
        PermissionGroup permissionGroup = Optional.ofNullable(permissionGroupService.getById(groupId)).orElseThrow(() -> new BusinessException("权限组不存在"));
        permissionGroup.setMember(member);
        permissionGroupService.updateById(permissionGroup);

        // 设置权限
        List<PermissionSetting> permissionSettings = Optional.ofNullable(permissions).orElseGet(ArrayList::new)
                .stream()
                .filter(p -> ObjectNull.isNotNull(p.getDesignId()))
                .map(p ->
                        new PermissionSetting()
                                .setPermissionGroupId(groupId)
                                .setPermissionType(permissionGroup.getPermissionType())
                                .setJvsAppId(appId)
                                .setDesignId(p.getDesignId())
                                .setPermission(p.getPermission()))
                .collect(Collectors.toList());
        // 先删除旧配置，再添加新配置
        permissionSettingService.removeByGroup(groupId);
        if (ObjectNull.isNotNull(permissionSettings)) {
            permissionSettingService.saveBatch(permissionSettings);
        }
    }

    @Override
    public DesignRoleSettingDto getDesignPermission(String jvsAppId, String designId, String dataModelId) {
        DesignRoleSettingDto settingDto = new DesignRoleSettingDto();
        // 应用信息
        JvsApp jvsApp = appService.getById(jvsAppId);
        settingDto.setJvsAppId(jvsAppId);
        settingDto.setJvsAppName(jvsApp.getName());
        settingDto.setModeId(dataModelId);
        settingDto.setJvsAppCreateById(jvsApp.getCreateById());

        // 获取权限
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .in(PermissionSetting::getDesignId, Arrays.asList(designId, dataModelId)));
        if (ObjectNull.isNull(permissionSettings)) {
            return settingDto;
        }
        // 查询权限组权限成员配置
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);

        // 操作权限
        List<DesignRole> operationRoles = getOperationDesignRoles(permissionSettings, groupMemberMap, designId);
        settingDto.setRole(operationRoles);

        // 数据权限
        getDataPermission(settingDto, permissionSettings, groupMemberMap, designId, dataModelId);
        return settingDto;
    }

    @Override
    public DesignRoleSettingDto getDataModelPermission(String dataModelId) {
        return getDataModelPermission(null, dataModelId);
    }

    @Override
    public DesignRoleSettingDto getDataModelPermission(String designId, String dataModelId) {
        if (ObjectNull.isNull(dataModelId)) {
            return null;
        }
        // 获取权限
        List<String> designIds = new ArrayList<>();
        designIds.add(dataModelId);
        if (ObjectNull.isNotNull(designId)) {
            designIds.add(designId);
        }
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .in(PermissionSetting::getDesignId, designIds)
                .eq(PermissionSetting::getPermissionType, PermissionType.DATA));
        if (ObjectNull.isNull(permissionSettings)) {
            return null;
        }
        // 查询权限组权限成员配置
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);
        DesignRoleSettingDto settingDto = new DesignRoleSettingDto();
        DynamicDataUtils.setDto(settingDto);
        getDataPermission(settingDto, permissionSettings, groupMemberMap, designId, dataModelId);
        return settingDto;
    }

    @Override
    public List<DesignRole> getOperationPermissions(String designId) {
        // 获取权限
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .eq(PermissionSetting::getDesignId, designId)
                .eq(PermissionSetting::getPermissionType, PermissionType.OPERATION));
        if (ObjectNull.isNull(permissionSettings)) {
            return null;
        }
        // 查询权限组权限成员配置
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);
        return getOperationDesignRoles(permissionSettings, groupMemberMap, designId);
    }

    @Override
    public Map<String, List<DesignRole>> getBatchOperationPermission(List<String> designIds) {
        if (ObjectNull.isNull(designIds)) {
            return Collections.emptyMap();
        }
        // 获取权限
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .in(PermissionSetting::getDesignId, designIds)
                .eq(PermissionSetting::getPermissionType, PermissionType.OPERATION));
        if (ObjectNull.isNull(permissionSettings)) {
            return Collections.emptyMap();
        }
        // 查询权限组权限成员配置
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);

        // 操作权限
        Map<String, List<DesignRole>> batchPermissionMap = new HashMap<>(designIds.size());
        designIds.forEach(designId -> {
            List<DesignRole> operationRoles = getOperationDesignRoles(permissionSettings, groupMemberMap, designId);
            batchPermissionMap.put(designId, operationRoles);
        });
        return batchPermissionMap;
    }

    /**
     * 查询权限组权限成员配置
     *
     * @param permissionSettings 权限配置集合
     * @return 权限组权限成员配置
     */
    private Map<String, PermissionMemberDto> getGroupMemberMap(List<PermissionSetting> permissionSettings) {
        List<String> groupIds = permissionSettings
                .stream()
                .map(PermissionSetting::getPermissionGroupId)
                .collect(Collectors.toList());
        return permissionGroupService.listByIds(groupIds)
                .stream()
                .collect(Collectors.toMap(PermissionGroup::getId, p -> Optional.ofNullable(p.getMember()).orElseGet(PermissionMemberDto::new)));
    }

    /**
     * 获取指定设计的数据权限
     *
     * @param permissionSettings 权限集合
     * @param groupMemberMap     权限组成员
     * @param designId           设计id
     * @return 指定设计的数据权限
     */
    private List<DesignRole> getDataRole(List<PermissionSetting> permissionSettings, Map<String, PermissionMemberDto> groupMemberMap, String designId) {
        return permissionSettings.stream()
                .filter(p -> PermissionType.DATA.equals(p.getPermissionType()) && p.getDesignId().equals(designId))
                .map(p -> {
                    PermissionDto permission = p.getPermission();
                    PermissionMemberDto member = groupMemberMap.get(p.getPermissionGroupId());
                    return new DesignRole()
                            .setPersonType(member.getPersonType())
                            .setPersonnels(member.getPersonnels())
                            .setScopeList(permission.getScopeList())
                            .setConditionList(permission.getConditionList());
                }).collect(Collectors.toList());
    }


    /**
     * 得到数据权限
     *
     * @param settingDto 权限设置
     * @param permissionSettings 权限集合
     * @param groupMemberMap 权限组成员
     * @param designId 设计id
     * @param dataModelId 模型id
     */
    private void getDataPermission(DesignRoleSettingDto settingDto, List<PermissionSetting> permissionSettings, Map<String, PermissionMemberDto> groupMemberMap, String designId, String dataModelId) {
        // 数据权限
        List<DesignRole> dataRoles = null;
        // 设计id不为空时获取设计的数据权限
        if (ObjectNull.isNotNull(designId)) {
            // 先得到设计的自定义数据权限配置
            dataRoles = getDataRole(permissionSettings, groupMemberMap, designId);
            // 没有自定义的数据权限，则使用模型的数据权限
            if (ObjectNull.isNull(dataRoles)) {
                dataRoles.addAll(getDataRole(permissionSettings, groupMemberMap, dataModelId));
            }
        } else {
            // 设计id不存在，则直接获取模型的数据权限配置
            dataRoles = getDataRole(permissionSettings, groupMemberMap, dataModelId);
        }
        settingDto.setDataModelRole(dataRoles);

        // 设置是否开启工作流
        DataModelPo dataModelPo = dataModelService.getById(dataModelId);
        settingDto.setEnableWorkflow(dataModelPo.getEnableWorkflow());
    }

    /**
     * 获取设计的操作权限
     *
     * @param permissionSettings 权限集合
     * @param groupMemberMap 权限组成员
     * @param designId 设计id
     * @return 设计的操作权限
     */
    private List<DesignRole> getOperationDesignRoles(List<PermissionSetting> permissionSettings, Map<String, PermissionMemberDto> groupMemberMap, String designId) {
        return permissionSettings.stream()
                .filter(p -> PermissionType.OPERATION.equals(p.getPermissionType()) && p.getDesignId().equals(designId))
                .map(p -> {
                    PermissionMemberDto member = groupMemberMap.get(p.getPermissionGroupId());
                    return new DesignRole()
                            .setPersonType(member.getPersonType())
                            .setPersonnels(member.getPersonnels())
                            .setOperation(p.getPermission().getOperation())
                            .setTreeOperation(p.getPermission().getTreeOperation());
                }).collect(Collectors.toList());
    }

}
