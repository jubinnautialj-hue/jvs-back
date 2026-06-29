package cn.bctools.ai.api;

import cn.bctools.ai.config.DisableLoadBalancerConfig;
import cn.bctools.ai.dto.AiToolsDto;
import cn.bctools.ai.dto.AiUserDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 * url 留空，通过 target 代替
 */
@FeignClient(name = "${ai:http://jvs-ai}", url = "", configuration = DisableLoadBalancerConfig.class)
public interface JvsAILoginCheckApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/ai/login";

    /**
     * 获取这个用户可以通过 ai 调用的功能权限
     */
    @GetMapping(PREFIX)
    R<AiUserDto> check(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

}
