package cn.bctools.im.feign.api;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "user-api",  configuration = UserAuthFeignConfig.class)
public interface AuthUserServiceFeignClient extends AuthUserServiceApi {


}
