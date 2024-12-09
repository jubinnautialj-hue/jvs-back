package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.QueryOrderDto;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import cn.bctools.design.util.ModeUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——动态数据
 */
@Service
@AllArgsConstructor
public class DynamicDataAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DynamicDataPo> {
    private final DynamicDataService dynamicDataService;

    /**
     * 可导出最大数据量
     */
    private static final Integer MAX_DATA_TOTAL = 5000;

    /**
     * 单个模型可导出的最大数据量
     */
    private static final Integer MAX_SINGLE_MODEL_DATA_TOTAL = 100;

    @Override
    public List<DynamicDataPo> list(List<String> modelIds, List<String> ids) {
        if (ObjectNull.isNull(modelIds)) {
            return Collections.emptyList();
        }
        List<DynamicDataPo> dynamicDataPos = new ArrayList<>();
        // 按修改时间倒序查询
        List<QueryOrderDto> orderBys = new ArrayList<>();
        orderBys.add(new QueryOrderDto()
                .setFieldKey(Get.name(DynamicDataPo::getUpdateTime))
                .setDirection(Sort.Direction.DESC));
        for (String modelId : modelIds) {
            // 不替换数据id，直接查询数据
            Page<DynamicDataPo> page = new Page<>(1, MAX_SINGLE_MODEL_DATA_TOTAL);
            Page<Map<String, Object>> mapPage = dynamicDataService.queryPage(null, page, modelId, null, null, orderBys, null, false, false, false, null);
            dynamicDataPos.addAll(mapPage.getRecords().stream().map(dynamicDataService::parseBean).collect(Collectors.toList()));
            if (dynamicDataPos.size() > MAX_DATA_TOTAL) {
                throw new BusinessException("模板导出数据总量不能超过条", MAX_DATA_TOTAL.toString());
            }
        }
        return dynamicDataPos;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        if (ObjectNull.isNull(templateBo.getDynamicDataPos())) {
           return;
        }
        // 版本回退不回退数据
        VersionIterationTypeEnum type = JvsAppTemplateUtils.getContextVersionIterationType();
        if (VersionIterationTypeEnum.SWITCH_VERSION.equals(type)) {
            return;
        }
        List<DynamicDataPo> dynamicDataPos = BeanCopyUtil.copys(templateBo.getDynamicDataPos(), DynamicDataPo.class);
        Map<String, List<DynamicDataPo>> collect = dynamicDataPos.stream()
                .filter(ObjectNull::isNotNull)
                .filter(e -> ObjectNull.isNotNull(e.getModelId()))
                .collect(Collectors.groupingBy(DynamicDataPo::getModelId));
        // 根据目标版本，设置模式上下文，以便将数据集保存到版本对应的数据库下
        AppVersionTypeEnum targetVersionType = targetAppVersion.getVersionType();
        SwitchModeDto currentMode = ModeUtils.getSwitchMode();
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(targetVersionType));
        for (String dataModelId : collect.keySet()) {
            dynamicDataService.removeAll(dataModelId);
            dynamicDataService.saveBatch(collect.get(dataModelId), dataModelId);
        }
        // 恢复模式上下文
        ModeUtils.setSwitchModel(currentMode);
    }
}
