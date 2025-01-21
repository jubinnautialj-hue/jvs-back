package cn.bctools.design.rule.impl.datamodel.delete;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.oauth2.utils.UserCurrentUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        value = "删除数据",
        group = RuleGroup.模型插件,
        test = true,
        order = 3,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.未识别,
//        iconUrl = "rule-piliangshanchu",
        explain = "默认跳过数据权限，根据查询条件删除数据模型中数据"
)
public class DataModelDeleteServiceImpl implements BaseCustomFunctionInterface<DataModelDeleteDto> {

    DynamicDataService dynamicDataService;
    DataNoticeHandler dataNoticeHandler;
    DataModelService dataModelService;

    @Override
    @SneakyThrows
    public Object execute(DataModelDeleteDto dataModelDto, Map<String, Object> params) {
        if (ObjectNull.isNull(dataModelDto.getBody()) && ObjectNull.isNull(dataModelDto.getIds())) {
            throw new BusinessException("没有条件不支持删除");
        }
        DynamicDataUtils.freePermit();
        List<String> ids = new ArrayList<>();
        String appId = dataModelService.getById(dataModelDto.getDataModelId()).getAppId();
        if (ObjectNull.isNotNull(dataModelDto.getIds())) {
            for (String id : dataModelDto.getIds()) {
                //多条删除
                dynamicDataService.onlyRemove(dataModelDto.getDataModelId(), id);
                ids.add(id);
            }
        } else {
            //查詢所有要删除的数据，发通知,再删除
            List<Object> objects = dynamicDataService.removeMulti(dataModelDto.getDataModelId(), dataModelDto.getBody());
            if (ObjectNull.isNotNull(objects)) {
                objects.forEach(e -> {
                    try {
                        dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.DELETED, dataModelDto.getDataModelId(), null, (Map) e);
                        Object o = ((Map<?, ?>) e).get("id");
                        ids.add(o.toString());
                    } catch (Exception ex) {
                        log.error("通知异常");
                    }
                });
            }
        }
        String user = Optional.ofNullable(UserCurrentUtils.getNullableUser()).map(e -> e.getRealName()).orElseGet(() -> "");
        if (ObjectNull.isNotNull(ids)) {
            return user + "删除成功:" + ids.stream().collect(Collectors.joining(","));
        }
        //表示没有删除的数据
        return false;
    }


    @Override
    public void inspect(DataModelDeleteDto o) {
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
