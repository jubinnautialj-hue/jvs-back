package cn.bctools.design.rule.impl.datamodel.select.ids;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "查询多条",
        group = RuleGroup.模型插件,
        order = 8,
        test = true,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.对象,
//        iconUrl = "rule-chaxunsousuo",
        explain = "跳过数据权限，根据id数组查询指定模型多条模型数据"
)
public class DataModelSelectIdsServiceImpl implements BaseCustomFunctionInterface<DataModelSelectIdsDto> {

    DynamicDataService dynamicDataService;
    DataModelService dataModelService;
    DataFieldService dataFieldService;

    @Override
    @SneakyThrows
    public Object execute(DataModelSelectIdsDto dataModelDto, Map<String, Object> params) {
        DynamicDataUtils.freePermit();
        if (ObjectNull.isNull(dataModelDto.getFields())) {
            return dynamicDataService.getByIds(dataModelDto.getDataModelId(), dataModelDto.getIds());
        }
        Set<String> fieldList = new HashSet<>(dataModelDto.getFields());
        String appId = dataModelService.getById(dataModelDto.getDataModelId()).getAppId();
        List<Map> maps = dynamicDataService.getByIds(dataModelDto.getDataModelId(), dataModelDto.getIds(), fieldList);
        List<FieldBasicsHtml> allFieldDefault = dataFieldService.getAllFieldDefault(appId, dataModelDto.getDataModelId(), e -> fieldList.contains(e));
        maps = dynamicDataService.echo(maps, allFieldDefault, false);
        maps.forEach(e -> e.put("modelId", dataModelDto.getDataModelId()));
        return maps;
    }

}
