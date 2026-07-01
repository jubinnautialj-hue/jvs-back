package cn.bctools.design.filter;


import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import com.alibaba.fastjson2.JSONObject;

import javax.servlet.ServletOutputStream;

/**
 * 拦截器工具
 * 用于错误信息返回
 *
 * @author jvs
 */
public class InterceptorUtil {

    /**
     * 抛异常
     *
     * @param outputStream the output stream
     * @param errorMessage the error message
     * @throws Exception the exception
     */
    public static void throwException(ServletOutputStream outputStream, String errorMessage) throws Exception {
        errorMessage = SpringContextUtil.msg(errorMessage);
        outputStream.write(JSONObject.toJSONString(R.failed(errorMessage)).getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
