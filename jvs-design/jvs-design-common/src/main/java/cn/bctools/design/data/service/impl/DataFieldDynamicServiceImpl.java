package cn.bctools.design.data.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataFieldDynamicPo;
import cn.bctools.design.data.fields.dto.page.DataTableFieldAdvancedSettingsHtml;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import cn.bctools.design.data.fields.dto.page.ModelDisplayHtml;
import cn.bctools.design.data.fields.enums.DisplayShowModelTypeEnum;
import cn.bctools.design.data.mapper.DataFieldDynamicMapper;
import cn.bctools.design.data.service.DataFieldDynamicService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataFieldDynamicServiceImpl extends ServiceImpl<DataFieldDynamicMapper, DataFieldDynamicPo> implements DataFieldDynamicService {

    @Override
    public void saveOrUpdateFields(String appId, List<DataFieldDynamicPo> fields, String modelId, List<String> designId) {
        remove(new LambdaQueryWrapper<DataFieldDynamicPo>().eq(DataFieldDynamicPo::getModelId, modelId).in(DataFieldDynamicPo::getDesignId, designId));
        fields.stream().flatMap(e -> {
            return designId.stream().map(a ->
            {
                return new DataFieldDynamicPo().setDesignId(a).setJvsAppId(appId).setProp(e.getProp()).setLabel(e.getLabel()).setFieldJson(e.getFieldJson()).setModelId(modelId).setType(e.getType());
            });
        }).forEach(this::save);
    }

    @Override
    public boolean deleteFields(String appId, List<String> fields, String modelId, String designId) {
        remove(new LambdaQueryWrapper<DataFieldDynamicPo>().eq(DataFieldDynamicPo::getModelId, modelId).in(DataFieldDynamicPo::getDesignId, designId).in(DataFieldDynamicPo::getProp, fields));
        return true;
    }

    @Override
    public List<DataTableFieldDesignHtml> getPageAutoTableFields(String jvsAppId, String modelId, String designId) {
        List<DataFieldDynamicPo> list = list(new LambdaQueryWrapper<DataFieldDynamicPo>()
                .eq(DataFieldDynamicPo::getModelId, modelId)
                .eq(DataFieldDynamicPo::getDesignId, designId)
                .eq(DataFieldDynamicPo::getJvsAppId, jvsAppId));
        if (ObjectNull.isNull(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(e -> {
            return new DataTableFieldDesignHtml().setAliasColumnName(e.getProp()).setDesignJson(e.getFieldJson()).setComponentType(e.getType()).setSupportShow(true).setShow(true).setShowChinese(e.getLabel());
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map> getFormAutoTableFields(String jvsAppId, String dataModelId, String designId, Set<String> prop) {
        List<DataFieldDynamicPo> list = list(new LambdaQueryWrapper<DataFieldDynamicPo>().eq(DataFieldDynamicPo::getDesignId, designId).eq(DataFieldDynamicPo::getModelId, dataModelId).eq(DataFieldDynamicPo::getJvsAppId,
                jvsAppId));
        if (ObjectNull.isNull(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(e -> !prop.contains(e.getProp()))
                .map(DataFieldDynamicPo::getFieldJson).collect(Collectors.toList());
    }

    @Override
    public void parseModelDisplayFillFields(List<DataTableFieldDesignHtml> autoTableFields) {
        if (ObjectNull.isNull(autoTableFields)) {
            return;
        }
        //  解析显示设置-关联模型，获取关联显示字段。 Map<列表字段key, 待添加到列表的字段集合>
        Map<String, List<DataTableFieldDesignHtml>> modelDisplayFieldDesignHtmlMap = autoTableFields.stream()
                .filter(tableField -> ObjectNull.isNotNull(tableField.getAdvancedSettings()))
                .filter(tableField -> {
                    DataTableFieldAdvancedSettingsHtml advancedSettings = tableField.getAdvancedSettings();
                    ModelDisplayHtml modelDisplay = advancedSettings.getModelDisplay();
                    // 关联模型的关联显示方式为显示到当前列表 && 配置了关联显示字段。 则将显示字段添加到当前列表
                    return ObjectNull.isNotNull(modelDisplay)
                            && DisplayShowModelTypeEnum.oneToOne.equals(modelDisplay.getShowModelType())
                            && ObjectNull.isNotNull(modelDisplay.getLinkageFieldKeys());
                })
                .collect(Collectors.toMap(DataTableFieldDesignHtml::getAliasColumnName, tableField ->
                        tableField.getAdvancedSettings().getModelDisplay().getLinkageFieldKeys()
                                .stream()
                                .map(linkageField -> new DataTableFieldDesignHtml()
                                        .setAliasColumnName(linkageField.getAliasProp())
                                        .setDesignJson(null)
                                        .setComponentType(linkageField.getType())
                                        .setSupportShow(true)
                                        .setShow(true)
                                        .setShowChinese(linkageField.getLabel())
                                        .setAdvancedSettings(linkageField.getAdvancedSettings())
                                )
                                .collect(Collectors.toList())
                ));

        // 填充到列表页设计
        if (ObjectNull.isNull(modelDisplayFieldDesignHtmlMap)) {
            return;
        }
        modelDisplayFieldDesignHtmlMap.forEach((pageFieldKey, fillField) -> {
            OptionalInt optionalInt = IntStream.range(0, autoTableFields.size())
                    .filter(i -> pageFieldKey.equals(autoTableFields.get(i).getAliasColumnName()))
                    .findFirst();
            if (optionalInt.isPresent()) {
                autoTableFields.addAll(optionalInt.getAsInt() + 1, fillField);
            }
        });
    }

}

