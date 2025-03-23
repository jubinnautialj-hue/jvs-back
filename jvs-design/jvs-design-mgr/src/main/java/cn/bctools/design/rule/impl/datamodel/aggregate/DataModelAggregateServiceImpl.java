package cn.bctools.design.rule.impl.datamodel.aggregate;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 * The type Data model aggregate service.
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "模型聚合统计",
        group = RuleGroup.模型插件,
        test = true,
        order = 10,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
//        iconUrl = "rule-cishupanduan",
        explain = "跳过数据权限，根据条件对指定模型数据进行聚合统计"
)
public class DataModelAggregateServiceImpl implements BaseCustomFunctionInterface<DataModelAggregateDto> {

    /**
     * The Dynamic data service.
     */
    DynamicDataService dynamicDataService;

    @Override
    public Object execute(DataModelAggregateDto dataModelDto, Map<String, Object> params) {
        DynamicDataUtils.freePermit();
        List<QueryConditionDto> queryConditions = dataModelDto.getBody();
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        return dynamicDataService.aggregate(criteria, dataModelDto.getDataModelId(), dataModelDto.getType(), dataModelDto.getGroupBy(), dataModelDto.getFields());
    }

    @Override
    public void inspect(DataModelAggregateDto o) {
        if (ObjectNull.isNull(o.getBody())) {
            throw new BusinessException("查询条件不能为空");
        }
        //并校验条件是否为空，如果为空，则查询条件不满足返回异常
        o.getBody().forEach(e -> {
            if (ObjectNull.isNull(e.getValue())) {
                throw new BusinessException(e.getFieldKey() + "查询条件为空");
            }
        });
    }

    @Override
    public void removeKey(Map<String, Object> body) {
        //不做清空操作。直接
    }
}
