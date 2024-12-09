package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.external.service.ExternalPageService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——外部页面配置
 */
@Service
@AllArgsConstructor
public class ExternalPageAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<ExternalPage> {
    private final ExternalPageService externalPageService;

    @Override
    public List<ExternalPage> list(String jvsAppId, List<String> ids) {
        List<ExternalPage> externalPageList = externalPageService.list(Wrappers.<ExternalPage>lambdaQuery().eq(ExternalPage::getJvsAppId, jvsAppId));
        if (CollectionUtils.isEmpty(externalPageList)) {
            return Collections.emptyList();
        }
        List<String> listIds = externalPageList.stream()
                .peek(this::clearDefaultData)
                .map(ExternalPage::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return externalPageList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        deleteByAppId(externalPageService, ExternalPage::getJvsAppId, jvsApp.getId());
        // 新增
        List<ExternalPage> externalPages = templateBo.getExternalPages();
        if (ObjectNull.isNull(externalPages)) {
            return;
        }
        // 清空默认数据
        externalPages.forEach(this::clearDefaultData);
        saveBatch(externalPageService, externalPages);
    }
}
