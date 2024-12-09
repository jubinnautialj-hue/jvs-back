package cn.bctools.design.data.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.expression.FormItemParam;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.notice.handler.enums.ReceiverSystemFieldEnum;
import cn.bctools.design.notice.handler.enums.ReceiverTypeEnum;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[data]消息通知支持的用户选项")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/data/notice/user")
public class DataNoticeUserController {

    private final DataFieldService dataFieldService;

    Map<String, IDataFieldHandler> handlerMap;

    FormItemParam formItemParam;

    @Log
    @ApiOperation("消息通知支持的用户选项")
    @GetMapping("/options/{modelId}")
    public R<Map<String, List<Map<String, String>>>> systemUserOptions(@PathVariable String modelId, @PathVariable String appId) {
        // 得到模型用户字段集合
        List<Map<String, String>> userFields = getUserFields(appId, modelId);
        // 得到系统字段
        List<Map<String, String>> systemFields = ReceiverSystemFieldEnum.getMap();
        // 封装响应
        Map<String, List<Map<String, String>>> result = new HashMap<>(2);
        result.put(ReceiverTypeEnum.system_field.getValue(), systemFields);
        result.put(ReceiverTypeEnum.model_field.getValue(), userFields);
        return R.ok(result);
    }

    /**
     * 得到模型字段
     *
     * @param appId
     * @param modelId
     * @return
     */
    private List<Map<String, String>> getUserFields(String appId, String modelId) {
        List<FieldBasicsHtml> modelFields = dataFieldService.getFields(appId, modelId, true, false);
        if (CollectionUtils.isEmpty(modelFields)) {
            return Collections.emptyList();
        }
        List<ElementVo> elementVos = new ArrayList<>();
        for (FieldBasicsHtml field : modelFields) {
            ElementVo elementVo = fieldDto2ElementVo(field);
            elementVos.add(elementVo);
            IDataFieldHandler iDataFieldHandler = handlerMap.get(field.getType().getDesc());
            if (ObjectNull.isNull(iDataFieldHandler, field.getDesignJson())) {
                continue;
            }
            FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field);
            if (publicHtml.getNext()) {
                iDataFieldHandler.next(elementVos, publicHtml, handlerMap, elementVo);
            }
        }
        if (CollectionUtils.isEmpty(elementVos)) {
            return Collections.emptyList();
        }
        List<String> tableKeys = elementVos.stream()
                .filter(e -> DataFieldType.tableForm.name().equals(e.getFieldType()))
                .map(ElementVo::getId).collect(Collectors.toList());
        return elementVos.stream()
                // 只返回用户组件字段
                .filter(f -> DataFieldType.user.name().equals(f.getFieldType()))
                // 排除表格中的字段
                .filter(f -> Boolean.FALSE.equals(tableKeys.stream().anyMatch(tableKey -> f.getId().startsWith(tableKey))))
                .map(f -> {
                    Map<String, String> userField = new HashMap<>(2);
                    userField.put("id", f.getId());
                    userField.put("name", f.getName());
                    return userField;
                }).collect(Collectors.toList());
    }

    /**
     * 字段对象转表达式参数对象
     *
     * @param fieldDto 字段对象
     * @return 表达式参数对象
     */
    private ElementVo fieldDto2ElementVo(FieldBasicsHtml fieldDto) {
        return new ElementVo()
                .setId(fieldDto.getFieldKey())
                .setName(fieldDto.getFieldName())
                .setShortName(fieldDto.getFieldName())
                .setInfo(fieldDto.getFieldKey() + "  " + fieldDto.getFieldName() + "\n" + fieldDto.getType().getDesc())
                .setJvsParamType(JvsParamType.getByClass(fieldDto.getType().getAClass()))
                .setFieldType(fieldDto.getType().name());
    }
}
