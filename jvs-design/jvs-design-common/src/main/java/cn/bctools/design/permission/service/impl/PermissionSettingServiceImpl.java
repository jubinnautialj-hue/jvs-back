package cn.bctools.design.permission.service.impl;


import cn.bctools.design.permission.entity.PermissionSetting;
import cn.bctools.design.permission.mapper.PermissionSettingMapper;
import cn.bctools.design.permission.service.PermissionSettingService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhuxiaokang
 * 权限设置
 */
@Service
@AllArgsConstructor
public class PermissionSettingServiceImpl extends ServiceImpl<PermissionSettingMapper, PermissionSetting> implements PermissionSettingService, IJvsDesigner {

    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void removeByGroup(String groupId) {
        remove(Wrappers.<PermissionSetting>lambdaQuery().eq(PermissionSetting::getPermissionGroupId, groupId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<PermissionSetting>lambdaQuery().eq(PermissionSetting::getJvsAppId, appId));
    }
}
