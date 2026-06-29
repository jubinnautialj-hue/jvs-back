package cn.bctools.auth.controller.platform;

import cn.bctools.auth.service.ApplyService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.service.TenantService;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.entity.dto.PlatformDto;
import cn.bctools.common.enums.*;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.mapper.SysFileMapper;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "平台基础信息")
@RestController
@RequestMapping("/platform/info")
@AllArgsConstructor
public class InfoController {

    TenantService tenantService;
    DiscoveryClient discoveryClient;
    RedisUtils redisUtils;
    SysConfigsService sysConfigService;
    ApplyService applyService;
    UserService userService;
    SysFileMapper fileMapper;

    /**
     * 获取平台信息
     */
    @GetMapping("/base")
    public R<PlatformConfig> base() {
        PlatformConfig config = sysConfigService.getConfig(ConfigsTypeEnum.BASE);
        return R.ok(config);
    }

    /**
     * 编辑或修改信息
     *
     * @param sysConfigs
     * @return
     */
    @Log
    @PostMapping
    @ApiOperation(value = "保存平台基础配置信息")
    public R<SysConfigs> edit(@RequestBody SysConfigs sysConfigs) {
        if (UserCurrentUtils.getCurrentUser().getAdminFlag()) {
            //主动设置终端信息
            sysConfigs.setConfigLevel(TypeEnum.platform).setType(ConfigsTypeEnum.BASE);
            sysConfigService.saveOrUpdate(sysConfigs);
        } else {
            return R.failed("租户管理员才能修改");
        }
        return R.ok();
    }

    @ApiOperation(value = "获取开发配置")
    @GetMapping("/dev")
    public R<Map<ConfigsTypeEnum, SysConfigs>> dev() {
        Map<ConfigsTypeEnum, SysConfigs> configMap = new HashMap<>();
        TenantContextHolder.clear();
        configMap.put(ConfigsTypeEnum.AUTO_CREATE_USER_HEAD_IMG, sysConfigService.getOne(Wrappers.query(new SysConfigs().setType(ConfigsTypeEnum.AUTO_CREATE_USER_HEAD_IMG))));
        configMap.put(ConfigsTypeEnum.ERROR_MESSAGE_SEND_DING, sysConfigService.getOne(Wrappers.query(new SysConfigs().setType(ConfigsTypeEnum.ERROR_MESSAGE_SEND_DING))));
        return R.ok(configMap);
    }

    /**
     * 获取平台信息
     */
    @GetMapping
    public R<PlatformDto> info() {
        PlatformConfig config = sysConfigService.getConfig(ConfigsTypeEnum.BASE);
        PlatformDto platformDto = new PlatformDto();
        platformDto.setVersion(SpringContextUtil.getVersion());
        platformDto.setName(config.getName());
        platformDto.setDateTime(SpringContextUtil.getDate());
        platformDto.setServicesCluster(discoveryClient.getInstances(SpringContextUtil.getApplicationContextName()).size() > 1);
        String collect = redisUtils.sGet("jvs:device").stream().map(String::valueOf).collect(Collectors.joining("_"));
        platformDto.setDevices(collect);

        List<PlatformDto.Block> list = new ArrayList<>();
        list.add(new PlatformDto.Block().setName(SpringContextUtil.msg("终端个数")).setSize(applyService.count()));
        list.add(new PlatformDto.Block().setName(SpringContextUtil.msg("平台人数")).setSize(userService.count()));
        TenantContextHolder.clear();
        list.add(new PlatformDto.Block().setName(SpringContextUtil.msg("附件总数")).setSize(fileMapper.selectCount(Wrappers.query())));
        platformDto.setList(list);
        return R.ok(platformDto);
    }

}
