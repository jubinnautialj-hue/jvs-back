package cn.bctools.design.rule.impl.datamodel.convert;


import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
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

import java.util.ArrayList;
import java.util.HashMap;
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
        value = "数据转模型",
        group = RuleGroup.模型插件,
        test = true,
        order = 12,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
//        iconUrl = "rule-cishupanduan",
        explain = "将数据转换为模型关联的值，常用于导入文件，将部门、下拉、多选等信息转换为数据的 id"
)
public class DataModelShowConvertDataServiceImpl implements BaseCustomFunctionInterface<DataModelConvertDto> {

    DataModelService dataModelService;
    DataFieldService dataFieldService;
    Map<String, IDataFieldHandler> iDataFieldHandler;

    @Override
    public Object execute(DataModelConvertDto dto, Map<String, Object> params) {
        DataModelPo model = dataModelService.getModel(dto.getDataModelId());
        Map<String, FieldBasicsHtml> typeMaps = dataFieldService.getAllField(model.getAppId(), dto.getDataModelId()).stream().collect(Collectors.toMap(e -> e.getFieldKey(), Function.identity()));
        String tenantId = TenantContextHolder.getTenantId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //可能存在数据来源于另一个逻辑导致无法正常获取数据
        CompletableFuture<List<Map<String, Object>>> resultCompletableFuture = CompletableFuture.supplyAsync(() -> {
            RuleSystemThreadLocal.clear();
            TenantContextHolder.setTenantId(tenantId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            List<Map<String, Object>> list = new ArrayList<>(dto.getBody());
            //做数据转换
            list.forEach(e -> {
                Map<String, Object> lineData = new HashMap<>(e);
                for (String key : e.keySet()) {
                    FieldBasicsHtml fieldBasicsHtml = typeMaps.get(key);
                    if (ObjectNull.isNotNull(fieldBasicsHtml)) {
                        IDataFieldHandler handler = iDataFieldHandler.get(fieldBasicsHtml.getType().getDesc());
                        Object o = e.get(key);
                        if (ObjectNull.isNotNull(o, handler, fieldBasicsHtml.getDesignJson())) {
                            Object obj = handler.getConversionKey(handler.toHtml(fieldBasicsHtml), o, lineData);
                            e.put(key, obj);
                        }
                    }
                }
            });
            return list;
        });
        try {
            return resultCompletableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
