package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.Role;
import cn.bctools.auth.entity.enums.RoleMemberScopeEnum;
import cn.bctools.auth.mapper.RoleMapper;
import cn.bctools.auth.service.RoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 *
 * @author
 */
@Service
@AllArgsConstructor
@Slf4j
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<String> getScopeAllRoles() {
        return list(Wrappers.<Role>lambdaQuery()
                .eq(Role::getMemberScope, RoleMemberScopeEnum.ALL))
                .stream()
                .map(Role::getId)
                .collect(Collectors.toList());
    }
}
