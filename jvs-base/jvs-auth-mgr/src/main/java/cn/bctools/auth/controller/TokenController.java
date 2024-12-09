package cn.bctools.auth.controller;

import cn.bctools.auth.service.ApplyService;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@RestController
@Api(tags = "token管理")
@AllArgsConstructor
@RequestMapping("/token")
public class TokenController {

    JvsOAuth2AuthorizationServiceImpl authorizationService;
    RedisUtils redisUtils;
    RedisTemplate redisTemplate;
    ApplyService applyService;

    @Log
    @ApiOperation(value = "后台强制退出", notes = "由管理员在后台管理系统中直接退出系统")
    @DeleteMapping("/{jvs}")
    public R forceLogout(@PathVariable("jvs") String jvs) {
        List<OAuth2Authorization> keys = authorizationService.keys(jvs, OAuth2TokenType.ACCESS_TOKEN);
        keys.forEach(authorizationService::remove);
        //删除jvs
        redisUtils.del("jvs:token:" + jvs);
        return R.ok();
    }

    @Log
    @ApiOperation(value = "分页查询登陆的用户", notes = "同一个人可以在不同的应用下进行同时登录")
    @GetMapping("/page")
    public R<Page> page(Page page) {
        String tenantId = TenantContextHolder.getTenantId();
        Set<String> keys = redisUtils.keys("jvs:token:" + tenantId + "*");
        //处理数据
        List voList = keys.parallelStream()
                .map(e -> {
                    String jvs = e.toString().substring(4);
                    List o = (List) redisUtils.get(jvs);
                    Object o1 = o.get(1);
                    OAuth2Authorization byToken = null;
                    try {
                        byToken = authorizationService.findByToken(o1.toString().replaceAll(OAuth2TokenType.REFRESH_TOKEN.getValue().toLowerCase(), ""), OAuth2TokenType.REFRESH_TOKEN);
                    } catch (Exception ex) {
                        redisUtils.del(e.substring(RedisUtils.getPrefix().length()));
                        return null;
                    }
                    CustomUser user = byToken.getAttribute("user");
                    assert user != null;
                    Map<String, Object> stringObjectMap = BeanToMapUtils.beanToMap(user.getUserDto());
                    stringObjectMap.remove("tenants");
                    stringObjectMap.remove("password");
                    stringObjectMap.remove("tenant");
                    stringObjectMap.remove("id");
                    stringObjectMap.put("expiresAt", byToken.getRefreshToken().getToken().getExpiresAt());
                    stringObjectMap.put("jvs", jvs.split(":")[2]);
                    return stringObjectMap;
                })
                .filter(e -> ObjectNull.isNotNull(e))
                .collect(Collectors.toList());
        page.setTotal(voList.size()).setRecords(voList);
        return R.ok(page);
    }

}
