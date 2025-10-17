package cn.bctools.design.rule.impl.datamodel.list;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.rule.impl.datamodel.FieldStructureUtils;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "查询所有",
        group = RuleGroup.模型插件,
        test = true,
        order = 5,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
        explain = "跳过数据权限, 根据查询条件查询指定模型数据"
)
public class DataModelListServiceImpl implements BaseCustomFunctionInterface<DataModelListDto> {

    DynamicDataService dynamicDataService;
    DataFieldService fieldService;
    DataModelService dataModelService;


    @Override
    @SneakyThrows
    public Object execute(DataModelListDto dataModelDto, Map<String, Object> params) {
        String dataModelId = dataModelDto.getDataModelId();
        DynamicDataUtils.freePermit();
        List<String> fieldList = dataModelDto.getFields();
        List<QueryConditionDto> queryConditions = dataModelDto.getBody();
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        Sort sort;
        if (ObjectNull.isNull(dataModelDto.getOrderBy())) {
            sort = Sort.by(Get.name(DynamicDataPo::getCreateTime)).descending();
        } else {
            List<Sort.Order> collect = dataModelDto.getOrderBy().stream().map(e -> new Sort.Order(e.getDirection(), e.getFieldKey())).collect(Collectors.toList());
            sort = Sort.by(collect);
        }
        LOG.info("查询条件" + dataModelId + "," + ModeUtils.getMode() + " ," + JSONObject.toJSONString(dataModelDto));
        List<Map<String, Object>> collect = dynamicDataService.queryList(dataModelId, criteria, fieldList, sort, dataModelDto.getTop())
                .stream()
                .peek(e -> e.put("modelId", dataModelId))
                .collect(Collectors.toList());
        List<FieldBasicsHtml> dataFieldList = fieldService.getFields(dataModelId, dataModelId, true, true);
        collect = collect.stream().map(e -> dynamicDataService.echo(e, dataFieldList, false)).collect(Collectors.toList());
        return collect;
    }

    /**
     * 自定义参数结构，可用于下级节点选择结构
     */
    @Override
    public List<RuleElementVo> structureType(DataModelListDto o) {
        if (ObjectNull.isNull(o)) {
            return null;
        }
        if (ObjectNull.isNull(o.getFields())) {
            return null;
        }
        return FieldStructureUtils.transform(o.getDataModelId(), o.getFields(), dataModelService, fieldService);
    }

    @Override
    public void inspect(DataModelListDto o) {
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
