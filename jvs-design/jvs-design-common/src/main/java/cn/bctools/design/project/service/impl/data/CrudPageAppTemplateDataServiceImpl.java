package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.service.CrudPageService;
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
 * 应用模板——列表页设计
 */
@Service
@AllArgsConstructor
public class CrudPageAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<CrudPage> {
    private final CrudPageService crudPageService;

    @Override
    public List<CrudPage> list(String jvsAppId, List<String> ids) {
        List<CrudPage> pageList = crudPageService.list(Wrappers.query(new CrudPage().setJvsAppId(jvsAppId)))
                .stream()
                .peek(e -> {
                    e.setIsDeploy(true);
                    // 清空默认数据
                    clearDefaultData(e);
                })
                .collect(Collectors.toList());
        List<String> collect = pageList.stream().map(CrudPage::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return pageList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(crudPageService, existsIds, targetVersionTemplateBo.getCrudPageList(), CrudPage::getId);
        // 新增或修改设计
        List<CrudPage> crudPageList = templateBo.getCrudPageList();
        if (ObjectNull.isNull(crudPageList)) {
            return;
        }
        crudPageList.forEach(e -> {
            e.setIsDeploy(true);
            // 清空默认数据
            clearDefaultData(e);
            // 设置版本号
            setAppVersion(e, CrudPage::setAppVersion, targetAppVersion);
        });
        saveOrUpdate(crudPageService, existsIds, crudPageList, CrudPage::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        crudPageService.update(Wrappers.<CrudPage>lambdaUpdate().eq(CrudPage::getJvsAppId, appId).set(CrudPage::getAppVersion, appVersion));
    }
}
