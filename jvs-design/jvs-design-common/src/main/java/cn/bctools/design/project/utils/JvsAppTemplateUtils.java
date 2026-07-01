package cn.bctools.design.project.utils;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpRequest;
import cn.hutool.system.oshi.OshiUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wl
 */
@Slf4j
public class JvsAppTemplateUtils {
    /**
     * 目前不支持平台应用
     */
    public static final String DOMAIN = "frame.bctools.cn";

    /**
     * 应用版本迭代操作类型上下文key
     */
    private static final String CONTEXT_VERSION_ITERATION_TYPE = "jvs-design-version-iteration-type";

    /**
     * 应用模板文件名
     */
    private static final String CONTEXT_TEMPLATE_FILE_NAME = "app_template_file_name";

    /**
     * 根据请求地址
     *
     * @param request
     * @return
     */
    public static R get(HttpRequest request) {
        //加载在线模板添加上平台上的模板数据
        try {
            String body = request
                    .header("jvs", MD5.create().digestHex((OshiUtil.getSystem().getHardwareUUID() + OshiUtil.getProcessor().getProcessorIdentifier().getProcessorID()).replace(" ", "")))
                    // 2秒超时
                    .timeout(2000)
                    .execute().body();
            R r = JSONObject.parseObject(body, R.class);
            if (r.is()) {
                return r;
            } else {
                //如果失败了直接返回空
                return R.ok();
            }
        } catch (Exception e) {
            //可能会失败
            //在线模板不能删除
            log.error("在线模板获取异常", e);
            return null;
        }
    }

    /**
     * 设置应用版本迭代操作类型上下文
     *
     * @param type 应用版本迭代操作类型
     */
    public static void setContextVersionIterationType(VersionIterationTypeEnum type) {
        SystemThreadLocal.set(CONTEXT_VERSION_ITERATION_TYPE, type);
    }

    /**
     * 从上下文获取应用版本迭代操作类型
     *
     * @return 应用版本迭代操作类型
     */
    public static VersionIterationTypeEnum getContextVersionIterationType() {
        return SystemThreadLocal.get(CONTEXT_VERSION_ITERATION_TYPE);
    }

    /**
     * 设置应用模板文件名
     *
     * @param fileName 模板文件名
     */
    public static void setTemplateFileName(String fileName) {
        SystemThreadLocal.set(CONTEXT_TEMPLATE_FILE_NAME, fileName);
    }

    /**
     * 从上下文获取应用模板文件名
     *
     * @return 应用模板文件名
     */
    public static String getTemplateFileName() {
        return SystemThreadLocal.get(CONTEXT_TEMPLATE_FILE_NAME);
    }
}
