package cn.bctools.design.data.util;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
public class RoleUtils {

    /**
     * 根据设计权限添加是否有功能判断返回
     *
     * @param roles 权限信息集合
     * @return 校验结果
     */
    public static boolean hasPermitJsonObject(List<JSONObject> roles) {
        List<DesignRole> designRoles = JSONArray.parseArray(JSONObject.toJSONString(roles), DesignRole.class);
        return RoleUtils.hasPermit(designRoles);
    }

    public static boolean hasPermit(List<DesignRole> roles) {
        if (ObjectUtils.isEmpty(roles)) {
            // 未配置权限, 默认不放行
            return false;
        }
        //判断角色，判断人员
        for (DesignRole role : roles) {
            boolean check = hasPermit(role);
            if (check) {
                return true;
            }
        }
        return false;
    }

    /**
     * 权限操作校验
     * <p>
     * 会用到当前登录用户的信息
     *
     * @param designRole 权限范围
     * @return 是否有权限
     */
    public static boolean hasPermit(DesignRole designRole) {
        //暂时先跳过，因为权限使用问题。 不是每一个表单都会有权限的。 会导致每一次都创建一个新的权限,需要建立串链关系 ，让权限复用，把权限独立出来
        if (Objects.isNull(designRole)) {
            return false;
        }
        if (ObjectNull.isNull(designRole.getPersonType())) {
            designRole.setPersonType(PersonnelTypeEnum.all);
        }
        if (designRole.getPersonType().equals(PersonnelTypeEnum.all)) {
            return true;
        }
        if (designRole.getPersonType().equals(PersonnelTypeEnum.custom)) {
            List<PersonnelDto> custom = designRole.getPersonnels();
            return checkPersonnels(custom);
        }
        List<PersonnelDto> personnels = designRole.getPersonnels();
        return checkPersonnels(personnels);
    }

    public static boolean hasDataPermit(DesignRole designRole) {
        //暂时先跳过，因为权限使用问题。 不是每一个表单都会有权限的。 会导致每一次都创建一个新的权限,需要建立串链关系 ，让权限复用，把权限独立出来
        if (Objects.isNull(designRole)) {
            return false;
        }
        if (ObjectNull.isNotNull(designRole.getPersonType())) {
            if (designRole.getPersonType().equals(PersonnelTypeEnum.all)) {
                return true;
            }
            if (designRole.getPersonType().equals(PersonnelTypeEnum.custom)) {
                List<PersonnelDto> custom = designRole.getPersonnels();
                return checkPersonnels(custom);
            }
        }
        List<PersonnelDto> personnels = designRole.getPersonnels();
        return checkPersonnels(personnels);
    }

    public static boolean checkPersonnels(List<PersonnelDto> personnels) {
        UserInfoDto<? extends UserDto> userInfo = UserCurrentUtils.init();
        UserDto userDto = userInfo.getUserDto();
        String userId = userDto.getId();
        Set<String> deptId = userDto.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toSet());
        List<String> roleIds = userInfo.getRoles();
        if (ObjectUtils.isEmpty(personnels)) {
            return false;
        }
        for (PersonnelDto personnel : personnels) {
            String id = personnel.getId();
            PersonnelTypeEnum personnelType = personnel.getType();
            if (StringUtils.isBlank(id) || Objects.isNull(personnelType)) {
                continue;
            }
            boolean match = false;
            switch (personnelType) {
                case user:
                    match = id.equals(userId);
                    break;
                case dept:
                    match = deptId.contains(id);
                    break;
                case role:
                    match = ObjectUtils.isNotEmpty(roleIds) && roleIds.contains(id);
                    break;
                default:
                    break;
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取允许的操作权限
     *
     * @param designRole 权限信息
     * @return 操作集合
     */
    public static Set<String> getPermitOperation(DesignRole designRole) {
        if (Objects.isNull(designRole)) {
            return Collections.emptySet();
        }
        List<String> operation = new ArrayList<>();
        if(ObjectNull.isNotNull(designRole.getOperation())){
            operation.addAll(designRole.getOperation());
        }
        if(ObjectNull.isNotNull(designRole.getTreeOperation())) {
            operation.addAll(designRole.getTreeOperation());
        }
        if (ObjectUtils.isEmpty(operation) || !hasPermit(designRole)) {
            return Collections.emptySet();
        }
        return new HashSet<>(operation);
    }

    /**
     * 获取允许的操作权限
     *
     * @param designRoles 权限信息集合
     * @return 操作集合
     */
    public static Set<String> getPermitOperation(List<DesignRole> designRoles, List<ButtonDesignHtml> buttons) {
        if (ObjectUtils.isEmpty(designRoles)) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (DesignRole designRole : designRoles) {
            // 未启用轻应用版本功能，且是"所有人"返回全部操作权限
            if (Boolean.FALSE.equals(CurrentAppUtils.getAppEnableVersionFeature()) && designRole.getPersonType().equals(PersonnelTypeEnum.all)) {
                return buttons.stream().map(ButtonSettingDto::getName).collect(Collectors.toSet());
            }
            set.addAll(getPermitOperation(designRole));
        }
        return set;
    }

    /**
     * 获取所有操作权限
     *
     * @param designRole 权限信息
     * @return 操作集合
     */
    public static Set<String> getOperation(DesignRole designRole) {
        if (Objects.isNull(designRole)) {
            return Collections.emptySet();
        }
        List<String> operation = designRole.getOperation();
        if (ObjectUtils.isEmpty(operation)) {
            return Collections.emptySet();
        }
        return new HashSet<>(operation);
    }

    /**
     * 获取所有操作权限
     *
     * @param designRoles 权限信息集合
     * @return 操作集合
     */
    public static Set<String> getOperation(List<DesignRole> designRoles) {
        if (ObjectUtils.isEmpty(designRoles)) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (DesignRole designRole : designRoles) {
            set.addAll(getOperation(designRole));
        }
        return set;
    }

    /**
     * 过滤当前用户匹配的权限配置
     *
     * @param designRoleList 设计套件权限组
     * @return 当前用户匹配的配置
     */
    public static List<DesignRole> filterDesignRole(List<DesignRole> designRoleList) {
        if (ObjectUtils.isEmpty(designRoleList)) {
            return Collections.emptyList();
        }
        return designRoleList.stream().filter(RoleUtils::hasPermit).collect(Collectors.toList());
    }

}
