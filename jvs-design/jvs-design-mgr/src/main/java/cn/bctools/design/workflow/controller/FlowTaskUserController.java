package cn.bctools.design.workflow.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.auth.api.enums.UserQueryType;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.UserScopeSelectedDto;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.PersonnelScope;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "[workflow]工作流-工作流用户相关")
@RestController
@RequestMapping("/base/workflow/user")
@AllArgsConstructor
public class FlowTaskUserController {
    private final AuthUserServiceApi authUserServiceApi;

    @Log
    @ApiOperation(value = "用户选择", notes = "多纬度选择对象")
    @PostMapping("/selected/page")
    public R<Page<UserDto>> searchPage(@RequestBody UserScopeSelectedDto dto) {
        Page<UserDto> userPage = new Page<>();
        userPage.setSize(dto.getSize());
        if (CollectionUtils.isEmpty(dto.getPersonnelScopes())) {
            return R.ok(userPage);
        }
        SearchUserDto searchUser = new SearchUserDto();
        searchUser.setType(UserQueryType.or);
        PersonnelScope personnelScope = new PersonnelScope();
        personnelScope.setPersonnelScopes(dto.getPersonnelScopes());
        // 根据用户id范围查询
        List<String> scopeUserIds = personnelScope.getPersonnelIdByType(TargetObjectTypeEnum.user);
        if (CollectionUtils.isNotEmpty(scopeUserIds)) {
            searchUser.setUserIds(scopeUserIds);
        }
        // 根据角色id范围查询
        List<String> scopeRoleIds = personnelScope.getPersonnelIdByType(TargetObjectTypeEnum.role);
        if (CollectionUtils.isNotEmpty(scopeRoleIds)) {
            searchUser.setRoleIds(scopeRoleIds);
        }
        // 根据部门id范围查询
        List<String> scopeDeptIds = personnelScope.getPersonnelIdByType(TargetObjectTypeEnum.dept);
        if (CollectionUtils.isNotEmpty(scopeDeptIds)) {
            searchUser.setDeptIds(scopeDeptIds);
        }
        // 根据岗位id范围查询
        List<String> scopeJobIds = personnelScope.getPersonnelIdByType(TargetObjectTypeEnum.job);
        if (CollectionUtils.isNotEmpty(scopeJobIds)) {
            searchUser.setJobIds(scopeJobIds);
        }
        List<UserDto> scopeUsers = authUserServiceApi.userSearch(searchUser).getData();
        // 条件筛选
        if (StringUtils.isNotBlank(dto.getKey())) {
            scopeUsers = scopeUsers.stream()
                    .filter(u -> (ObjectNull.isNotNull(u.getRealName()) && u.getRealName().contains(dto.getKey()))
                            || (ObjectNull.isNotNull(u.getPhone()) && u.getPhone().contains(dto.getKey())))
                    .collect(Collectors.toList());
        }
        long total = scopeUsers.size();
        if (CollectionUtils.isEmpty(scopeUsers)) {
            return R.ok(userPage);
        }
        scopeUsers = scopeUsers.stream().limit(dto.getSize()).collect(Collectors.toList());

        userPage.setTotal(total);
        userPage.setRecords(scopeUsers);
        return R.ok(userPage);
    }
}
