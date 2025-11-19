package cn.bctools.design.data.fields.dto.enums;

import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.enums.DeptEnum;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum Data condition type.
 *
 * @author guojing
 */
@Slf4j
@ApiModel("数据权限自定义表单内容条件")
@Getter
@AllArgsConstructor
public enum DataConditionType {
    /**
     * 当前登录用户 id data condition type.
     */
    当前登录用户ID,
    /**
     * 当前登录用户名 data condition type.
     */
    当前登录用户名,
    /**
     * 当前登录用户所在部门 data condition type.
     */
    当前登录用户所在部门,
    /**
     * 当前登录用户所在部门及以下 data condition type.
     */
    当前登录用户所在部门及以下,
    /**
     * 当前登录用户所在公司 data condition type.
     */
    当前登录用户所在公司,
    /**
     * 当前登录用户所在公司及以下部门 data condition type.
     */
    当前登录用户所在公司及以下部门,
    /**
     * 当前登录用户同级部门 data condition type.
     */
    当前登录用户同级部门,
    /**
     * 当前登录用户同级公司 data condition type.
     */
    当前登录用户同级公司,
    ;

    /**
     * Get object.
     *
     * @param str the str
     * @return the object
     */
    public static Object get(Object str) {
        try {
            DataConditionType value = DataConditionType.valueOf(str.toString());
            switch (value) {
                case 当前登录用户名:
                    if (value.name().equals(str)) {
                        return UserCurrentUtils.getRealName();
                    }
                case 当前登录用户ID:
                    if (value.name().equals(str)) {
                        return UserCurrentUtils.getUserId();
                    }
                case 当前登录用户所在部门:
                    if (value.name().equals(str)) {
                        return UserCurrentUtils.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toList());
                    }
                case 当前登录用户同级公司:
                    return UserCurrentUtils.getDept().stream().map(e -> getStrings(e.getDeptId())).collect(Collectors.toList());
                case 当前登录用户同级部门:
                    //如果没有部门
                    return UserCurrentUtils.getDept().stream().map(e -> AuthorityManagementUtils.getParentDept(e.getDeptId()))
                            .filter(ObjectNull::isNotNull)
                            .flatMap(e -> AuthorityManagementUtils.getChildDepts(e.getId())
                                    .stream().map(SysDeptDto::getId)).collect(Collectors.toList());
                case 当前登录用户所在公司:
                    return UserCurrentUtils.getDept().stream().map(e -> AuthorityManagementUtils.getParentBranchOffice(e.getDeptId()).getId()).collect(Collectors.toList());
                case 当前登录用户所在部门及以下:
                    List<String> collect = new ArrayList<>();
                    UserCurrentUtils.getDept()
                            .forEach(e -> extracted(e.getDeptId(), collect));
                    return collect;
                case 当前登录用户所在公司及以下部门:
                    //查询用户所在公司
                    return UserCurrentUtils.getDept().stream().map(e -> AuthorityManagementUtils.getParentBranchOffice(e.getDeptId()))
                            .map(DataConditionType::branch)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                default:
                    return str;
            }
        } catch (IllegalArgumentException e) {
            log.error("系统数据权限未匹配到数据" + str);
            return str;
        } catch (RuntimeException e) {
            return "未匹配到数据";
        }
    }

    private static void extracted(String deptId, List<String> collect) {
        List<SysDeptDto> childDepts = AuthorityManagementUtils.getChildDepts(deptId);
        getChildDepts(childDepts, collect);
        collect.add(deptId);
    }

    @NotNull
    private static List<String> branch(SysDeptDto sysDeptDto) {
        if (ObjectNull.isNull(sysDeptDto)) {
            //获取顶级公司
            List<SysDeptDto> deptTree = AuthorityManagementUtils.getDeptTree();
            List<String> deptList = new ArrayList<>();
            getDeptTree(deptTree, deptList);
            return deptList;
        } else {
            SysDeptDto dept = AuthorityManagementUtils.getParentBranchOffice(sysDeptDto.getId());
            List<SysDeptDto> deptDtos = AuthorityManagementUtils.getChildDepts(dept.getId());
            List<String> deptList = new ArrayList<>();
            deptList.add(sysDeptDto.getId());
            getChildDepts(deptDtos, deptList);
            return deptList;
        }
    }

    @Nullable
    private static List<String> getStrings(String deptId1) {
        SysDeptDto deptDtov = AuthorityManagementUtils.getParentBranchOffice(deptId1);
        if (ObjectNull.isNull(deptDtov)) {
            //没有找到返回空
            return null;
        } else {
            SysDeptDto parentDept = AuthorityManagementUtils.getParentDept(deptDtov.getId());
            if (ObjectNull.isNull(parentDept)) {
                return null;
            } else {
                //找同级公司
                List<String> offices = AuthorityManagementUtils.getChildDepts(deptDtov.getId())
                        .stream().filter(e -> e.getType().equals(DeptEnum.branchOffice))
                        .map(SysDeptDto::getId).collect(Collectors.toList());
                return offices;
            }
        }
    }

    private static void getDeptTree(List<SysDeptDto> deptTree, List<String> list) {
        if (ObjectNull.isNotNull(deptTree)) {
            for (SysDeptDto childDept : deptTree) {
                list.add(childDept.getId());
                getDeptTree(childDept.getChildList(), list);
            }
        }
    }

    /**
     * 递归获取下级子部门信息
     *
     * @param childDepts
     * @param list
     */
    private static void getChildDepts(List<SysDeptDto> childDepts, List<String> list) {
        if (ObjectNull.isNotNull(childDepts)) {
            for (SysDeptDto childDept : childDepts) {
                list.add(childDept.getId());
                getChildDepts(AuthorityManagementUtils.getChildDepts(childDept.getId()), list);
            }
        }
    }

}
