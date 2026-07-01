package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataIdPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataIdService;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——模型数据自增id
 */
@Service
@AllArgsConstructor
public class DataIdAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DataIdPo> {
    private final DataIdService dataIdService;

    @Override
    public List<DataIdPo> list(List<String> modelIds, List<String> ids) {
        if (ObjectNull.isNull(modelIds)) {
            return Collections.emptyList();
        }
        dataIdService.syncUpdateDataId(DesignType.data, modelIds);
        return dataIdService.list(new LambdaQueryWrapper<DataIdPo>().in(DataIdPo::getModelId, modelIds));
    }

    @Override
    public void save(List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 版本回退不回退数据自增id
        VersionIterationTypeEnum type = JvsAppTemplateUtils.getContextVersionIterationType();
        if (VersionIterationTypeEnum.SWITCH_VERSION.equals(type)) {
            return;
        }
        List<DataIdPo> dataIdPos = templateBo.getDataIdPoList();
        if (ObjectNull.isNull(dataIdPos)) {
            return;
        }
        Set<String> modelIds = dataIdPos.stream()
                .map(dataIdPo -> dataIdPo.setId(null))
                .map(DataIdPo::getModelId)
                .collect(Collectors.toSet());
        // 先删除模型自增id
        dataIdService.removeId(DesignType.data, modelIds);
        // 保存新的自增id
        dataIdService.saveBatch(dataIdPos);
    }
}
