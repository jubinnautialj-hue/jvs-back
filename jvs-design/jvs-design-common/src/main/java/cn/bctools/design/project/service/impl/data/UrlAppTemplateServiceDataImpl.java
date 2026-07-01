package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.AppUrlPo;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Service
@AllArgsConstructor
public class UrlAppTemplateServiceDataImpl extends AppTemplateDataBase implements AppTemplateDataService<AppUrlPo> {
    private final AppUrlService appUrlService;

    @Override
    public List<AppUrlPo> list(String jvsAppId, List<String> ids) {
        List<AppUrlPo> list = appUrlService.list(Wrappers.query(new AppUrlPo().setJvsAppId(jvsAppId)));
        List<String> listIds = list.stream().peek(this::clearDefaultData).map(AppUrlPo::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return list;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        deleteByAppId(appUrlService, AppUrlPo::getJvsAppId, jvsApp.getId());
        // 新增
        List<AppUrlPo> appUrlPos = templateBo.getAppUrlPos();
        if (ObjectNull.isNull(appUrlPos)) {
            return;
        }
        // 清空默认数据
        appUrlPos.forEach(this::clearDefaultData);
        saveBatch(appUrlService, appUrlPos);
    }
}
