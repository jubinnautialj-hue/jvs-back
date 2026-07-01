package cn.bctools.design.rule.impl.datamodel;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.common.RuleElementVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wl
 */
public class FieldStructureUtils {

    public static List<RuleElementVo> transform(String dataModelId, List<String> fields, DataModelService dataModelService, DataFieldService fieldService) {
        DataModelPo model = dataModelService.getModel(dataModelId);
        //根据模型ID获取字段结构
        List<RuleElementVo> collect = fieldService.getAllField(model.getAppId(), dataModelId)
                .stream()
                //必须是包含关系才显示 ， 其它字段不显示在此
                .filter(e -> {
                    if (ObjectNull.isNotNull(fields)) {
                        return fields.contains(e.getFieldKey());
                    }
                    return true;
                })
                .map(e -> new RuleElementVo(e.getFieldKey(), e.getFieldName(), JvsParamType.getByClass(e.getType().getAClass())))
                .collect(Collectors.toList());
        collect.add(0, new RuleElementVo().setName("id").setName("id").setInfo("id").setJvsParamType(JvsParamType.text));
        return collect;

    }

}
