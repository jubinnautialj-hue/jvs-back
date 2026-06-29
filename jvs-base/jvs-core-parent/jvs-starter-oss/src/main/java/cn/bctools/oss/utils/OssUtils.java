package cn.bctools.oss.utils;

import cn.bctools.common.utils.SystemThreadLocal;

/**
 * @author jvs  将业务数据的文件归纳到一个目录下
 */
public class OssUtils {
    /**
     * 将业务类型和业务数据值放到上下文中
     *
     * @param type       业务类型
     * @param businessId 业务值
     */
    public static void setOssTemplateBusinessId(String type, String businessId) {
        //根据目录进行分离
        SystemThreadLocal.set("ossbusinessId", "jvs_" + businessId + "/" + type + "/");
    }

    /**
     * Gets oss template business id.
     *
     * @return the oss template business id
     */
    public static Object getOssTemplateBusinessId() {
        return SystemThreadLocal.get("ossbusinessId");
    }
}
