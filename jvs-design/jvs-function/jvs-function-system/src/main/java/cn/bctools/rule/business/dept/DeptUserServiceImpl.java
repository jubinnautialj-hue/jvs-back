package cn.bctools.rule.business.dept;


import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.enums.DeptEnum;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.lang.Dict;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "根据部门获取用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 4,
        explain = "根据对应的部门信息获取以下所有的用户信息"
)
public class DeptUserServiceImpl implements BaseCustomFunctionInterface<DeptDto> {

    AuthUserServiceApi authUserServiceApi;

    @Override
    public Object execute(DeptDto userDto, Map<String, Object> params) {
        SysDeptDto deptById = AuthorityManagementUtils.getDeptById(userDto.getDept());
        List<String> deptList = getDept(userDto, deptById);
        Map<String, UserDto> userDtoMap = authUserServiceApi.getByDeptIds(deptList).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        if (ObjectNull.isNotNull(userDtoMap)) {
            return Dict.create().set("ids", userDtoMap.keySet()).set("users", userDtoMap.values());
        }
        return new ArrayList<>(0);
    }

    private List<String> getDept(DeptDto userDto, SysDeptDto deptById) {
        List<String> list = new ArrayList<>();
        switch (userDto.getType()) {
            case 请选择类型:
                list.add(deptById.getId());
                break;
            case 所属公司:
                String id = getParentBranchOffice(deptById.getParentId()).getId();
                list.add(id);
                break;
            case 同级部门及以下:
                List<SysDeptDto> childDepts = AuthorityManagementUtils.getChildDepts(deptById.getParentId());
                List<String> deptList = new ArrayList<>();
                getChildDepts(childDepts, deptList);
                list.addAll(deptList);
                break;
            case 当前及下级部门:
                List<SysDeptDto> childDepts2 = AuthorityManagementUtils.getChildDepts(deptById.getId());
                List<String> list2 = new ArrayList<>();
                getChildDepts(childDepts2, list2);
                list2.add(deptById.getId());
                break;

            case 同级部门或公司:
                AuthorityManagementUtils.getChildDepts(deptById.getParentId())
                        .stream()
                        .map(SysDeptDto::getId).forEach(list::add);
                break;
        }
        return list;
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
        SysDeptDto deptById = AuthorityManagementUtils.getDeptById(parentId);
        if (ObjectNull.isNotNull(deptById)) {
            if (deptById.getType().equals(DeptEnum.branchOffice)) {
                return deptById;
            }
        }
        return null;
    }

}

