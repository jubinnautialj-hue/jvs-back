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
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
        if (ObjectNull.isNotNull(dataModelDto.getGroupBy())) {
            if (dataModelDto.getGroupBy() instanceof List) {
                long count = ((List<?>) dataModelDto.getGroupBy()).stream().filter(ObjectNull::isNotNull).count();
                if (count != 0) {
                    if (dataModelDto.getGroupBy() instanceof String) {
                        Fields from = Fields.from(Fields.field((String) dataModelDto.getGroupBy()));
                        return dynamicDataService.aggregate(criteria, dataModelDto.getDataModelId(), dataModelDto.getType(), dataModelDto.getFields(), from);
                    } else if (dataModelDto.getGroupBy() instanceof List) {
                        String[] a = new String[((JSONArray) dataModelDto.getGroupBy()).size()];
                        String[] array = ((JSONArray) dataModelDto.getGroupBy()).toArray(a);
                        Fields from = Fields.fields(array);
                        return dynamicDataService.aggregate(criteria, dataModelDto.getDataModelId(), dataModelDto.getType(), dataModelDto.getFields(), from);
                    }
                }
            }
        }
        List aggregate = dynamicDataService.aggregate(criteria, dataModelDto.getDataModelId(), dataModelDto.getType(), dataModelDto.getFields(), null);
        if (ObjectNull.isNotNull(aggregate)) {
            Map<String, Object> o = (Map<String, Object>) aggregate.get(0);
            Object o1 = o.get("value");
            return o1;
        } else {
            return 0;
        }
    }

    @Override
    public void inspect(DataModelAggregateDto o) {
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
