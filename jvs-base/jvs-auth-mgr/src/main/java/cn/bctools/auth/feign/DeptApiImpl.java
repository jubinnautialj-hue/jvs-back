package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.enums.DeptEnum;
import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.service.DeptService;
import cn.bctools.auth.service.UserTenantService;
import cn.bctools.common.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 部门相关接口
 *
 * @Author: GuoZi
 */
@RequestMapping
@RestController
@AllArgsConstructor
public class DeptApiImpl implements AuthDeptServiceApi {

    DeptService deptService;
    UserTenantService userTenantService;


    @Override
    public R<List<SysDeptDto>> getAll() {
        List<Dept> list = deptService.list(Wrappers.<Dept>lambdaQuery().select(Dept::getId, Dept::getName, Dept::getSort, Dept::getParentId, Dept::getLeaderId));
        return R.ok(BeanCopyUtil.copys(list, SysDeptDto.class));
    }

    @Override
    public R<SysDeptDto> getById(String deptId) {
        Dept dept = deptService.getOne(Wrappers.<Dept>lambdaQuery()
                .select(Dept::getId, Dept::getName, Dept::getType, Dept::getParentId)
                .eq(Dept::getId, deptId));
        return R.ok(BeanCopyUtil.copy(dept, SysDeptDto.class));
    }

    @Override
    public R<List<SysDeptDto>> getByIds(List<String> deptIds) {
        if (ObjectUtils.isEmpty(deptIds)) {
            return R.ok(Collections.emptyList());
        }
        List<Dept> list = deptService.list(Wrappers.<Dept>lambdaQuery()
                .select(Dept::getId, Dept::getName, Dept::getParentId)
                .in(Dept::getId, deptIds));
        return R.ok(BeanCopyUtil.copys(list, SysDeptDto.class));
    }

    @Override
    public R<List<SysDeptDto>> getAllTree() {
        List<SysDeptDto> deptList = this.getAll().getData();
        // 添加根节点, 顶级部门的上级id默认为当前租户id
        String tenantId = TenantContextHolder.getTenantId();
        SysDeptDto root = new SysDeptDto()
                .setId(tenantId)
                .setChildList(Collections.emptyList());
        deptList.add(root);
        // 转树形结构
        TreeUtils.list2Tree(deptList, tenantId, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
        return R.ok(root.getChildList());
    }

    @Override
    public R<List<SysDeptDto>> getDeptTree(String deptId) {
        List<SysDeptDto> deptList = this.getAll().getData();
        // 添加根节点, 顶级部门的上级id默认为当前租户id
        SysDeptDto root = new SysDeptDto()
                .setId(deptId)
                .setChildList(Collections.emptyList());
        deptList.add(root);
        // 转树形结构
        TreeUtils.list2Tree(deptList, deptId, SysDeptDto::getId, SysDeptDto::getParentId, SysDeptDto::setChildList, Comparator.comparing(SysDeptDto::getSort));
        return R.ok(root.getChildList());
    }

    /**
     * 找下级部门数据
     *
     * @param deptId
     * @param list
     */
    public void getChild(String deptId, List<Dept> list) {
        List<Dept> depts = deptService.list(Wrappers.<Dept>lambdaQuery()
                .select(Dept::getId, Dept::getName, Dept::getParentId)
                .eq(Dept::getParentId, deptId));
        if (ObjectNull.isNotNull(depts)) {
            list.addAll(depts);
            depts.forEach(e -> getChild(e.getId(), list));
        }
    }

    @Override
    public R<List<SysDeptDto>> getChildList(String deptId) {
        List<Dept> list = new ArrayList<>();
        getChild(deptId, list);
        //递归查找下级部门
        return R.ok(BeanCopyUtil.copys(list, SysDeptDto.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<SysDeptDto> getParent(String deptId) {
        Dept child = deptService.getById(deptId);
        if (Objects.isNull(child)) {
            return R.ok();
        }
        Dept parent;
        String parentId = child.getParentId();
        if (StringUtils.isBlank(parentId) || TenantContextHolder.getTenantId().equals(parentId)) {
            parent = child;
        } else {
            parent = deptService.getOne(Wrappers.<Dept>lambdaQuery()
                    .select(Dept::getId, Dept::getName, Dept::getParentId, Dept::getLeaderId)
                    .eq(Dept::getId, parentId));
        }
        return R.ok(BeanCopyUtil.copy(parent, SysDeptDto.class));
    }

    @Override
    public R<SysDeptDto> getParentBranchOffice(String deptId) {
        Dept parent = deptService.getById(deptId);
        if (Objects.isNull(parent)) {
            return R.ok();
        } else {
            if (DeptEnum.branchOffice.equals(parent.getType())) {
                SysDeptDto copy = BeanCopyUtil.copy(parent, SysDeptDto.class);
                return R.ok(copy);
            } else {
                if (parent.getParentId().equals(TenantContextHolder.getTenantId())) {
                    //如果相同，则直接直接返回当前这一级
                    SysDeptDto copy = BeanCopyUtil.copy(parent, SysDeptDto.class);
                    return R.ok(copy);
                }
                //递归找
                return getParentBranchOffice(parent.getParentId());
            }
        }
    }

    @Override
    public R<String> saveOrUpdate(SysDeptDto dto) {
        if(ObjectNull.isNull(dto.getParentId())){
            dto.setParentId(TenantContextHolder.getTenantId());
        }
        deptService.saveOrUpdate(BeanCopyUtil.copy(dto, Dept.class));
        return R.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R delete(String deptId) {
        //删除部门
        List<UserTenant> list = userTenantService.list(new LambdaQueryWrapper<UserTenant>().like(UserTenant::getDeptId, deptId));
        list.forEach(e -> e.getDeptId().removeIf(s -> s.equals(deptId)));
        userTenantService.updateBatchById(list);
        deptService.removeById(deptId);
        return R.ok(true, "删除成功");
    }

    @Override
    public R<List<SysDeptDto>> search(SysDeptDto dto) {
        Dept copy = BeanCopyUtil.copy(dto, Dept.class);
        List<Dept> list = deptService.list(Wrappers.query(copy));
        if (ObjectNull.isNotNull(list)) {
            List<SysDeptDto> copys = BeanCopyUtil.copys(list, SysDeptDto.class);
            return R.ok(copys);
        } else {
            return R.ok();
        }
    }
}
