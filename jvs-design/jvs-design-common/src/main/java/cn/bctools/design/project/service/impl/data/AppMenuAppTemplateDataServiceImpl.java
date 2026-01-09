package cn.bctools.design.project.service.impl.data;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——菜单
 */
@Service
@AllArgsConstructor
public class AppMenuAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<AppMenu> {
    private final AppMenuService appMenuService;

    /**
     * 不包含在模板中的设计类型
     */
    private static final List<DesignType> EXCLUDE_DESIGN_TYPE_LIST = Arrays.asList(DesignType.chart, DesignType.report);

    @Override
    public List<AppMenu> list(JvsApp jvsApp, List<String> ids) {
        List<AppMenu> appMenuList = appMenuService.list(Wrappers.query(new AppMenu().setJvsAppId(jvsApp.getId())))
                .stream()
                // 图表、报表不进模板
                .filter(m -> Boolean.FALSE.equals(EXCLUDE_DESIGN_TYPE_LIST.contains(m.getDesignType())))
                .peek(e -> {
                    // 未启用轻应用版本功能，设置默认权限（兼容旧权限功能）
                    if (Boolean.FALSE.equals(jvsApp.getEnableVersionFeature())) {
                        DesignRole role = new DesignRole().setPersonType(PersonnelTypeEnum.all);
                        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(role));
                        e.setRole(JSONArray.of(jsonObject)).setRoleType(true);
                    }
                    // 清空默认数据
                    clearDefaultData(e);
                })
                .collect(Collectors.toList());
        List<String> collect = appMenuList.stream().map(AppMenu::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return appMenuList;
    }

    @Override
    public List<AppMenu> list(String jvsAppId, List<String> ids) {
        List<AppMenu> appMenuList = appMenuService.list(Wrappers.query(new AppMenu().setJvsAppId(jvsAppId)))
                .stream()
                // 排除图表、报表
                .filter(m -> Boolean.FALSE.equals(EXCLUDE_DESIGN_TYPE_LIST.contains(m.getDesignType())))
                .collect(Collectors.toList());
        List<String> collect = appMenuList.stream().map(AppMenu::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return appMenuList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(appMenuService, existsIds, targetVersionTemplateBo.getAppMenus(), AppMenu::getId);

        // 新增或修改
        Map<String, String> pageViewJsonMap = Optional.ofNullable(templateBo.getCrudPageList()).orElseGet(ArrayList::new).stream()
                .filter(p -> ObjectNull.isNotNull(p.getViewJson()))
                .collect(Collectors.toMap(CrudPage::getId, CrudPage::getViewJson));
        Map<String, String> formViewJsonMap = Optional.ofNullable(templateBo.getFormPoList()).orElseGet(ArrayList::new).stream()
                .filter(f -> ObjectNull.isNotNull(f.getViewJson()))
                .collect(Collectors.toMap(FormPo::getId, FormPo::getViewJson));
        List<AppMenu> newAppMenus = templateBo.getAppMenus();
        if (ObjectNull.isNull(newAppMenus)) {
            return;
        }
        newAppMenus.forEach(e -> {
            // 设置版本号
            setAppVersion(e, AppMenu::setAppVersion, targetAppVersion);
            // 兼容新旧版权限功能
            if (Boolean.TRUE.equals(jvsApp.getEnableVersionFeature())) {
                if (ObjectNull.isNull(e.getPermission())) {
                    String designId = e.getDesignId();
                    if (DesignType.page.equals(e.getDesignType())) {
                        e.setPermission(DesignPermissionUtil.parseDesign(e.getDesignType(), pageViewJsonMap.get(designId)));
                    }
                    if (DesignType.form.equals(e.getDesignType())) {
                        e.setPermission(DesignPermissionUtil.parseDesign(e.getDesignType(), formViewJsonMap.get(designId)));
                    }
                }
            } else {
                if ("[]".equals(e.getRole().toString())) {
                    //兼容体验默认权限数据
                    e.setRole(JSONArray.parseArray("[{\"operation\": [], \"scopeList\": [], \"personType\": \"all\", \"personnels\": [], \"conditionList\": [], \"treeOperation\": []}]"));
                }
            }
            // 清空默认数据
            clearDefaultData(e);
        });

        saveOrUpdate(appMenuService, existsIds, newAppMenus, AppMenu::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        appMenuService.update(Wrappers.<AppMenu>lambdaUpdate().eq(AppMenu::getJvsAppId, appId).set(AppMenu::getAppVersion, appVersion));
    }
}
