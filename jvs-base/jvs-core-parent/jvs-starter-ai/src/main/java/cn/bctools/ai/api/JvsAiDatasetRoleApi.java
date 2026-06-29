package cn.bctools.ai.api;

import cn.bctools.ai.config.DisableLoadBalancerConfig;
import cn.bctools.ai.dto.AiDatasetDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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
    String PREFIX = "/api/ai/dataset/role";

    /**
     * 当在聊天过程中需要问问题时，会根据当前用户的场景权限时实调用此接口获取对应的权限，并加载再到向量数据库中去搜索内容。
     * 请在集成项目中，实现这个接口。并返回相关内容
     * 只返回知识库 id，和文档 id即可。 其它字段可以忽略
     * <p>
     * 这里针对不同项目的权限可以自己去定义，
     * 如会议系统可以只创建一个库，通过文档去划分权限。
     * 知识库， 可以划分多个库， 同时根据用户的权限去划分文档权限
     *
     * @return the r Map<库 id,List<文档 id>>  返回有哪些库和哪些文档有权限，没有返回的即没有权限
     */
    @GetMapping(PREFIX + "/permission")
    R<Map<String, List<String>>> permission(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

}
