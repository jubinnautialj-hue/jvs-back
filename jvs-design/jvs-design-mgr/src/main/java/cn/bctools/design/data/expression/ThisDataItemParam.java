package cn.bctools.design.data.expression;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 所有模型的字段
 *
 * @Author: guojing
 */
@Order(1)
@Component
@JvsExpression(groupName = "模型", useCase = {EnvConstant.THIS_DATA_ITEM_VALUE}, prefix = "THISDATAMODEL")
@AllArgsConstructor
public class ThisDataItemParam implements IJvsParam<ElementVo> {

    FormService formService;
    DataFieldService fieldService;
    Map<String, IDataFieldHandler> handlerMap;
    DataModelService dataModelService;

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
        List<FieldBasicsHtml> fields = fieldService.getFields(model.getAppId(), designId, false, true);
        return fields.stream()
                .filter(s -> ObjectNull.isNotNull(s.getFieldName()))
                //排除条容器类组件
                .filter(s -> !DataFieldType.CONTAINER.contains(s.getFieldType()))
                .map(this::fieldDto2ElementVo).collect(Collectors.toList());
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
                .setInfo(fieldDto.getFieldKey() + "  " + fieldDto.getFieldName() + "\n" + fieldDto.getType().getDesc())
                .setJvsParamType(JvsParamType.getByClass(fieldDto.getType().getAClass()));
    }

}
