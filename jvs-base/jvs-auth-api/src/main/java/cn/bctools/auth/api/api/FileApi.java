package cn.bctools.auth.api.api;

import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface File api.
 *
 * @author guojing
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "file")
public interface FileApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/file";

    /**
     * 上传文件后, 将数据资源通过Feign传递到了统一处理
     *
     * @param entity 用户信息
     * @return 不返回数据信息, 根据R判断
     */
    @PostMapping(PREFIX + "/insert")
    R insert(@RequestBody String entity);

}
