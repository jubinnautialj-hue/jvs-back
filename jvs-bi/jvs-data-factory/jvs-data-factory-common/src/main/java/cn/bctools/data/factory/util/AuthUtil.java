package cn.bctools.data.factory.util;

import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.dto.AuthRole;
import cn.bctools.data.factory.entity.RolePo;
import cn.bctools.data.factory.enums.AuthRolePersonType;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 菜单权限过滤
 *
 * @author xiaohui
 */
@Component
public class AuthUtil<T extends Enum, E extends RolePo> {

    /**
     * 判断是否有权限
     *
     * @param buttonName 需要校验的权限按钮
     * @param role       权限
     * @param createById 创建人
     * @param roleType   权限类型 是否为自定义权限
     * @return 当前数据可操作的权限 如果没有权限返回空数组
     */
    public List<T> check(T buttonName, String createById, List<JSONObject> role, List<T> defaultList, Boolean roleType) {
        //如果是创建人 拥有所有权限
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (createById.equals(user.getId())) {
            return defaultList;
        }
        //公开权限
        if (!roleType) {
            return defaultList;
        }
        if (ObjectUtil.isNull(role)) {
            return new ArrayList<>();
        }
        List<AuthRole<T>> roles = role.stream()
                .map(e -> {
                    Class aClass = defaultList.get(0).getClass();
                    List operation = JSONArray.parseArray(e.getJSONArray("operation").toJSONString(), aClass);
                    return (AuthRole<T>) e.toJavaObject(AuthRole.class).setOperation(operation);
                }).collect(Collectors.toList());
        List<String> roleList = UserCurrentUtils.init().getRoles();
        //获取当前用户的操作权限
        return roles.stream().filter(e -> {
                    if (e.getPersonType().equals(AuthRolePersonType.all)) {
                        return true;
                    }
                    if (e.getPersonnels().isEmpty()) {
                        return false;
                    }
                    //如果操作按钮有值需要判断是否存在此按钮 如果按钮没有值 只需要判断选择的用户中 是否存在当前用户  没有值 就表示 是查看权限
                    if (buttonName != null && !"查看".equals(buttonName.name())) {
                        boolean match = e.getOperation().stream().anyMatch(x -> x.equals(buttonName));
                        if (!match) {
                            return false;
                        }
                    }
                    return e.getPersonnels().stream().anyMatch(x -> {
                        switch (x.getType()) {
                            case dept:
                                return Optional.ofNullable(user.getDept()).orElse(new ArrayList<>()).stream().map(DeptDto::getDeptId).anyMatch(v->v.equals(x.getId()));
                            case role:
                                return roleList.contains(x.getId());
                            case user:
                                return x.getId().equals(user.getId());
                            default:
                                return false;
                        }
                    });
                })
                .flatMap(e -> {
                    if (e.getPersonType().equals(AuthRolePersonType.all)) {
                        return defaultList.stream();
                    }
                    return e.getOperation().stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 过滤当前用户没有权限的数据
     *
     * @param list 需要过滤的数据
     * @return 过滤后的数据
     */
    public List<E> auth(List<E> list, T buttonName, List<T> defaultList) {
        return list.stream().peek(e -> auth(e, buttonName, defaultList))
                .filter(e -> !e.getOperationList().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 获取单个的权限
     *
     * @param dto 单个
     * @return 过滤后的数据
     */
    public E auth(E dto, T buttonName, List<T> defaultList) {
        List<T> check = check(buttonName, dto.getCreateById(), dto.getRole(), defaultList, dto.getRoleType());
        JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(check));
        dto.setOperationList(jsonArray);
        return dto;
    }
}
