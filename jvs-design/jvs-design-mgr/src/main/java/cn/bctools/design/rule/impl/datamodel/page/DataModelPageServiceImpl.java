package cn.bctools.design.rule.impl.datamodel.page;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "分页查询",
        group = RuleGroup.模型插件,
        test = true,
        order = 7,
        testShowEnum = TestShowEnum.JSON,
        customStructure = true,
        returnType = ClassType.对象,
//        iconUrl = "rule-page_icon",
        explain = "数据模型分页查询"
)
public class DataModelPageServiceImpl implements BaseCustomFunctionInterface<DataModelPageDto> {

    DynamicDataService dynamicDataService;
    DataModelService dataModelService;
    DataFieldService fieldService;


    /**
     * 自定义参数结构，可用于下级节点选择结构
     */
    @Override
    public List<RuleElementVo> structureType(DataModelPageDto o) {
        if (ObjectNull.isNull(o.getFields())) {
            return null;
        }
        return FieldStructureUtils.transform(o.getDataModelId(), o.getFields(), dataModelService, fieldService);
    }

    @Override
    @SneakyThrows
    public Object execute(DataModelPageDto dataModelDto, Map<String, Object> params) {
        String dataModelId = dataModelDto.getDataModelId();
        DataModelPo model = dataModelService.getModel(dataModelId);

        DynamicDataUtils.freePermit();
        Page<DynamicDataPo> page = new Page<>(dataModelDto.getCurrent(), dataModelDto.getSize());
        List<QueryConditionDto> queryConditions = new ArrayList<>();

        if (ObjectNull.isNotNull(dataModelDto.getBody())) {
            queryConditions = dataModelDto.getBody().stream().filter(e -> ObjectNull.isNotNull(e.getEnabledQueryTypes())).collect(Collectors.toList());
        }
        List<String> fieldList = BeanCopyUtil.copys(dataModelDto.getFields(), String.class);
        // 筛选字段
        List<FieldBasicsHtml> dataFieldList = fieldService.getFields(model.getAppId(), dataModelId, true, true);
        Page<Map<String, Object>> mapPage = dynamicDataService.queryPage(model.getAppId(), page, dataModelId, null, Collections.singletonList(queryConditions), dataModelDto.getOrderBy(), fieldList, false, false, true,
                dataFieldList, new HashSet<>(),false);
        mapPage.getRecords().forEach(e -> e.put("modelId", dataModelId));
        return mapPage;
    }

    @Override
    public void inspect(DataModelPageDto o) {
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
