package cn.bctools.data.factory.source.util;

import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.source.dto.AuthRole;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.enums.AuthRolePersonType;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.enums.OperationEnum;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单权限过滤
 *
 * @author xiaohui
 */
public class AuthUtil {

    /**
     * 判断是否有权限
     *
     * @param buttonName 需要校验的权限按钮
     * @param chartPage  需要校验的数据
     * @return 当前数据可操作的权限 如果没有权限返回空数组
     */
    public static List<OperationEnum> check(DataSource chartPage, OperationEnum buttonName) {
        //默认权限
        List<OperationEnum> defaultList = Arrays.asList(OperationEnum.values());
        //如果是创建人 拥有所有权限
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (chartPage.getCreateById().equals(user.getId())) {
            return defaultList;
        }
        if (!chartPage.getRoleType()) {
            return defaultList;
        }
        if (chartPage.getRole() == null) {
            return new ArrayList<>();
        }
        List<JSONObject> role = chartPage.getRole();
        List<AuthRole> roles = role.stream().map(e -> e.toJavaObject(AuthRole.class)).collect(Collectors.toList());
        //判断是否有权限
        if (roles.isEmpty()) {
            return defaultList;
        }
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
                    if (buttonName != null && !OperationEnum.查看.equals(buttonName)) {
                        boolean match = e.getOperation().stream().anyMatch(x -> x.equals(buttonName));
                        if (!match) {
                            return false;
                        }
                    }
                    return e.getPersonnels().stream().anyMatch(x -> {
                        switch (x.getType()) {
                            case dept:
                                return user.getDept().stream().map(DeptDto::getDeptId).anyMatch(v->v.equals(x.getId()));
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
                    //如果此条数据有当前用户 但是没有按钮 表示只能查看
                    if (e.getOperation().isEmpty()) {
                        return Arrays.asList(OperationEnum.查看).stream();
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
    public static List<DataSource> auth(List<DataSource> list) {
        return list.stream().peek(AuthUtil::auth)
                .filter(e -> e.getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource) || !e.getOperationList().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 获取单个的权限
     *
     * @param dataSource 单个
     * @return 过滤后的数据
     */
    public static DataSource auth(DataSource dataSource) {
        //判断是否为数据集 数据集不走数据源权限
        if (dataSource.getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource)) {
            return dataSource.setOperationList(new ArrayList<>());
        }
        return dataSource.setOperationList(check(dataSource, null));
    }

}
