package cn.bctools.design.crud;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.AppUrlPo;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.dto.PermissionEndpoint;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


/**
 * @author jvs
 * 自定义页面支持两种 方式 ,独立项目, 和融合项目,将项目内容放一起处理
 */
@Tag(name = "自定义外部页面")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/jvsAppUrl")
public class AppUrlController {

    AppUrlService appUrlService;
    AppMenuService appMenuService;
    AppMenuHandler appMenuHandler;
    JvsAppService jvsAppService;
    IdentificationService identificationService;

    @SneakyThrows
    @PostMapping("/permission")
    @Operation(summary = "加载权限资源")
    public R<PermissionIdentificationDto> permission(@RequestBody AppUrlPo design, @PathVariable String appId, @RequestHeader("referer") String referer) {
        Identification one = identificationService.getOne(Wrappers.query(new Identification().setJvsAppId(appId).setDesignType(DesignType.permission)));
        String path = "";
        if (ObjectNull.isNotNull(one)) {
            try {
                URI uri = new URI(design.getUrl());
                if (new URI(one.getIdentifier()).getAuthority().equals(uri.getAuthority())) {
                    //表示一致，需要加载这个对应的路径的权限
                    path = uri.getPath();
                }
            } catch (Exception e) {

            }
        }
        if (design.getUrl().startsWith("/app/source")) {
            path = design.getUrl();
        }
        if (ObjectNull.isNotNull(path)) {
            try {
                //获取权限
                String permission;
                Identification app = identificationService.getOne(Wrappers.query(new Identification().setJvsAppId(appId).setDesignType(DesignType.app)));
                if (ObjectNull.isNotNull(one) && one.getIdentifier().startsWith("http")) {
                    //直接获取数据内容
                    try {
                        permission = HttpUtil.get(one.getIdentifier(), 200);
                    } catch (Exception e) {
                        throw new BusinessException("权限标识地址请求失败");
                    }
                } else {
                    URL url = new URL(referer);
                    //根据地址加载对应的权限有哪些
                    String json = url.getProtocol() + "://" + url.getAuthority() + "/app/source/" + app.getIdentifier() + ".json";
                    //获取权限
                    permission = HttpUtil.get(json, 200);
                }
                HashMap<String, Object> permissionMap = null;
                try {
                    permissionMap = JSONObject.parseObject(permission, HashMap.class);
                } catch (Exception e) {
                    throw new BusinessException("权限标识不符合要求");
                }
                String finalPath = path;
                Optional<String> first = permissionMap.keySet().stream().filter(e -> e.endsWith(finalPath)).findFirst();
                if (first.isPresent()) {
                    JSONArray value = (JSONArray) permissionMap.get(first.get());
                    List<PermissionEndpoint> list = BeanCopyUtil.copys(value, PermissionEndpoint.class);
                    list.add(0, new PermissionEndpoint().setName("查看").setPermission(design.getName()));
                    PermissionIdentificationDto permissionIdentificationDto = new PermissionIdentificationDto().setUrlOperation(list);
                    return R.ok(permissionIdentificationDto);
                }
            } catch (BusinessException e) {
                return R.failed(e.getMessage());
            } catch (MalformedURLException e) {

            }
        }
        return R.ok();
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Operation(summary = "新增")
    @PostMapping
    public R<AppUrlPo> create(@RequestBody AppUrlPo design, @PathVariable String appId, @RequestHeader("referer") String referer) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        design.setJvsAppId(appId);
        appUrlService.save(design);
        appMenuHandler.addMenu(DesignType.URL, design.getId(), design.getJvsAppId(), null, design.getName(), design.getType());
        PermissionIdentificationDto permissionIdentificationDto = permission(design, appId, referer).getData();
        if (ObjectNull.isNotNull(permissionIdentificationDto)) {
            //初始化权限
            AppMenu appMenu = appMenuService.getOne(Wrappers.<AppMenu>lambdaQuery()
                    .eq(AppMenu::getDesignId, design.getId())
                    .eq(AppMenu::getJvsAppId, appId));
            //其它自定义页面不初始化权限
            appMenu.setPermission(permissionIdentificationDto)
                    .setName(design.getName())
                    .setType(design.getType());
            appMenuService.update(appMenu);
        }
        return R.ok(design);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Operation(summary = "更新")
    @PutMapping
    public R<AppUrlPo> update(@RequestBody AppUrlPo design, @PathVariable String appId, @RequestHeader("referer") String referer) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        design.setJvsAppId(appId);
        AppUrlPo one = appUrlService.getOne(Wrappers.query(new AppUrlPo().setId(design.getId()).setJvsAppId(appId)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        appUrlService.updateById(design);
        PermissionIdentificationDto permissionIdentificationDto = permission(design, appId, referer).getData();
        if (ObjectNull.isNotNull(permissionIdentificationDto)) {
            //初始化权限
            AppMenu appMenu = appMenuService.getOne(Wrappers.<AppMenu>lambdaQuery()
                    .eq(AppMenu::getDesignId, design.getId())
                    .eq(AppMenu::getJvsAppId, appId));
            //其它自定义页面不初始化权限
            appMenu.setPermission(permissionIdentificationDto)
                    .setName(design.getName())
                    .setType(design.getType());
            appMenuService.update(appMenu);
        } else {
            appMenuService.updateType(design.getJvsAppId(), design.getId(), design.getType(), design.getName());
        }
        return R.ok(design);
    }

}
