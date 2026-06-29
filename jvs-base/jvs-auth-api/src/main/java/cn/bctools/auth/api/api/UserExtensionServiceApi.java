package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "userExtension")
public interface UserExtensionServiceApi {
    String PREFIX = "/api/user/extension";

    /**
     * 查询用户的其它扩展信息
     *
     * @param userIds 用户ids数组
     * @param type    扩展类型  类型根据三方登陆类型为准
     * @return
     */
    @Deprecated
    @GetMapping(PREFIX + "/query")
    R<List<UserExtensionDto>> query(@RequestParam("userIds") List<String> userIds, @RequestParam("type") String type);

    @PostMapping(PREFIX + "/query/post")
    R<List<UserExtensionDto>> queryPost(@RequestBody List<String> userIds, @RequestParam("type") String type);

    /**
     * 获取当前租户下配置的用户扩展字段有哪些，
     * 只有启用了三方授权登录后才有这个字段其它的没有
     *
     * @return
     */
    @GetMapping(PREFIX + "/field")
    R<Map<String, String>> field();
}
