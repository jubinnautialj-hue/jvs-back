package cn.bctools.ai.api;

import cn.bctools.ai.config.DisableLoadBalancerConfig;
import cn.bctools.ai.dto.AiDatasetDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 * url 留空，通过 target 代替
 */
@FeignClient(name = "${ai:http://jvs-ai}", url = "", configuration = DisableLoadBalancerConfig.class)
public interface JvsAiDatasetRoleApi {


    /**
     * The constant PREFIX.
     */
    String PREFIX = "/aichat/api/ai/dataset/role";

    /**
     * 当在聊天过程中需要问问题时，会根据当前用户的场景权限时实调用此接口获取对应的权限，并加载再到向量数据库中去搜索内容。
     * 请在集成项目中，实现这个接口。并返回相关内容
     * 只返回知识库 id，和文档 id即可。 其它字段可以忽略
     *
     * @return the r
     */
    @GetMapping(PREFIX + "/dataset")
    R<List<AiDatasetDto>> dataset();

}
