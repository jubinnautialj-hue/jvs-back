package cn.bctools.auth.api.api;

import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.utils.R;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * 租户级配置信息
 *
 * @author guojing
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "tenantConfig")
public interface AuthTenantConfigServiceApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/tenant/config";

    /**
     * 不同类型,返回不同的结果,
     *
     * @param type     不同类型返回不同的结果
     * @param tenantId 获取某个租户的配置信息
     * @return r
     */
    @GetMapping(PREFIX + "/key")
    R<String> key(@RequestParam("type") ConfigsTypeEnum type, @RequestParam("tenantId") String tenantId);

    /**
     * 与key方法配合使用,因为feign无法使用泛型,如果使用rpc可以不关心此功能转换
     * 返回的是字符串,通过此方法还原真实数据,默认转换器,其它应用通过feign请求的时候
     *
     * @param <T>  the type parameter
     * @param type 类型
     * @param body 数据
     * @return t
     */
    default <T extends SysConfigBase> T convertKey(ConfigsTypeEnum type, String body) {
        return (T) JSONObject.parseObject(body, type.cls);
    }

    /**
     * 获取已经启用的有哪些配置项
     *
     * @return 返回所有的类型 r
     */
    @GetMapping(PREFIX + "/keys")
    R<List<ConfigsTypeEnum>> keys();
}
