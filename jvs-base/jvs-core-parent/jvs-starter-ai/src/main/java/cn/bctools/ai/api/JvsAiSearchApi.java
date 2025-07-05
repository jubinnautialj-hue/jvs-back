package cn.bctools.ai.api;

import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 */
@FeignClient(value = "jvs-ai", contextId = "default-search")
public interface JvsAiSearchApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/aichat/api/ai/search";

    /**
     * 互联网搜索
     */
    @PostMapping(PREFIX + "/query")
    R<String> query(@RequestBody String query);

}
