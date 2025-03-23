package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.SelectedServiceApi;
import cn.bctools.auth.api.dto.*;
import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.Job;
import cn.bctools.auth.entity.Role;
import cn.bctools.auth.entity.UserGroup;
import cn.bctools.auth.service.*;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.utils.*;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@RequestMapping
@RestController
@AllArgsConstructor
public class SelectedApiImpl implements SelectedServiceApi {

    JobService jobService;
    UserService userService;
    DeptService deptService;
    RoleService roleService;
    UserGroupService userGroupService;

    @Override
    public R<UserSelectedDto> search(SearchDto searchDto) {
        boolean jobBoolean = false;
        boolean userBoolean = false;
        boolean roleBoolean = false;
        boolean deptBoolean = false;
        boolean groupBoolean = false;
        if (ObjectNull.isNotNull(searchDto, searchDto.getType(), searchDto.getValue())) {
            switch (searchDto.getType()) {
                case user:
                    userBoolean = true;
                    break;
                case role:
                    roleBoolean = true;
                    break;
                case dept:
                    deptBoolean = true;
                    break;
                case job:
                    jobBoolean = true;
                    break;
                case group:
                    groupBoolean = true;
                    break;
                default:
                    break;
            }
        }
        // 添加顶级部门节点
        String rootDeptId = TenantContextHolder.getTenantId();
        List<Dept> list = new ArrayList<>();
        if (ObjectNull.isNotNull(searchDto.getRangType())) {
            switch (searchDto.getRangType()) {
                case current:
                    //本部门及以下
                    list = UserCurrentUtils.getDept().stream().flatMap(e -> deptService.getAllChild(e.getDeptId()).stream()).collect(Collectors.toList());
                    break;
                case samelevel:
                    list = UserCurrentUtils.getDept().stream()
                            .map(e -> deptService.getById(e.getDeptId()))
                            .flatMap(e -> deptService.getAllChild(e.getParentId()).stream())
                            .collect(Collectors.toList());
                    break;
                case dept:
                    if (ObjectNull.isNotNull(searchDto.getRangIds())) {
                        //指定部门
                        list = searchDto.getRangIds().stream().flatMap(e -> deptService.getAllChild(e).stream()).collect(Collectors.toList());
                    }
            }
        } else {
            list = deptService.list(new LambdaQueryWrapper<Dept>()
                    .select(Dept::getId, Dept::getName, Dept::getParentId, Dept::getSort)
                    .like(deptBoolean, Dept::getName, searchDto.getValue())
                    .orderByDesc(Dept::getCreateTime));
        }

        if (deptBoolean && ObjectNull.isNotNull(list)) {
            //根据树形部门结构查询递归反推查询整体树型
            Map<String, Dept> map = list.stream().collect(Collectors.toMap(Dept::getId, e -> e));
            getParentList(list.stream().map(Dept::getParentId).collect(Collectors.toSet()), map);
            list = map.values().stream().collect(Collectors.toList());
        }
        List<SysDeptDto> deptList = BeanCopyUtil.copys(list, SysDeptDto.class);
        final UserSelectedDto userSelectedDto = new UserSelectedDto();

        //如果是为默认的租户顶级，添加一级
        if (rootDeptId == TenantContextHolder.getTenantId()) {
            if (ObjectNull.isNull(searchDto.getRangType())) {
                SysDeptDto root = new SysDeptDto().setId(rootDeptId).setSort(0).setChildList(Collections.emptyList());
                deptList.add(root);
                // 转树形结构
                SysDeptDto deptDto = TreeUtils.list2Tree(deptList, rootDeptId, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
                if (Objects.isNull(deptDto)) {
                    userSelectedDto.setDepts(Collections.emptyList());
                } else {
                    userSelectedDto.setDepts(deptDto.getChildList());
                }
            } else {
                List<SysDeptDto> deptlist = new ArrayList<>();
                switch (searchDto.getRangType()) {
                    case dept:
                        if (ObjectNull.isNotNull(searchDto.getRangIds())) {
                            searchDto.getRangIds().forEach(e -> {
                                SysDeptDto sysDeptDto = TreeUtils.list2Tree(deptList, e, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
                                deptlist.add(sysDeptDto);
                            });
                        }
                        break;
                    case current:
                        Set<String> ids = UserCurrentUtils.getCurrentUser().getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toSet());
                        if (ObjectNull.isNotNull(ids)) {
                            ids.forEach(e -> {
                                SysDeptDto sysDeptDto = TreeUtils.list2Tree(deptList, e, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
                                deptlist.add(sysDeptDto);
                            });
                        }
                        break;
                    case samelevel:
                        if (ObjectNull.isNotNull(UserCurrentUtils.getDept())) {
                            UserCurrentUtils.getDept()
                                    .stream()
                                    .map(e -> deptService.getById(e.getDeptId()))
                                    .flatMap(e -> deptService.list(new LambdaQueryWrapper<Dept>().eq(Dept::getParentId, e.getParentId())).stream())
                                    .filter(ObjectNull::isNotNull)
                                    .forEach(e -> {
                                        SysDeptDto sysDeptDto = TreeUtils.list2Tree(deptList, e.getId(), SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
                                        deptlist.add(sysDeptDto);
                                    });
                        }
                        break;
                }
                userSelectedDto.setDepts(deptlist);
            }
        } else {
            // 转树形结构
            SysDeptDto deptDto = TreeUtils.list2Tree(deptList, rootDeptId, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
            List<SysDeptDto> objects = new ArrayList<SysDeptDto>();
            objects.add(deptDto);
            userSelectedDto.setDepts(objects);
        }

        List<UserGroup> groups = userGroupService.list(new LambdaQueryWrapper<UserGroup>()
                .select(UserGroup::getId, UserGroup::getName, UserGroup::getUsers)
                .like(groupBoolean, UserGroup::getName, searchDto.getValue()));
        userSelectedDto.setGroups(BeanCopyUtil.copys(groups, UserGroupDto.class));
        final List<Role> roles = roleService.list(new LambdaQueryWrapper<Role>()
                .select(Role::getId, Role::getRoleName, Role::getRoleDesc)
                .like(roleBoolean, Role::getRoleName, searchDto.getValue()));
        userSelectedDto.setRoles(BeanCopyUtil.copys(roles, SysRoleDto.class));
        final List<Job> jobs = jobService.list(new LambdaQueryWrapper<Job>()
                .select(Job::getId, Job::getName)
                .like(jobBoolean, Job::getName, searchDto.getValue()));
        userSelectedDto.setJobs(BeanCopyUtil.copys(jobs, SysJobDto.class));
        return R.ok(userSelectedDto);
    }

    private void getParentList(Set<String> ids, Map<String, Dept> map) {
        List<Dept> depts = deptService.listByIds(ids);
        if (ObjectNull.isNotNull(depts)) {
            ids.clear();
            depts.forEach(e -> {
                ids.add(e.getParentId());
                map.put(e.getId(), e);
            });
            getParentList(ids, map);
        }
    }
}
