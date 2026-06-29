package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.RoleGroup;
import cn.bctools.auth.mapper.RoleGroupMapper;
import cn.bctools.auth.service.RoleGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色分组服务
 *
 * @author
 */
@Service
@AllArgsConstructor
@Slf4j
public class RoleGroupServiceImpl extends ServiceImpl<RoleGroupMapper, RoleGroup> implements RoleGroupService {

}
