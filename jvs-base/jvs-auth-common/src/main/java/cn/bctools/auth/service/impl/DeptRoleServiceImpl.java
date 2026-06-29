package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.DeptRole;
import cn.bctools.auth.mapper.DeptRoleMapper;
import cn.bctools.auth.service.DeptRoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 部门角色
 */
@Slf4j
@Service
@AllArgsConstructor
public class DeptRoleServiceImpl extends ServiceImpl<DeptRoleMapper, DeptRole> implements DeptRoleService {

    @Override
    public void clearRoleDept(String roleId) {
        this.remove(Wrappers.<DeptRole>lambdaQuery().eq(DeptRole::getRoleId, roleId));
    }
}
