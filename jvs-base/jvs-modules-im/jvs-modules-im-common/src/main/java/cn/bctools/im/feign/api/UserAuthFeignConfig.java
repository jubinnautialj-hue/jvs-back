package cn.bctools.im.feign.api;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.im.utils.ImLoginUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
public class UserAuthFeignConfig implements RequestInterceptor {

    @SneakyThrows
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> loginParam = Optional.ofNullable(ImLoginUtil.get()).orElse(new HashMap<>());
        final String version = loginParam.get(ImLoginUtil.VERSION);
        if (ObjectNull.isNotNull(version)) {
            requestTemplate.header(SysConstant.VERSION, URLEncoder.encode(version, "UTF-8"));
        }
        requestTemplate.header(HttpHeaders.AUTHORIZATION, loginParam.get(ImLoginUtil.TOKEN));
    }

}
