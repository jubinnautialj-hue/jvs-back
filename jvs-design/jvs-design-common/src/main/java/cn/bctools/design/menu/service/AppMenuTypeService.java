package cn.bctools.design.menu.service;

import cn.bctools.design.menu.entity.AppMenuType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 轻应用菜单目录 服务类
 */
public interface AppMenuTypeService extends IService<AppMenuType> {

    /**
     * 修改目录
     *
     * @param id       目录id
     * @param appId    应用id
     * @param oldType  旧应用目录
     * @param newType  新应用目录
     * @param icon     图标
     * @param parentId 上级目录id
     */
    void updateType(String id, String appId, String oldType, String newType, String icon, String parentId);

    /**
     * 删除目录
     *
     * @param appId
     * @param typeId
     */
    void removeType(String appId, String typeId);

    /**
     * 获取应用所有目录
     *
     * @param appId 应用id
     * @return
     */
    List<AppMenuType> getAppAll(String appId);

    /**
     * 获取指定应用版本所有目录
     *
     * @param appId 应用id
     * @param appVersion 应用版本号
     * @return
     */
    List<AppMenuType> getAppAll(String appId, String appVersion);
}
