package cn.bctools.data.factory.controller;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Administrator
 */
@Api(tags = "第三方扩展字段")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/auth/extension")
public class AuthExtensionController {
    private final UserExtensionServiceApi userExtensionServiceApi;

    @ApiOperation("获取拓展字段")
    @GetMapping("/field")
    public R<Map<String, String>> getField() {
        Map<String, String> field = userExtensionServiceApi.field().getData();
        return R.ok(field);
    }

}
