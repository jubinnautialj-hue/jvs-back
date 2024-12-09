package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudAssociationPo;
import cn.bctools.design.crud.service.JvsCrudAssociationService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
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
 * 应用模板——数据操作规则
 */
@Service
@AllArgsConstructor
public class CrudAssociationAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<CrudAssociationPo> {
    private final JvsCrudAssociationService associationService;

    @Override
    public List<CrudAssociationPo> list(String jvsAppId, List<String> ids) {
        List<CrudAssociationPo> associationPoList = associationService.list(Wrappers.query(new CrudAssociationPo().setJvsAppId(jvsAppId)));
        if (CollectionUtils.isEmpty(associationPoList)) {
            return Collections.emptyList();
        }
        List<String> listIds = associationPoList.stream().map(CrudAssociationPo::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return associationPoList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(associationService, existsIds, targetVersionTemplateBo.getCrudAssociationPos(), CrudAssociationPo::getId);
        // 新增或修改
        List<CrudAssociationPo> crudAssociationPos = templateBo.getCrudAssociationPos();
        if (ObjectNull.isNull(crudAssociationPos)) {
            return;
        }
        crudAssociationPos.forEach(e -> setAppVersion(e, CrudAssociationPo::setAppVersion, targetAppVersion));
        saveOrUpdate(associationService, existsIds, crudAssociationPos, CrudAssociationPo::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        associationService.update(Wrappers.<CrudAssociationPo>lambdaUpdate().eq(CrudAssociationPo::getJvsAppId, appId).set(CrudAssociationPo::getAppVersion, appVersion));
    }
}
