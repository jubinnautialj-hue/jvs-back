package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
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
 * 应用模板——自定义设计标识符
 */
@Service
@AllArgsConstructor
public class IdentificationAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<Identification> {
    private final IdentificationService identificationService;

    @Override
    public List<Identification> list(String jvsAppId, List<String> ids) {
        List<Identification> identificationList = identificationService.list(Wrappers.query(new Identification().setJvsAppId(jvsAppId)));
        List<String> collect = identificationList.stream().peek(this::clearDefaultData).map(Identification::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return identificationList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        deleteByAppId(identificationService, Identification::getJvsAppId, jvsApp.getId());
        // 新增
        List<Identification> identifications = templateBo.getIdentifications();
        if (ObjectNull.isNull(identifications)) {
            return;
        }
        // 清空默认数据
        identifications.forEach(i -> {
            clearDefaultData(i);
            i.setTenantId(null);
        });
        saveBatch(identificationService, identifications);
    }
}
