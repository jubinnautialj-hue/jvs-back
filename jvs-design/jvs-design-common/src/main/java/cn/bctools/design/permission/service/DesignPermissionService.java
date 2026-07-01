package cn.bctools.design.permission.service;

import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.permission.dto.PermissionSettingDto;
import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;

import java.util.List;
import java.util.Map;

/**
 * The interface Design permission service.
 *
 * @author zhuxiaokang  权限服务
 */
public interface DesignPermissionService {

    /**
     * 保存权限
     *
     * @param appId       应用id
     * @param groupId     权限组id
     * @param member      权限成员
     * @param permissions 权限
     */
    void savePermission(String appId, String groupId, PermissionMemberDto member, List<PermissionSettingDto> permissions);

    /**
     * 获取权限
     *
     * @param jvsAppId    应用id
     * @param designId    设计id
     * @param dataModelId 模型id
     * @return 包含作权限和数据权限 design permission
     */
    DesignRoleSettingDto getDesignPermission(String jvsAppId, String designId, String dataModelId);

    /**
     * 获取模型数据权限
     *
     * @param dataModelId 模型id
     * @return 模型数据权限 data model permission
     */
    DesignRoleSettingDto getDataModelPermission(String dataModelId);


    /**
     * 获取数据权限
     *
     * @param designId    设计id
     * @param dataModelId 模型id
     * @return 包括设计的自定义数据权限和模型的设计权限 data model permission
     */
    DesignRoleSettingDto getDataModelPermission(String designId, String dataModelId);


    /**
     * 获取指定设计的操作权限
     *
     * @param designId 设计id
     * @return 指定设计的操作权限集合 operation permissions
     */
    List<DesignRole> getOperationPermissions(String designId);

    /**
     * 批量获取设计的操作权限
     *
     * @param identification the identification
     * @return the operation permissions
     */
    Boolean getOperationPermissions(Identification identification);

    /**
     * 批量获取设计的操作权限
     *
     * @param designIds 设计id集合
     * @return Map<设计id, 权限集合> batch operation permission
     */
    Map<String, List<DesignRole>> getBatchOperationPermission(List<String> designIds);

    /**
     * 根据请求类型判断是否需要加载权限
     *
     * @param modelId 模型 id
     */
    void handleDesignDataScope(String modelId);
}
