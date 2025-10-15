package cn.bctools.design.permission.service.impl;


import cn.bctools.design.permission.entity.PermissionGroup;
import cn.bctools.design.permission.mapper.PermissionGroupMapper;
import cn.bctools.design.permission.service.PermissionGroupService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhuxiaokang
 * 权限组
 */
@Service
@AllArgsConstructor
public class PermissionGroupServiceImpl extends ServiceImpl<PermissionGroupMapper, PermissionGroup> implements PermissionGroupService, IJvsDesigner {

    private final MapperMethodHandler mapperMethodHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<PermissionGroup>lambdaQuery().eq(PermissionGroup::getJvsAppId, appId));
    }
}
