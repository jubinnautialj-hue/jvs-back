package cn.bctools.design.project.handler;

import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;

import org.jetbrains.annotations.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * JVS设计套件
 *
 * @Author: GuoZi
 */
public interface IJvsDesigner {

    /**
     * 获取设计的权限配置
     *
     * @param designId 设计id
     * @return 权限配置
     */
    default DesignRoleSettingDto getDesignRole(@NotNull String designId) {
        return null;
    }

    /**
     * 获取设计的按钮配置
     *
     * @param designId 设计id
     * @param useCase
     * @return 按钮配置
     */
    default List<? extends ButtonSettingDto> getButtonSettings(@NotNull String designId, String useCase) {
        return null;
    }

    /**
     * 删除设计
     *
     * @param appId    应用id
     * @param designId 设计id
     */
    default void delete(String appId, String designId) {
        // do nothing
    }

    /**
     * 修改名称
     *
     * @param appId    应用id
     * @param designId 设计id(为空时修改该应用下所有设计)
     * @param name     名称(为空时不做修改)
     */
    default void updateName(String appId, @Nullable String designId, @Nullable String name) {
        // do nothing
    }

    /**
     * 获取设计所属数据模型id
     *
     * @param appId     应用id
     * @param designId  设计id
     * @return
     */
    default String getDataModelId(String appId, String designId) {
        return null;
    }

    /**
     * 应用删除前
     *
     * @param appId 应用id
     */
    default void beforeAppDeleted(String appId) {
        // do nothing
    }

    /**
     * 显示隐藏功能组件
     *
     * @param appId
     * @param designId
     * @param mobileDisplay
     * @param pcDisplay
     */
    default void display(String appId, String designId, Boolean mobileDisplay, Boolean pcDisplay) {

    }
}
