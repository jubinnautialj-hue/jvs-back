package cn.bctools.auth.controller.platform;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.Dict;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户解锁登录，部分将登录用户的解锁状态再
 *
 * @author jvs
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/lock")
public class AccountLockController {

    private final RedisUtils redisUtils;

    @Log(back = false)
    @ApiOperation(value = "获取不能登录的帐号")
    @GetMapping("/account")
    public R account() {
        String key = SysConstant.redisKey("lock", "");
        //返回所有的帐号
        Set<Dict> keys = redisUtils.keys(key + "*")
                .stream()
                .map(e -> {
                    String[] split = toString().split(":");
                    return Dict.create().set("ip", split[3]).set("account", split[4]);
                })
                .collect(Collectors.toSet());
        return R.ok(keys);
    }

}
