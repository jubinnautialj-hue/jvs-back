package cn.bctools.auth.controller.platform.gateway;

import cn.bctools.auth.service.InitConfigService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.gateway.entity.GatewayRoutePo;
import cn.bctools.gateway.mapper.GatewayRouteMapper;
import cn.bctools.log.annotation.Log;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网关路由表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "网关路由表")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/GatewayRoutePo")
public class GatewayRoutePoController {

    GatewayRouteMapper service;
    RedisUtils redisUtils;
    InitConfigService configService;

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<GatewayRoutePo>> page(Page<GatewayRoutePo> page, GatewayRoutePo dto) {
        service.selectPage(page, Wrappers.lambdaQuery(dto));
        return R.ok(page);
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<GatewayRoutePo> detail(GatewayRoutePo dto) {
        return R.ok(service.selectOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<GatewayRoutePo> save(@RequestBody GatewayRoutePo dto) {
        service.insert(dto);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<GatewayRoutePo> edit(@RequestBody GatewayRoutePo dto) {
        service.updateById(dto);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R remove(@PathVariable String id) {
        return R.ok(service.deleteById(id));
    }

    @Log
    @ApiOperation("刷新")
    @GetMapping("/refresh")
    public R refresh() {
        List<GatewayRoutePo> gatewayRoutePos = service.selectList(new LambdaQueryWrapper<GatewayRoutePo>());
        if (ObjectNull.isNotNull(gatewayRoutePos)) {
            redisUtils.publish("gatewayRoute", gatewayRoutePos);
        }
        return R.ok();
    }

}
