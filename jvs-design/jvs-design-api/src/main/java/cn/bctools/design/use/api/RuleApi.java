package cn.bctools.design.use.api;

import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * The interface Rule api.
 * 逻辑引擎的接口 api
 *
 * @author jvs
 */
@Component
@FeignClient(value = "jvs-design-mgr", contextId = "rule")
public interface RuleApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/jvsdesign/rule";

    /**
     * Run r.
     * 执行逻辑引擎,返回对应数据
     *
     * @param key     逻辑调用的 Key
     * @param dataMap 调用逻辑的参数信息
     * @return 返回对应的执行结果 ，类型不确定
     */
    @PostMapping(PREFIX + "/run/{key}")
    R run(@PathVariable("key") String key, @RequestBody Map<String, Object> dataMap);
}
