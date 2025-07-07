package cn.bctools.design.rule.impl.datamodel.save;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.rule.impl.datamodel.FieldStructureUtils;
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
        value = "新增数据",
        group = RuleGroup.模型插件,
        order = 1,
        test = true,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.文本,
//        iconUrl = "rule-xinzengshujufuwu",
        explain = "数据模型新增数据"
)
public class DataModelSaveServiceImpl implements BaseCustomFunctionInterface<DataModelSaveDto> {

    DynamicDataService dynamicDataService;
    DataFieldService fieldService;
    DataModelService dataModelService;
    DataNoticeHandler dataNoticeHandler;

    @Override
    public List<RuleElementVo> structureType(DataModelSaveDto o) {
        return FieldStructureUtils.transform(o.getDataModelId(), null, dataModelService, fieldService);
    }

    /**
     * 执行方法
     *
     * @param dataModelDto
     * @param params
     * @return
     */
    @Override
    @SneakyThrows
    public Object execute(DataModelSaveDto dataModelDto, Map<String, Object> params) {
        String appId = dataModelService.getById(dataModelDto.getDataModelId()).getAppId();
        if (ObjectNull.isNotNull(appId)) {
            dynamicDataService.checkDataFieldType(appId, dataModelDto.getDataModelId(), dataModelDto.getBody(), false);
            String save = dynamicDataService.saveTransactionalRule(appId, dataModelDto.getDataModelId(), dataModelDto.getBody());
            dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.CREATED, dataModelDto.getDataModelId(), save, dataModelDto.getBody());
            return String.valueOf(save);
        }
        return null;
    }


}
