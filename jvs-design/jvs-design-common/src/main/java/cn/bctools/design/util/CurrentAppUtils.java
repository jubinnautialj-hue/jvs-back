package cn.bctools.design.util;

import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.project.entity.JvsApp;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 应用缓存
 */
public class CurrentAppUtils {

    /**
     * 应用信息
     */
    private static final String INTERCEPTOR_APP = "app";

    /**
     * 设置应用
     *
     * @param jvsApp 应用
     */
    public static void setApp(JvsApp jvsApp) {
        SystemThreadLocal.set(INTERCEPTOR_APP, jvsApp);
    }

    /**
     * 获取应用
     *
     * @return 应用
     */
    public static JvsApp getApp() {
        return SystemThreadLocal.get(INTERCEPTOR_APP);
    }


    public static void clear() {
        SystemThreadLocal.remove(INTERCEPTOR_APP);
    }


    /**
     * 获取应用版本号
     *
     * @return 应用版本号
     */
    public static String getAppVersion() {
        return Optional.ofNullable(getApp()).map(JvsApp::getUseVersion).orElse(null);
    }

    /**
     * 应用是否启用轻应用版本功能
     *
     * @return true-启用了版本功能，false-未启用版本功能
     */
    public static Boolean getAppEnableVersionFeature() {
        return Optional.ofNullable(getApp()).map(JvsApp::getEnableVersionFeature).orElse(false);
    }

}
