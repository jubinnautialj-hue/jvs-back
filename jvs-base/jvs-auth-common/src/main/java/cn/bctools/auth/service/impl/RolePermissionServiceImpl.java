package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.RolePermission;
import cn.bctools.auth.mapper.RolePermissionMapper;
import cn.bctools.auth.service.RolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色菜单基础服务
 *
 * @author Administrator
 */
@Slf4j
@Service
@AllArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
