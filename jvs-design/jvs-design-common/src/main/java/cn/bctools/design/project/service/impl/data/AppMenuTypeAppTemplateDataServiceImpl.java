package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——目录
 */
@Service
@AllArgsConstructor
public class AppMenuTypeAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<AppMenuType> {
    private final AppMenuTypeService appMenuTypeService;

    @Override
    public List<AppMenuType> list(String jvsAppId, List<String> ids) {
        List<AppMenuType> appMenuTypesList = appMenuTypeService.list(Wrappers.query(new AppMenuType().setJvsAppId(jvsAppId)));
        List<String> collect = appMenuTypesList.stream().map(AppMenuType::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return appMenuTypesList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(appMenuTypeService, existsIds, targetVersionTemplateBo.getAppMenuTypes(), AppMenuType::getId);
        // 新增或修改
        List<AppMenuType> appMenuTypes = templateBo.getAppMenuTypes();
        if (ObjectNull.isNull(appMenuTypes)) {
            return;
        }
        appMenuTypes.forEach(e -> setAppVersion(e, AppMenuType::setAppVersion, targetAppVersion));

        saveOrUpdate(appMenuTypeService, existsIds, appMenuTypes, AppMenuType::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        appMenuTypeService.update(Wrappers.<AppMenuType>lambdaUpdate().eq(AppMenuType::getJvsAppId, appId).set(AppMenuType::getAppVersion, appVersion));
    }
}
