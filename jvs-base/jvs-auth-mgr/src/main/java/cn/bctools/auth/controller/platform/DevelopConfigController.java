package cn.bctools.auth.controller.platform;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author zx
 */
@Api(tags = "系统更新的配置信息")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/develop/config")
public class DevelopConfigController {

    @Autowired
    @SuppressWarnings("all")
    private RedisTemplate<String, Object> redisTemplate;
    private RedisUtils redisUtils;

    private final SysConfigsService sysConfigService;

    /**
     * 刷新二级缓存数据
     *
     * @return
     */
    @Log
    @GetMapping("/refresh/mybatis/cache")
    public R refresh() {
        if (UserCurrentUtils.getCurrentUser().getPlatformAdmin()) {
            Set<String> keys = redisTemplate.keys(RedisUtils.prefix + JvsRedisCache.PREFIX + "*");
            redisTemplate.delete(keys);
            //清除清楚其它缓存
            redisUtils.publish("functionCleanCache", null);
            return R.ok("刷新成功");
        }
        return R.ok();
    }

    /**
     * 编辑或修改信息配置
     *
     * @param sysConfigs
     * @return
     */
    @Log
    @PostMapping
    @ApiOperation(value = "保存开发配置")
    public R<SysConfigs> edit(@RequestBody SysConfigs sysConfigs) {
        TenantContextHolder.clear();
        sysConfigs.setConfigLevel(TypeEnum.platform).setTenantId(null);
        sysConfigService.saveOrUpdate(sysConfigs);
        return R.ok();
    }
}
