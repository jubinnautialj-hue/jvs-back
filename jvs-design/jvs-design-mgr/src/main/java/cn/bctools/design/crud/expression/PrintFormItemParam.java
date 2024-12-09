package cn.bctools.design.crud.expression;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.vo.PrintFormElementVo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import cn.hutool.core.util.StrUtil;
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

/**
 * @author zhuxiaokang
 * 表单打印模板设计返回的字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "表单字段", useCase = {EnvConstant.PRINT_FORM_ITEM_VALUE})
@AllArgsConstructor
public class PrintFormItemParam implements IJvsParam<ElementVo> {

    FormService formService;
    DataFieldService fieldService;
    DataModelService dataModelService;
    Map<String, IDataFieldHandler> handlerMap;

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return null;
    }

    @Override
    public List<ElementVo> getAllElements() {
        String designId = this.getDesignId();
        List<ElementVo> list = getFormDesignVo(designId);
        list.forEach(e -> e.setType(null));
        return list;
    }

    private List<ElementVo> getFormDesignVo(String designId) {
        if (StringUtils.isBlank(designId)) {
            return Collections.emptyList();
        }
        String dataModelId = designId;
        try {
            // 根据表单设计获取数据模型id
            dataModelId = formService.get(designId).getDataModelId();
        } catch (Exception e) {
            //兼容表单设计规则
            log.error("根据表单设计获取数据模型id异常：{}", e.getMessage());
        }
        DataModelPo byId = dataModelService.getById(dataModelId);
        // 根据数据模型获取字段
        List<FieldBasicsHtml> fields = fieldService.getFields(byId.getAppId(), dataModelId, designId, true, false)
                .stream().filter(e -> ObjectNull.isNotNull(e.getType())).collect(Collectors.toList());

        List<ElementVo> list = new ArrayList<>();
        fields.forEach(field -> {
            //先添加当前这个字段
            ElementVo e = fieldDto2ElementVo(field);
            list.add(e);
            //判断所有的容器组件，进行下级组件内容解析
            IDataFieldHandler iDataFieldHandler = handlerMap.get(field.getType().getDesc());
            if (ObjectNull.isNotNull(field.getDesignJson())) {
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field);
                if (publicHtml.getNext()) {
                    iDataFieldHandler.next(list, publicHtml, handlerMap, e);
                }
            }
        });
        // 解析并转换数据结构
        convert(list);
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

    /**
     * 解析并转换数据结构
     *
     * @param list
     */
    private void convert(List<ElementVo> list) {
        // 要从结果中移除的字段
        List<String> removeFieldIds = new ArrayList<>();
        list.stream()
                // 封装表格的字段（打印设计可拖动的最深层次为表格）
                .filter(f -> DataFieldType.tableForm.name().equals(f.getFieldType()))
                .forEach(t -> {
                    // 找到表格的字段
                    List<ElementVo> tableFields = list.stream()
                            .filter(f -> Boolean.FALSE.equals(DataFieldType.tableForm.name().equals(f.getFieldType())) && f.getId().startsWith(t.getId()))
                            .collect(Collectors.toList());
                    removeFieldIds.addAll(tableFields.stream().map(ElementVo::getId).collect(Collectors.toList()));
                    // 替换表格字段的路径（前端不需要全路径）
                    List<ElementVo> newTableFields = tableFields.stream()
                            .map(f -> new ElementVo()
                                    .setId(f.getId().replace(t.getId() + StrUtil.DOT, ""))
                                    .setName(f.getName().replace(t.getName() + StrUtil.DOT, ""))
                                    .setInfo(null)
                            )
                            .collect(Collectors.toList());

                    t.getOther().put(Get.name(PrintFormElementVo::getTableFields), newTableFields);
                });
        // 移除指定字段，且移除选项卡字段
        list.removeIf(l -> removeFieldIds.contains(l.getId()) || DataFieldType.tab.name().equals(l.getFieldType()));
    }

}
