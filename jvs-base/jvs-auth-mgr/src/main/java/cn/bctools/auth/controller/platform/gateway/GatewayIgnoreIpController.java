package cn.bctools.auth.controller.platform.gateway;

import cn.bctools.auth.service.InitConfigService;
import cn.bctools.common.utils.R;
import cn.bctools.gateway.entity.GatewayIgnoreIp;
import cn.bctools.gateway.mapper.GatewayIgnoreIpMapper;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 网关路径忽略
 * <p>
 *
 * @author Auto Generator
 */
@Api(tags = "网关忽略加密IP")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/GatewayIgnoreIp")
public class GatewayIgnoreIpController {

    GatewayIgnoreIpMapper service;
    InitConfigService configService;


    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<GatewayIgnoreIp>> page(Page<GatewayIgnoreIp> page, GatewayIgnoreIp dto) {
        service.selectPage(page, Wrappers.lambdaQuery(dto));
        return R.ok(page);
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<GatewayIgnoreIp> detail(GatewayIgnoreIp dto) {
        return R.ok(service.selectOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<GatewayIgnoreIp> save(@RequestBody GatewayIgnoreIp dto) {
        service.insert(dto);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<GatewayIgnoreIp> edit(@RequestBody GatewayIgnoreIp dto) {
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
        configService.refresh();
        return R.ok();
    }

}
