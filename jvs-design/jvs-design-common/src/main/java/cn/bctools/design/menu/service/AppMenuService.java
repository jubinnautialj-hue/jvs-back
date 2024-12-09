package cn.bctools.design.menu.service;

import cn.bctools.design.menu.entity.AppMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 菜单权限表 服务类
 */
public interface AppMenuService extends IService<AppMenu> {

    /**
     * 显示隐藏功能组件
     *
     * @param appId
     * @param designId
     * @param mobileDisplay
     * @param pcDisplay
     */
    void display(String appId, String designId, Boolean mobileDisplay, Boolean pcDisplay);

    /**
     * 删除菜单
     *
     * @param appId      应用id
     * @param designId   设计id
     */
    void remove(String appId, String designId);

    /**
     * 修改名称/目录
     *
     * @param appId    应用id
     * @param designId 设计id(为空时修改该应用下所有设计)
     * @param newType  新应用目录(为空时不做修改)
     * @param name 设计名称
     */
     void updateType(String appId, String designId, String newType, String name);

    /**
     * 修改权限配置
     *
     * @param appMenu
     */
    void update(AppMenu appMenu);

    /**
     * 获取指定设计的菜单信息
     *
     * @param designId 设计id
     * @param appId 应用id
     * @return
     */
     AppMenu getDesignMenu(String designId, String appId);

    /**
     * 指定应用,目录下是否存在设计
     *
     * @param appId 应用id
     * @param typeId  应用目录Id
     * @return TRUE-目录下有设计，FALSE-目录下无数据
     */
    boolean exist(String appId, String typeId);

    /**
     * 获取指定应用所有设计
     *
     * @param appId 应用id
     * @return
     */
    List<AppMenu> getAppAll(String appId);

    /**
     * 获取指定应用版本所有设计
     *
     * @param appId 应用id
     * @param appVersion 应用版本号
     * @return
     */
    List<AppMenu> getAppAll(String appId, String appVersion);

    /**
     * 批量保存设计的资源
     *
     * @param appId   应用id
     * @param designs 设计集合
     * @param <T>
     */
    <T> void saveBatchPermission(String appId, List<T> designs);
}
