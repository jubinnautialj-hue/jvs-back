package cn.bctools.design.permission.service;

import cn.bctools.design.permission.entity.PermissionSetting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zhuxiaokang
 * 权限设置
 */
public interface PermissionSettingService extends IService<PermissionSetting> {

    /**
     * 删除指定权限组的所有权限配置
     *
     * @param groupId 权限组id
     */
    void removeByGroup(String groupId);
}
