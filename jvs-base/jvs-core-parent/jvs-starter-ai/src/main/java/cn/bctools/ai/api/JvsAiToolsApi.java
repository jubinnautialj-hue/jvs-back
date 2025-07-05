package cn.bctools.ai.api;

import cn.bctools.ai.config.DisableLoadBalancerConfig;
import cn.bctools.ai.dto.AiCallDto;
import cn.bctools.ai.dto.AiToolsDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


/**
 * The interface Jvs ai api.
 *
 * @author jvs  url 留空，通过 target 代替
 */
@FeignClient(name = "${ai:http://jvs-ai}", url = "", configuration = DisableLoadBalancerConfig.class)
public interface JvsAiToolsApi {


    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/ai/tools";

    /**
     * 获取当前服务有哪些 ai 调用的功能权限
     *
     * @param authorization the authorization
     * @return the r
     */
    @GetMapping(PREFIX)
    R<List<AiToolsDto>> tools(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization);

    /**
     * 调用某一个工具
     *
     * @param aiCallDto     调用工具的参数
     * @param authorization 用户的 token
     * @return the r
     */
    @PostMapping(PREFIX + "/call")
    R<Object> call(@RequestBody AiCallDto aiCallDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

}
