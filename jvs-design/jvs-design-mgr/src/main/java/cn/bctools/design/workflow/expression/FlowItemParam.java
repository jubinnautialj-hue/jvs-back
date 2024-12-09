package cn.bctools.design.workflow.expression;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Service
@AllArgsConstructor
@JvsExpression(groupName = "数据字段", useCase = EnvConstant.FLOW_FUNCTION_ITEM_VALUE)
public class FlowItemParam implements IJvsParam<ElementVo> {

    private final DataFieldService dataFieldService;
    private final FlowDesignService flowDesignService;
    Map<String, IDataFieldHandler> handlerMap;

    @Override
    public List<ElementVo> getAllElements() {
        String designId = this.getDesignId();
        if (StringUtils.isBlank(designId)) {
            return Collections.emptyList();
        }
        FlowDesign flowDesign = flowDesignService.getById(designId);
        if (ObjectUtils.isEmpty(flowDesign)) {
            return Collections.emptyList();
        }
        String modeId = flowDesign.getDataModelId();
        List<FieldBasicsHtml> dataFieldDtos = dataFieldService.getFields(flowDesign.getJvsAppId(), modeId, null, true, false);
        if (CollectionUtils.isEmpty(dataFieldDtos)) {
            return Collections.emptyList();
        }
        List<ElementVo> list = new ArrayList<>();
        for (FieldBasicsHtml field : dataFieldDtos) {
            //先添加当前这个字段
            ElementVo e = fieldDto2ElementVo(field);
            //如果类型为空,直接下一条
            if (ObjectNull.isNull(e.getJvsParamType())) {
                continue;
            }
            //主动设置为数组
            if (field.getFieldType().equals(DataFieldType.tableForm)) {
                e.setJvsParamType(JvsParamType.array);
            }
            list.add(e);
            //判断所有的容器组件，进行下级组件内容解析
            IDataFieldHandler iDataFieldHandler = handlerMap.get(field.getType().getDesc());
            if (ObjectNull.isNotNull(iDataFieldHandler, field.getDesignJson())) {
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(field);
                if (publicHtml.getNext()) {
                    iDataFieldHandler.next(list, publicHtml, handlerMap, e);
                }
            }
        }
        list.forEach(e -> e.setType(null));
        return list;
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return data.get(paramName);
    }

    /**
     * 字段对象转表达式参数对象
     *
     * @param fieldDto 字段对象
     * @return 表达式参数对象
     */
    public static ElementVo fieldDto2ElementVo(FieldBasicsHtml fieldDto) {
        return new ElementVo()
                .setId(fieldDto.getFieldKey())
                .setPath(fieldDto.getFieldKey())
                .setName(fieldDto.getFieldName())
                .setShortName(fieldDto.getFieldName())
                .setInfo(fieldDto.getFieldKey() + "  " + fieldDto.getFieldName() + "\n" + fieldDto.getType().getDesc())
                .setJvsParamType(JvsParamType.getByClass(fieldDto.getType().getAClass()));
    }
}
