package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
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
 * 应用模板——表单设计
 */
@Service
@AllArgsConstructor
public class FormAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<FormPo> {
    private final FormService formService;

    @Override
    public List<FormPo> list(String jvsAppId, List<String> ids) {
        List<FormPo> formList = formService.list(Wrappers.query(new FormPo().setJvsAppId(jvsAppId)))
                .stream()
                .peek(e -> {
                    e.setIsDeploy(true);
                    // 清空默认数据
                    clearDefaultData(e);
                })
                .collect(Collectors.toList());
        List<String> collect = formList.stream().map(FormPo::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return formList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(formService, existsIds, targetVersionTemplateBo.getFormPoList(), FormPo::getId);
        // 新增或修改设计
        List<FormPo> formPoList = templateBo.getFormPoList();
        if (ObjectNull.isNull(formPoList)) {
           return;
        }
        // 清空默认数据
        formPoList.forEach(e-> {
            clearDefaultData(e);
            // 设置版本号
            setAppVersion(e, FormPo::setAppVersion, targetAppVersion);
        });
        saveOrUpdate(formService, existsIds, formPoList, FormPo::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        formService.update(Wrappers.<FormPo>lambdaUpdate().eq(FormPo::getJvsAppId, appId).set(FormPo::getAppVersion, appVersion));
    }
}
