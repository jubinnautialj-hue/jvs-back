package cn.bctools.design.rule.impl.datamodel.convert;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gx
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "模型转数据",
        group = RuleGroup.模型插件,
        order = 11,
        test = true,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
//        iconUrl = "rule-cishupanduan",
        explain = "将查询出来的模型数据转换为对应的显示名称，常用于查询数据生成 excel 或将查询数据转换为用户可查看的值"
)
public class DataModelDataConvertShowServiceImpl implements BaseCustomFunctionInterface<DataModelConvertShowDto> {


    DataModelService dataModelService;
    DataFieldService dataFieldService;
    Map<String, IDataFieldHandler> iDataFieldHandler;

    @Override
    public Object execute(DataModelConvertShowDto dto, Map<String, Object> params) {
        DataModelPo model = dataModelService.getModel(dto.getDataModelId());
        Map<String, FieldBasicsHtml> typeMaps = dataFieldService.getAllField(model.getAppId(), dto.getDataModelId(), e -> false).stream().collect(Collectors.toMap(e -> e.getFieldKey(), Function.identity()));
        String tenantId = TenantContextHolder.getTenantId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //可能存在数据来源于另一个逻辑导致无法正常获取数据
        CompletableFuture<List<Map<String, Object>>> resultCompletableFuture = CompletableFuture.supplyAsync(() -> {
            RuleSystemThreadLocal.clear();
            TenantContextHolder.setTenantId(tenantId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //判断是单条还是多条数据
            List<Map<String, Object>> body = dto.getBody();
            if (ObjectNull.isNull(body)) {
                throw new BusinessException("没有数据");
            }
            //做数据转换
            try {
                body.forEach(e -> {
                    for (String key : e.keySet()) {
                        FieldBasicsHtml fieldBasicsHtml = typeMaps.get(key);
                        if (ObjectNull.isNotNull(fieldBasicsHtml)) {
                            IDataFieldHandler handler = iDataFieldHandler.get(fieldBasicsHtml.getType().getDesc());
                            Object o = e.get(key);
                            if (ObjectNull.isNotNull(o, handler, fieldBasicsHtml.getDesignJson())) {
                                FieldBasicsHtml html = handler.toHtml(fieldBasicsHtml);
                                Object obj = handler.getEcho(html, o, dto.getReplace(), e);
                                e.put(key, obj);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                log.error("转换数据异常", e);
                throw new BusinessException("转换数据异常");
            }
            return body;
        }, RuleStartUtils.EXECUTOR);
        try {
            return resultCompletableFuture.get();
        } catch (InterruptedException e) {
            log.error("模型转数据异常", e);
            throw new RuntimeException("", e);
        } catch (ExecutionException e) {
            log.error("模型转数据异常", e);
            throw new RuntimeException(e);
        }
    }
}
