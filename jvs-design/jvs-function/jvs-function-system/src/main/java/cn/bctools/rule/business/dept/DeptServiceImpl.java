package cn.bctools.rule.business.dept;


import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.enums.DeptEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "指定部门",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 4,
        explain = "根据指定部门或所属公司，返回详细信息。选择同级部门、当前及以下部门或公司，返回对应的 id数组"
)
public class DeptServiceImpl implements BaseCustomFunctionInterface<DeptDto> {

    AuthDeptServiceApi deptServiceApi;

    @Override
    public Object execute(DeptDto userDto, Map<String, Object> params) {
        if (ObjectNull.isNull(userDto.getDept())) {
            throw new BusinessException("未确定选择部门");
        }
        SysDeptDto deptById = AuthorityManagementUtils.getDeptById(userDto.getDept());
        switch (userDto.getType()) {
            case 请选择类型:
                return deptById;
            case 所属公司:
                return getParentBranchOffice(deptById.getParentId());
            case 同级部门及以下:
                List<SysDeptDto> childDepts = AuthorityManagementUtils.getChildDepts(deptById.getParentId());
                List<String> list = new ArrayList<>();
                getChildDepts(childDepts, list);
                return list;
            case 当前及下级部门:
                List<SysDeptDto> childDepts2 = AuthorityManagementUtils.getChildDepts(deptById.getId());
                List<String> list2 = new ArrayList<>();
                getChildDepts(childDepts2, list2);
                list2.add(deptById.getId());
                return list2;
            case 同级部门或公司:
                return AuthorityManagementUtils.getChildDepts(deptById.getParentId())
                        .stream()
                        .map(SysDeptDto::getId).collect(Collectors.toList());
            case 部门名称:
                return deptServiceApi.search(new SysDeptDto().setName(userDto.getDept())).getData();
            case 部门代码:
                return deptServiceApi.search(new SysDeptDto().setDeptCode(userDto.getDept())).getData().get(0).getDeptCode();
            default:

        }
        return userDto;
    }

    /**
     * 递归获取下级子部门信息
     *
     * @param childDepts
     * @param list
     */
    private void getChildDepts(List<SysDeptDto> childDepts, List<String> list) {
        if (ObjectNull.isNotNull(childDepts)) {
            for (SysDeptDto childDept : childDepts) {
                list.add(childDept.getId());
                getChildDepts(AuthorityManagementUtils.getChildDepts(childDept.getId()), list);
            }
        }
    }

    public SysDeptDto getParentBranchOffice(String parentId) {
        if (ObjectNull.isNull(parentId)) {
            return null;
        }
        SysDeptDto deptById = AuthorityManagementUtils.getDeptById(parentId);
        if (ObjectNull.isNotNull(deptById)) {
            if (deptById.getType().equals(DeptEnum.branchOffice)) {
                return deptById;
            }
            // 当前节点不是分公司，继续向上追溯父级
            return getParentBranchOffice(deptById.getParentId());
        }
        return null;
    }

}

