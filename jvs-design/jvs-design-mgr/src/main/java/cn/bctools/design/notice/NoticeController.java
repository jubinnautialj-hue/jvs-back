package cn.bctools.design.notice;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 */
@Api(tags = "[notice]消息通知")
@RestController
@RequestMapping("/base/notice")
@AllArgsConstructor
public class NoticeController {
    private final DataFieldService dataFieldService;
    private final AuthTenantConfigServiceApi tenantConfigApi;
    private final Map<String, IDataFieldHandler> handlerMap;

    /**
     * 消息可用的模型字段类型
     */
    private static final List<String> AVAILABLE_MODEL_FIELD_TYPES = new ArrayList<>();

    static {
        List<String> availableFieldType = Stream.of(
                DataFieldType.input.name(),
                DataFieldType.inputNumber.name(),
                DataFieldType.textarea.name(),
                DataFieldType.select.name(),
                DataFieldType.serialNumber.name(),
                DataFieldType.datePicker.name(),
                DataFieldType.timePicker.name(),
                DataFieldType.timeSelect.name(),
                DataFieldType.radio.name(),
                DataFieldType.checkbox.name(),
                DataFieldType.link.name(),
                DataFieldType.department.name(),
                DataFieldType.user.name(),
                DataFieldType.role.name(),
                DataFieldType.job.name()
        ).collect(Collectors.toList());
        AVAILABLE_MODEL_FIELD_TYPES.addAll(availableFieldType);
    }

    @Log
    @ApiOperation("支持的消息通知类型")
    @GetMapping("/type")
    public R<List> getType() {
        List<ConfigsTypeEnum> data = tenantConfigApi.keys().getData();
        List<NoticeTypeEnum> noticeTypeEnums = new ArrayList<>();
        noticeTypeEnums.add(NoticeTypeEnum.SYSTEM);
        if (data.contains(ConfigsTypeEnum.SMS_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.SMS);
        }
        if (data.contains(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.WECHAT_MP);
        }
        if (data.contains(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.WECHAT_ENTERPRISE);
        }
        if (data.contains(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.DING);
        }
        if (data.contains(ConfigsTypeEnum.MAIL_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.EMAIL);
        }
        //根据已经配置了的配置，返回支持的消息类型
        List<Map<String, String>> enableAll = NoticeTypeEnum.getEnableAll(noticeTypeEnums::contains);
        return R.ok(enableAll);
    }

    @Log
    @ApiOperation("系统消息可用的字段")
    @GetMapping("/fields")
    public R<List> getFields() {
        return R.ok(dataFieldService.getDefaultAllFields());
    }

    @Log
    @ApiOperation("消息可用的模型字段")
    @GetMapping("/{appId}/{modelId}/fields")
    public R<List> getFields(@PathVariable String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> modelFields = dataFieldService.getFields(appId, modelId, true, true);
        if (CollectionUtils.isEmpty(modelFields)) {
            return R.ok();
        }
        List<ElementVo> elementVos = new ArrayList<>();
        for (FieldBasicsHtml field : modelFields) {
            ElementVo elementVo = fieldDto2ElementVo(field);
            elementVos.add(elementVo);
            IDataFieldHandler iDataFieldHandler = handlerMap.get(field.getType().getDesc());
            if (ObjectNull.isNull(iDataFieldHandler, field.getDesignJson())) {
                continue;
            }
            FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field.getDesignJson());
            if (publicHtml.getNext()) {
                iDataFieldHandler.next(elementVos, publicHtml, handlerMap, elementVo);
            }
        }
        if (CollectionUtils.isEmpty(elementVos)) {
            return R.ok();
        }
        List<String> tableKeys = elementVos.stream()
                .filter(e -> DataFieldType.tableForm.name().equals(e.getFieldType()))
                .map(ElementVo::getId).collect(Collectors.toList());
        List<ElementVo> results = elementVos.stream()
                // 筛选消息可用的模型字段类型
                .filter(f -> AVAILABLE_MODEL_FIELD_TYPES.contains(f.getFieldType()))
                // 排除表格中的用户组件字段
                .filter(f -> Boolean.FALSE.equals(tableKeys.stream().anyMatch(tableKey -> f.getId().startsWith(tableKey))))
                .collect(Collectors.toList());
        return R.ok(results);
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
