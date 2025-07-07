package cn.bctools.design.project;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.jvslog.entity.JvsLog;
import cn.bctools.design.jvslog.service.JvsLogService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.project.handler.UpgradeFeatureVersionHandler;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppTemplateService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Slf4j
@Api(tags = "应用管理")
@RestController
@AllArgsConstructor
@RequestMapping("/app/manage/{appId}")
public class JvsAppController {
    AuthRoleServiceApi roleServiceApi;
    JvsAppService service;
    DesignHandler designHandler;
    JvsAppTemplateService templateService;
    JvsLogService jvsLogService;
    RedisUtils redisUtils;
    CrudPageService pageService;
    FormService formService;
    AppUrlService appUrlService;
    AppMenuTypeService appMenuTypeService;
    AppMenuService appMenuService;
    UpgradeFeatureVersionHandler upgradeFeatureVersionHandler;
    JvsAppVersionService appVersionService;
    SwaggerRuleApiCacheService swaggerRuleApiCacheService;

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-发布")
    @PutMapping("/deploy")
    public R<Boolean> deploy(@PathVariable("appId") String appId) {
        service.deploy(appId);
        return R.ok(true);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-卸载")
    @PutMapping("/unload")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> unload(@PathVariable("appId") String appId) {
        JvsApp byId = service.getById(appId);
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId).setJvsAppName(byId.getName()));
        service.unload(appId);
        return R.ok(true);
    }

    @Log
    @ApiOperation("应用-详情")
    @GetMapping("/detail")
    public R<JvsApp> detail(@PathVariable String appId) {
        JvsApp one = service.getById(appId);
        one.setSecret(null);
        return R.ok(one);
    }

    @Log
    @ApiOperation("修改详细描述")
    @PutMapping("/text")
    public R<JvsApp> text(@PathVariable String appId, @RequestBody Map<String, String> body) {
        JvsApp byId = service.getById(appId);
        byId.setLongText(body.getOrDefault("body", ""));
        service.updateById(byId);
        return R.ok(byId);
    }

    @Log
    @ApiOperation("应用-获取密钥")
    @GetMapping("/secret")
    public R<String> secret(@PathVariable("appId") String appId) {
        JvsApp jvsApp = service.getById(appId);
        return R.ok(jvsApp.getSecret());
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-修改")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/edit")
    public R<JvsApp> edit(@RequestBody JvsApp jvsApp, @PathVariable String appId) {
        JvsApp app = service.getAppById(appId);
        if (ObjectNull.isNull(app)) {
            return R.ok();
        }
        // true-应用名变更
        boolean changeAppName = !app.getName().equals(jvsApp.getName());
        jvsApp.setId(appId);
        // 主应用管理员至少有一个
        if (ObjectNull.isNotNull(jvsApp.getRole()) && Optional.ofNullable(jvsApp.getRole().getAdminMember()).orElseGet(ArrayList::new).size() == 0) {
            throw new BusinessException("至少保留一个应用主管理员");
        }
        service.updateById(jvsApp);
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(jvsApp.getId()).setJvsAppName(jvsApp.getName()));
        // 发布逻辑API swagger缓存变更事件
        if (changeAppName) {
            swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(false, appId);
        }
        return R.ok(jvsApp);
    }

    @Log
    @ApiOperation("应用-日志")
    @GetMapping("/jvsLog/page")
    public R<Page<JvsLog>> page(Page<JvsLog> page, JvsLog dto, @PathVariable String appId) {
        jvsLogService.page(page, Wrappers.query(dto.setJvsAppId(appId)));
        return R.ok(page);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-删除")
    @DeleteMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> remove(@PathVariable("appId") String appId, String appName) {
        JvsApp jvsApp = Optional.ofNullable(service.getById(appId)).orElseThrow(() -> new BusinessException("应用不存在"));
        if (Boolean.FALSE.equals(jvsApp.getName().equals(appName))) {
            return R.failed("应用名称不匹配无法删除");
        }
        String userId = UserCurrentUtils.getUserId();
        boolean withPermission = userId.equals(jvsApp.getCreateById()) ||
                UserCurrentUtils.getCurrentUser().getAdminFlag() ||
                (ObjectNull.isNotNull(jvsApp.getRole()) && Optional.ofNullable(jvsApp.getRole().getAdminMember())
                        .orElseGet(ArrayList::new)
                        .stream()
                        .anyMatch(m -> userId.equals(m.getId())));
        if (Boolean.FALSE.equals(withPermission)) {
            return R.failed("没有权限请联系管理员");
        }
        // 清空上下文中的应用信息，否则有应用版本号的表会自动加上应用版本号作为条件
        CurrentAppUtils.clear();
        // 找到应用版本的关联应用，一并删除
        String affiliationAppByAppId = appVersionService.getAffiliationAppByAppId(appId);
        List<JvsAppVersion> versionApps = new ArrayList<>(appVersionService.list(Wrappers.<JvsAppVersion>lambdaQuery()
                        .eq(JvsAppVersion::getAffiliationApp, affiliationAppByAppId))
                .stream()
                .collect(Collectors.toMap(JvsAppVersion::getJvsAppId, Function.identity(), (v1, v2) -> v1))
                .values());

        if (ObjectNull.isNull(versionApps)) {
            versionApps.add(new JvsAppVersion().setJvsAppId(appId).setVersionType(AppVersionTypeEnum.GA));
        }
        versionApps.forEach(app -> service.removeById(app.getJvsAppId()));
        designHandler.appDataDeleted(versionApps);
        // 发布逻辑API swagger缓存变更事件
        swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(true, appId);
        return R.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-功能升级[轻应用版本迭代]")
    @PutMapping("/upgrade/feature/version")
    public R<Boolean> upgradeFeatureVersion(@PathVariable("appId") String appId) {
        upgradeFeatureVersionHandler.upgrade(appId);
        return R.ok(true);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("应用-推荐")
    @PutMapping("/recommendation")
    public R<Boolean> recommend(@PathVariable("appId") String appId, Boolean recommend) {
        service.update(Wrappers.<JvsApp>lambdaUpdate().set(JvsApp::getRecommend, recommend).eq(JvsApp::getId, appId));
        return R.ok(true);
    }
}
