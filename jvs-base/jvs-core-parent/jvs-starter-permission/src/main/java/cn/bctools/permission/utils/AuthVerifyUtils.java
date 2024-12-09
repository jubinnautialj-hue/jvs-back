package cn.bctools.permission.utils;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.permission.dto.AuthRole;
import cn.bctools.permission.enums.OperationType;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限校验
 * @author wl
 */
public class AuthVerifyUtils {

    /**
     * 检查权限是否存在
     * @param roleType 权限类型
     * @param authRoles 权限设置
     * @param createById 创建人id
     * @param permission 权限集
     * @param operation 权限
     */
    public static Boolean verify(Boolean roleType,List<AuthRole> authRoles, String createById, List<OperationType> permission, OperationType operation){
       return verify(roleType,authRoles,createById,permission,operation,null);
    }

    /**
     * 检查权限是否存在
     * @param roleType 权限类型
     * @param authRoles 权限设置
     * @param createById 创建人id
     * @param permission 权限集
     * @param operation 权限
     * @param defaultRole 默认权限
     */
    public static Boolean verify(Boolean roleType,List<AuthRole> authRoles, String createById, List<OperationType> permission, OperationType operation,List<OperationType> defaultRole){
        if(!roleType){
            return true;
        }

        //当前客户端分类下的所有权限
        if(CollectionUtil.isEmpty(permission)){
            return Boolean.TRUE;
        }

        if(CollectionUtil.isEmpty(authRoles)){
            return Boolean.TRUE;
        }
        //如果是创建人 拥有所有权限
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (StrUtil.equals(createById,user.getId())) {
            return Boolean.TRUE;
        }

        return authRoles.parallelStream().anyMatch(e -> {
            if (CollectionUtil.isEmpty(e.getPersonnels())) {
                return false;
            }
            if(isIn(e.getPersonnels(),user)){
                boolean b = Optional.ofNullable(e.getOperation())
                        .orElse(new ArrayList<>())
                        .stream()
                        .anyMatch(v -> Objects.equals(v, operation));

                if(!b && CollectionUtil.isNotEmpty(defaultRole)){
                    if(CollectionUtil.isNotEmpty(e.getOperation())){
                        return defaultRole.stream().anyMatch(v -> v.equals(operation));
                    }
                }
                return b;
            }
            return false;
        });
    }

    /**
     * 获取操作权限
     * @param roleType 权限类型
     * @param roles 权限设置
     * @param createById 创建人id
     * @param permission 权限集
     * @return 拥有的权限
     */
    public static List<OperationType> getOperationList(Boolean roleType,List<AuthRole> roles,String createById, List<OperationType> permission){
        return getOperationList(roleType,roles,createById,permission,null);
    }

    /**
     * 获取操作权限
     * @param roleType 权限类型
     * @param roles 权限设置
     * @param createById 创建人id
     * @param permission 权限集
     * @param defaultList 默认权限
     * @return 拥有的权限
     */
    public static List<OperationType> getOperationList(Boolean roleType,List<AuthRole> roles,String createById, List<OperationType> permission,List<OperationType> defaultList){
        //当前客户端分类下的所有权限
        if(!roleType){
            return permission;
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        if(StrUtil.equals(createById,user.getId())) {
            return permission;
        }
        if(CollectionUtil.isEmpty(roles)){
            return new ArrayList<>();
        }
        return roles.parallelStream().filter(e -> {
            if (CollectionUtil.isEmpty(e.getPersonnels())) {
                return false;
            }
            return isIn(e.getPersonnels(),user);
        }).peek(e -> {
            //权限不为空时填入默认权限，列如 当只设置了编辑权限，且查看权限没有选时，此时需要默认装填进去
            if(CollectionUtil.isNotEmpty(defaultList) && CollectionUtil.isNotEmpty(e.getOperation())){
                e.getOperation().addAll(defaultList);
            }
        }).map(AuthRole::getOperation).flatMap(Collection::parallelStream).distinct().collect(Collectors.toList());
    }

    private static Boolean isIn(List<PersonnelDto> personnels,UserDto user){
        List<String> roleIds = Optional.ofNullable(user.getRoleIds()).orElse(new ArrayList<>());
       return Optional.ofNullable(personnels).orElse(new ArrayList<>())
                .stream()
                .anyMatch(v -> {
                            switch (v.getType()) {
                                case job:
                                    return StrUtil.equals(user.getJobId(), v.getId());
                                case role:
                                    return roleIds.contains(v.getId());
                                case user:
                                    return StrUtil.equals(user.getId(), v.getId());
                                case dept:
                                    return StrUtil.equals(user.getDeptId(), v.getId());
                                default:
                                    return false;
                            }
                        }
                );
    }

}
