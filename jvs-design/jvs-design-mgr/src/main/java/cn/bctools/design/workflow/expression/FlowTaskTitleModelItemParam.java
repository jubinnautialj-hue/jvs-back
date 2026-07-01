package cn.bctools.design.workflow.expression;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 * 工作流自定义流程任务标题设计可用数据字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "模型字段", useCase = {EnvConstant.FLOW_TASK_TITLE_MODEL_ITEM_VALUE})
@AllArgsConstructor
public class FlowTaskTitleModelItemParam implements IJvsParam<ElementVo> {
    FormService formService;
    DataFieldService fieldService;
    Map<String, IDataFieldHandler> handlerMap;
    DataModelService dataModelService;

    /**
     * 可用来拼接流程任务标题的数据字段类型
     */
    private static final List<String> AVAILABLE_FIELD_TYPES = new ArrayList<>();

    static {
        List<String> availableFieldType = Stream.of(
                DataFieldType.input.name(),
                DataFieldType.inputNumber.name(),
                DataFieldType.serialNumber.name(),
                DataFieldType.radio.name()
        ).collect(Collectors.toList());
        AVAILABLE_FIELD_TYPES.addAll(availableFieldType);
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        Object read = JvsJsonPath.read((data), paramName);
        return read;
    }

    @Override
    public List<ElementVo> getAllElements() {
        String designId = this.getDesignId();
        if (StringUtils.isBlank(designId)) {
            return Collections.emptyList();
        }
        DataModelPo model = dataModelService.getModel(designId);
        List<FieldBasicsHtml> fields = fieldService.getFields(model.getAppId(), designId, true, false);
        List<ElementVo> list = new ArrayList<>();

        fields.forEach(field -> {
            //先添加当前这个字段
            ElementVo e = fieldDto2ElementVo(field);
            list.add(e);
            //判断所有的容器组件，进行下级组件内容解析
            IDataFieldHandler iDataFieldHandler = handlerMap.get(field.getType().getDesc());
            if (ObjectNull.isNotNull(iDataFieldHandler, field.getDesignJson())) {
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field.getDesignJson());
                if (ObjectNull.isNotNull(publicHtml) && publicHtml.getNext()) {
                    iDataFieldHandler.next(list, publicHtml, handlerMap, e);
                }
            }
        });

        // 剔除不支持的字段
        List<String> tableKeys = list.stream()
                .filter(e -> DataFieldType.tableForm.name().equals(e.getFieldType()))
                .map(ElementVo::getId).collect(Collectors.toList());
        list.removeIf(e ->
                tableKeys.stream().anyMatch(tableKey -> e.getId().startsWith(tableKey)) ||
                        Boolean.FALSE.equals(AVAILABLE_FIELD_TYPES.contains(e.getFieldType())) ||
                        "jvsFlowTaskProgress".equals(e.getId())
        );
        list.forEach(e -> e.setType(null));
        return list;
    }

    /**
     * 字段对象转表达式参数对象
     *
     * @param fieldDto 字段对象
     * @return 表达式参数对象
     */
    private ElementVo fieldDto2ElementVo(FieldBasicsHtml fieldDto) {
        ElementVo element = new ElementVo();
        element.setId(fieldDto.getFieldKey());
        element.setName(fieldDto.getFieldName());
        element.setShortName(fieldDto.getFieldName());
        element.setInfo(fieldDto.getFieldKey() + "  " + fieldDto.getFieldName() + "\n" + fieldDto.getType().getDesc());
        element.setJvsParamType(JvsParamType.getByClass(fieldDto.getType().getAClass()));
        element.setFieldType(fieldDto.getType().name());
        return element;
    }
}
