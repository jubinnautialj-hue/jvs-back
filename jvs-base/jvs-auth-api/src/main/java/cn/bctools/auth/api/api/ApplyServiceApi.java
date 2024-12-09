package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.ApplyDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * The interface Apply service api.
 * 获取张端的 api
 *
 * @author guojing
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "apply")
public interface ApplyServiceApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/apply";

    /**
     * 获取所有的应用信息
     *
     * @return 获取基础client应用信息 ${@linkplain ApplyDto}
     */
    @GetMapping("/all")
    R<List<ApplyDto>> all();

}
