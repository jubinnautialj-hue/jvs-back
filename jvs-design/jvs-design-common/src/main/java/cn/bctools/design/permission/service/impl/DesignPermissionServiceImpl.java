package cn.bctools.design.permission.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.dto.PermissionEndpoint;
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
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.WebUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Design permission service.
 *
 * @author zhuxiaokang 权限服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class DesignPermissionServiceImpl implements DesignPermissionService {

    private final JvsAppService appService;
    private final DataModelService dataModelService;
    private final PermissionGroupService permissionGroupService;
    private final PermissionSettingService permissionSettingService;
    private final AppMenuService appMenuService;

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

    /**
     * 一个标识对应着多个不同的接口
     *
     * @param identification the identification
     * @return
     */
    @Override
    public Boolean getOperationPermissions(Identification identification) {
        JvsApp jvsApp = appService.getById(identification.getJvsAppId());
        if (RoleUtils.checkAppDesignPermission(UserCurrentUtils.getUserId(), jvsApp)) {
            return true;
        }
        // 获取权限
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .eq(PermissionSetting::getJvsAppId, identification.getJvsAppId())
                .eq(PermissionSetting::getPermissionType, PermissionType.OPERATION));
        if (ObjectNull.isNull(permissionSettings)) {
            return false;
        }
        // 查询权限组权限成员配置
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);
        List<DesignRole> collect = permissionSettings.stream()
                .filter(p -> PermissionType.OPERATION.equals(p.getPermissionType()))
                .map(p -> {
                    PermissionMemberDto member = groupMemberMap.get(p.getPermissionGroupId());
                    return new DesignRole()
                            .setPersonType(member.getPersonType())
                            .setPersonnels(member.getPersonnels())
                            .setOperation(p.getPermission().getOperation())
                            .setTreeOperation(p.getPermission().getTreeOperation());
                }).collect(Collectors.toList());
        //获取当前用户有哪些资源权限
        HttpServletRequest request = WebUtils.getRequest();
        //查询自定义的权限资源树有哪些并进行过滤
        collect = filterOperation(collect, request.getServletPath(), request.getMethod(), identification.getJvsAppId());
        return RoleUtils.hasPermitOperation(collect);
    }

    /**
     * 调用接口时根据当前用户权限进行过滤
     *
     * @param collect
     * @param servletPath
     * @param method
     * @param appId
     * @return
     */
    private List<DesignRole> filterOperation(List<DesignRole> collect, String servletPath, String method, String appId) {
        //找到匹配的资源数据
        appMenuService.list(new LambdaQueryWrapper<AppMenu>()
                        .isNotNull(AppMenu::getPermission)
                        .eq(AppMenu::getJvsAppId, appId).eq(AppMenu::getDesignType, DesignType.URL))
                .forEach(e -> {
                    List<PermissionEndpoint> urlOperation = e.getPermission().getUrlOperation();
                    if (ObjectNull.isNotNull(urlOperation)) {
                        urlOperation.forEach(a -> {
                            a.getUrl().forEach(b -> {
                                if (b.getType().toLowerCase().equals(method.toLowerCase()) && servletPath.equals(b.getUrl())) {
                                    //删除掉不存在的权限只保留部分资源标识
                                    collect.removeIf(c -> !c.getOperation().contains(a.getPermission()));
                                }
                            });
                        });
                    }
                });
        return collect;
    }

    @Override
    public Map<String, List<DesignRole>> getBatchOperationPermission(List<String> designIds) {
        long totalStart = System.currentTimeMillis();
        log.info("开始批量获取操作权限，设计ID数量: {}", designIds == null ? 0 : designIds.size());

        if (ObjectNull.isNull(designIds)) {
            return Collections.emptyMap();
        }

        // 获取权限
        long queryStart = System.currentTimeMillis();
        List<PermissionSetting> permissionSettings = permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                .in(PermissionSetting::getDesignId, designIds)
                .eq(PermissionSetting::getPermissionType, PermissionType.OPERATION));
        log.info("查询权限设置耗时: {}ms, 返回记录数: {}", System.currentTimeMillis() - queryStart,
                permissionSettings == null ? 0 : permissionSettings.size());

        if (ObjectNull.isNull(permissionSettings)) {
            return Collections.emptyMap();
        }

        // 查询权限组权限成员配置
        long groupStart = System.currentTimeMillis();
        Map<String, PermissionMemberDto> groupMemberMap = getGroupMemberMap(permissionSettings);
        log.info("查询权限组成员耗时: {}ms", System.currentTimeMillis() - groupStart);

        // 操作权限 - 按designId分组优化处理
        long processStart = System.currentTimeMillis();

        // 先按designId分组权限设置，避免重复遍历
        Map<String, List<PermissionSetting>> settingsByDesignId = permissionSettings.stream()
                .filter(p -> PermissionType.OPERATION.equals(p.getPermissionType()))
                .collect(Collectors.groupingBy(PermissionSetting::getDesignId));

        log.info("按designId分组后，需要处理的designId数量: {}", settingsByDesignId.size());

        Map<String, List<DesignRole>> batchPermissionMap = new HashMap<>(designIds.size());

        for (String designId : designIds) {
            long roleStart = System.currentTimeMillis();

            // 直接从分组后的map中获取，避免再次过滤
            List<PermissionSetting> designSettings = settingsByDesignId.getOrDefault(designId, Collections.emptyList());

            List<DesignRole> operationRoles = designSettings.stream()
                    .map(p -> {
                        PermissionMemberDto member = groupMemberMap.get(p.getPermissionGroupId());
                        if (member == null) {
                            return null;
                        }
                        return new DesignRole()
                                .setPersonType(member.getPersonType())
                                .setPersonnels(member.getPersonnels())
                                .setOperation(p.getPermission().getOperation())
                                .setTreeOperation(p.getPermission().getTreeOperation());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            batchPermissionMap.put(designId, operationRoles);

            if (System.currentTimeMillis() - roleStart > 100) { // 单个设计权限处理超过100ms才记录
                log.debug("处理设计ID {} 权限耗时: {}ms", designId, System.currentTimeMillis() - roleStart);
            }
        }

        long processTime = System.currentTimeMillis() - processStart;
        log.info("批量处理权限耗时: {}ms, 平均每个设计ID: {}ms",
                processTime, processTime / designIds.size());

        long totalTime = System.currentTimeMillis() - totalStart;
        log.info("批量获取操作权限总耗时: {}ms", totalTime);

        return batchPermissionMap;
    }

    /**
     * 查询权限组权限成员配置
     *
     * @param permissionSettings 权限配置集合
     * @return 权限组权限成员配置
     */
    private Map<String, PermissionMemberDto> getGroupMemberMap(List<PermissionSetting> permissionSettings) {
        long startTime = System.currentTimeMillis();

        // 去重groupIds，减少查询量
        Set<String> groupIdSet = permissionSettings
                .stream()
                .map(PermissionSetting::getPermissionGroupId)
                .collect(Collectors.toSet());

        log.info("需要查询的权限组数量: {}", groupIdSet.size());

        List<String> groupIds = new ArrayList<>(groupIdSet);
        List<PermissionGroup> groups = permissionGroupService.listByIds(groupIds);

        long costTime = System.currentTimeMillis() - startTime;
        log.info("查询权限组耗时: {}ms, 返回权限组数量: {}", costTime, groups.size());

        return groups.stream()
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
     * @param settingDto         权限设置
     * @param permissionSettings 权限集合
     * @param groupMemberMap     权限组成员
     * @param designId           设计id
     * @param dataModelId        模型id
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
     * @param groupMemberMap     权限组成员
     * @param designId           设计id
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

    @Override
    public void handleDesignDataScope(String modelId) {
        DesignRoleSettingDto dataModelPermission = getDataModelPermission(modelId);
        if(ObjectNull.isNotNull(dataModelPermission)) {
            //获取这个模型的数据权限有哪些
            DynamicDataUtils.handleDesignDataScope(dataModelPermission.getDataModelRole());
        }
    }
}
