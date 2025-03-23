package cn.bctools.im.feign.api;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "user-auth", configuration = UserAuthFeignConfig.class)
public interface UserAuthFeignClient {

    String PREFIX = "/index";

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping(PREFIX + "/user/info")
    R<UserDto> userInfo();
}
