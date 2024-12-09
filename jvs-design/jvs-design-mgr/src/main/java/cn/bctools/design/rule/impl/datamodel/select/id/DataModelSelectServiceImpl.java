package cn.bctools.design.rule.impl.datamodel.select.id;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.rule.impl.datamodel.FieldStructureUtils;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "查询单条",
        group = RuleGroup.模型插件,
        order = 4,
        test = true,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.对象,
//        iconUrl = "rule-chaxunsousuo",
        explain = "跳过数据权限，根据查询条件指定数据模型查询单条，结果存在多条时会执行异常"
)
public class DataModelSelectServiceImpl implements BaseCustomFunctionInterface<DataModelSelectDto> {

    DynamicDataService dynamicDataService;
    DataFieldService fieldService;
    DataModelService dataModelService;


    @Override
    @SneakyThrows
    public Object execute(DataModelSelectDto dataModelDto, Map<String, Object> params) {
        DynamicDataUtils.freePermit();

        String dataModelId = dataModelDto.getDataModelId();
        List<String> fieldList = BeanCopyUtil.copys(dataModelDto.getFields(), String.class);
        //默认添加查询模型id值
        fieldList.add("modelId");
        List<QueryConditionDto> queryConditions = dataModelDto.getBody();
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        List<Map<String, Object>> maps = dynamicDataService.queryList(dataModelId, criteria, fieldList);
        if (ObjectNull.isNotNull(maps)) {
            maps.forEach(e -> e.put("modelId", dataModelId));
            if (maps.size() == 1) {
                return maps.get(0);
            }
            throw new BusinessException("查询数据不唯一");
        }
        return new HashMap<>(1);
    }

    /**
     * 自定义参数结构，可用于下级节点选择结构
     */
    @Override
    public List<RuleElementVo> structureType(DataModelSelectDto o) {
        if (ObjectNull.isNull(o.getFields())) {
            return null;
        }
        return FieldStructureUtils.transform(o.getDataModelId(), o.getFields(), dataModelService, fieldService);
    }

}
