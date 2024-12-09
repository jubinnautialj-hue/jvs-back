package cn.bctools.auth.controller.platform;

import cn.bctools.auth.entity.User;
import cn.bctools.auth.mapper.SysLogMapper;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.log.annotation.Log;
import cn.bctools.log.po.LogPo;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * 错误请求
 *
 * @author guojing
 */
@Api(tags = "错误请求")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/request")
public class SysRequestController {

    /**
     * 最大查询天数
     */
    private static final int MAX_DAY_QUERY = 7;

    private final SysLogMapper logMapper;
    private final UserService userService;
    private final RedisUtils redisUtils;

    @Log(back = false)
    @ApiOperation("分页错误请求")
    @GetMapping("/page")
    public R<IPage<LogPo>> getDictPage(Page<LogPo> page, LogPo dto) {
        LogPo logPo = new LogPo()
                .setTid(dto.getTid())
                .setFunctionName(dto.getFunctionName())
                .setStatus(dto.getStatus())
                .setBusinessName(dto.getBusinessName());
        if (ObjectNull.isNull(dto.getStartTime(), dto.getEndTime())) {
            return R.failed("数据量大请必须使用时间查询过滤");
        }
        if (ObjectNull.isNull(dto.getTid())) {
            //如果tid查询条件为空时才进行处理
            if (Duration.between(dto.getStartTime(), dto.getEndTime()).toDays() > MAX_DAY_QUERY) {
                return R.failed("查询时间不能超过7天");
            }
        }
        //使用的用户名做为帐户名查询条件
        if (ObjectNull.isNotNull(dto.getUserName())) {
            String userName = dto.getUserName();
            User one = userService.getOne(Wrappers.query(new User().setAccountName(userName)));
            if (ObjectNull.isNotNull(one)) {
                dto.setUserId(one.getId());
                //清楚租户信息查询
                TenantContextHolder.clear();
                return R.ok(logMapper.selectPage(page, Wrappers.lambdaQuery(logPo)
                        .between(LogPo::getStartTime, dto.getStartTime(), dto.getEndTime())
                        .orderByDesc(LogPo::getCreateDate)));
            } else {
                return R.ok(page);
            }
        } else {
            return R.ok(logMapper.selectPage(page, Wrappers.lambdaQuery(logPo)
                    .between(LogPo::getStartTime, dto.getStartTime(), dto.getEndTime())
                    .orderByDesc(LogPo::getCreateDate)));
        }

    }

    @ApiOperation("获取待处理异常信息,默认只显示前20条")
    @GetMapping("/pending/top20")
    public R pending() {
        TenantContextHolder.clear();
        //查询状态为失败的，并处理人为空的前 20条数据
        Page<LogPo> page = new Page<>();
        logMapper.selectPage(page, new LambdaQueryWrapper<LogPo>().isNull(LogPo::getHandleUser).eq(LogPo::getStatus, false).orderByDesc(LogPo::getCreateDate));
        Dict set = Dict.create().set("list", page.getRecords()).set("size", page.getTotal());
        return R.ok(set);
    }

    @ApiOperation("处理异常接口")
    @DeleteMapping("/handler/{id}")
    public R handler(@PathVariable String id) {
        //需要清除租户
        TenantContextHolder.clear();
        LogPo logPo = logMapper.selectById(id);
        logPo.setHandleUser(UserCurrentUtils.getAccountName());
        logMapper.updateById(logPo);
        return R.ok();
    }

}
