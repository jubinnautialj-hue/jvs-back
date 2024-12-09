package cn.bctools.auth.controller.platform;

import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author jvs The type Platform controller.
 */
@Api(tags = "平台设置")
@RestController
@AllArgsConstructor
@RequestMapping("/platform")
public class PlatformController {

    private final SysConfigsService sysConfigService;

    /**
     * 获取公告
     *
     * @return the r
     */
    @Log
    @GetMapping("/bulletin")
    @ApiOperation(value = "获取公告")
    public R<SysConfigs> bulletin() {
        TenantContextHolder.clear();
        SysConfigs one = sysConfigService.getOne(new LambdaQueryWrapper<SysConfigs>().eq(SysConfigs::getType, ConfigsTypeEnum.BASE));
        return R.ok(one);
    }

    /**
     * 保存公告
     *
     * @param sysConfigs the sys configs
     * @return r
     */
    @Log
    @PostMapping("/bulletin")
    @ApiOperation(value = "保存配置信息")
    public R saveBulletin(@RequestBody SysConfigs sysConfigs) {
        sysConfigService.saveOrUpdate(sysConfigs);
        return R.ok();
    }

}
