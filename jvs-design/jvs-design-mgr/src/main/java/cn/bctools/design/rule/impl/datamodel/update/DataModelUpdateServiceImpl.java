package cn.bctools.design.rule.impl.datamodel.update;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        value = "更新模型",
        group = RuleGroup.模型插件,
        test = true,
        order = 2,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数字,
//        iconUrl = "rule-piliangbianji",
        explain = "跳过数据权限，根据查询条件指定更新数据模型，支持单条或数组字符串多条"
)
public class DataModelUpdateServiceImpl implements BaseCustomFunctionInterface<DataModelUpdateDto> {
    DataNoticeHandler dataNoticeHandler;
    DynamicDataService dynamicDataService;
    DataModelService dataModelService;
    DataFieldService fieldService;

    /**
     * 自定义参数结构，可用于下级节点选择结构
     */
    @Override
    public List<RuleElementVo> structureType(DataModelUpdateDto o) {
        String dataModelId = o.getDataModelId();
        if (ObjectNull.isNull(dataModelId)) {
            return null;
        }
        return FieldStructureUtils.transform(o.getDataModelId(), null, dataModelService, fieldService);
    }


    @Override
    @SneakyThrows
    public Object execute(DataModelUpdateDto dataModelDto, Map<String, Object> params) {
        DynamicDataUtils.freePermit();
        dataModelDto.getBody().keySet().forEach(e -> {
            if (dataModelDto.getBody().get(e).equals(DynamicDataConstant.DATA_EMPTY)) {
                dataModelDto.getBody().put(e, null);
            }
        });
        String appId = dataModelService.getById(dataModelDto.getDataModelId()).getAppId();
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id");
        List<QueryConditionDto> queryConditions = dataModelDto.getQuery();
        queryConditions.forEach(e -> fieldList.add(e.getFieldKey()));
        List<Map<String, Object>> maps = dynamicDataService.queryList(dataModelDto.getDataModelId(), DynamicDataUtils.buildDynamicCriteria(queryConditions), fieldList);
        dynamicDataService.checkDataFieldType(appId, dataModelDto.getDataModelId(), dataModelDto.getBody(), true);
        long l = dynamicDataService.updateMulti(dataModelDto.getDataModelId(), dataModelDto.getQuery(), dataModelDto.getBody());
        //查詢所有要删除的数据，发通知,再删除
        maps.forEach(e -> {
            try {
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.EDITED, dataModelDto.getDataModelId(), String.valueOf(e.get("id")), dataModelDto.getBody());
            } catch (Exception ignored) {
            }
        });
        return l;
    }


    @Override
    public void inspect(DataModelUpdateDto o) {
        if (ObjectNull.isNull(o.getQuery())) {
            throw new BusinessException("查询条件不能为空");
        }
        //并校验条件是否为空，如果为空，则查询条件不满足返回异常
        o.getQuery().forEach(e -> {
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
