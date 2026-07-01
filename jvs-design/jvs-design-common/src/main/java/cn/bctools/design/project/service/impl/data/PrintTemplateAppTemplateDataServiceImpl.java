package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.PrintTemplate;
import cn.bctools.design.crud.entity.enums.DesignTypeEnum;
import cn.bctools.design.crud.service.PrintTemplateService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——打印模板
 */
@Service
@AllArgsConstructor
public class PrintTemplateAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<PrintTemplate> {
    private final PrintTemplateService printTemplateService;

    @Override
    public List<PrintTemplate> list(String jvsAppId, List<String> ids) {
        List<PrintTemplate> printTemplateList = printTemplateService.list(Wrappers.<PrintTemplate>lambdaQuery().eq(PrintTemplate::getJvsAppId, jvsAppId));
        if (CollectionUtils.isEmpty(printTemplateList)) {
            return Collections.emptyList();
        }
        List<String> listIds = printTemplateList.stream()
                .peek(this::clearDefaultData)
                .map(PrintTemplate::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return printTemplateList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        List<PrintTemplate> savePrintTemplates = null;
        // 上传离线包，排除word模板（因为离线包上传的，可能不是同一个物理环境，会找不到模板文件）
        VersionIterationTypeEnum type = JvsAppTemplateUtils.getContextVersionIterationType();
        if (VersionIterationTypeEnum.UPLOAD_VERSION.equals(type)) {
            // 排除模板中的word模板，再保存
            List<PrintTemplate> printTemplates = templateBo.getPrintTemplates();
            if (ObjectNull.isNull(printTemplates)) {
                return;
            }
            savePrintTemplates = printTemplates.stream()
                    .filter(printTemplate -> DesignTypeEnum.CUSTOMIZE.equals(printTemplate.getDesignType()))
                    .collect(Collectors.toList());
            // 将已存在的word模板加入版本模板
            List<PrintTemplate> printTemplateList = printTemplateService.list(Wrappers.<PrintTemplate>lambdaQuery()
                    .eq(PrintTemplate::getJvsAppId, jvsApp.getId())
                    .eq(PrintTemplate::getDesignType, DesignTypeEnum.WORD));
            if (ObjectNull.isNotNull(printTemplateList)) {
                savePrintTemplates.addAll(printTemplateList);
            }
            templateBo.setPrintTemplates(savePrintTemplates);
        } else {
            savePrintTemplates = templateBo.getPrintTemplates();
        }
        // 删除
        deleteByAppId(printTemplateService, PrintTemplate::getJvsAppId, jvsApp.getId());
        // 新增
        if (ObjectNull.isNull(savePrintTemplates)) {
            return;
        }
        // 清空默认数据
        savePrintTemplates.forEach(this::clearDefaultData);
        saveBatch(printTemplateService, savePrintTemplates);
    }
}
