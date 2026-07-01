package cn.bctools.design.crud.expression;

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
import cn.bctools.word.utils.WordVariableReplaceUtil;
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
 * 表单文件打印模板字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "表单字段", useCase = {EnvConstant.PRINT_FILE_FORM_ITEM_VALUE})
@AllArgsConstructor
public class PrintFileFormItemParam implements IJvsParam<ElementVo> {

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
        //为了兼容表单数据，此处公式类型需要进行清空
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
            FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field);
            if (ObjectNull.isNotNull(publicHtml) && publicHtml.getNext()) {
                iDataFieldHandler.next(list, publicHtml, handlerMap, e);
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
        list.stream()
                .filter(f -> DataFieldType.tableForm.name().equals(f.getFieldType()))
                .forEach(table -> {
                    // 表格的字段key替换掉路径，保留具体字段key
                    list.stream()
                            .filter(f -> Boolean.FALSE.equals(DataFieldType.tableForm.name().equals(f.getFieldType())) && f.getId().startsWith(table.getId()))
                            .forEach(f -> f.setId(f.getId().replace(table.getId() + StrUtil.DOT, "")).setInfo(null));
                    table.setId(WordVariableReplaceUtil.buildTableKey(table.getId()));
                });
        // 替换非表格字段
        list.stream()
                .filter(f -> Boolean.FALSE.equals(DataFieldType.tableForm.name().equals(f.getFieldType())))
                .forEach(f -> {
                    f.setId(WordVariableReplaceUtil.buildVariableKey(printDataType(f.getFieldType()), f.getId()));
                });
        // 移除类型为选项卡的字段
        list.removeIf(e -> DataFieldType.tab.name().equals(e.getFieldType()));
    }

    /**
     * 获取打印字段类型
     *
     * @param currentFieldType 字段类型
     * @return 打印字段类型
     */
    private WordVariableReplaceUtil.DataType printDataType(String currentFieldType) {
        // 目前只有上传图片类型的字段需要指定打印字段类型，其它字段默认为文本
        if (DataFieldType.imageUpload.name().equals(currentFieldType) || DataFieldType.signature.name().equals(currentFieldType)) {
            return WordVariableReplaceUtil.DataType.IMAGE;
        }
        if (DataFieldType.htmlEditor.name().equals(currentFieldType)) {
            return WordVariableReplaceUtil.DataType.HTML;
        }
        return null;
    }

}
