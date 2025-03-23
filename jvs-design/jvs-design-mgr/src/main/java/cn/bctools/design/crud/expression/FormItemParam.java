package cn.bctools.design.crud.expression;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.entity.DynamicDataPo;
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
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单字段
 *
 * @Author: GuoZi
 */
@Order(-2)
@Component
@JvsExpression(groupName = "表单字段", useCase = {EnvConstant.FORM_ITEM_VALUE, EnvConstant.FORM_BUTTON_DISPLAY, EnvConstant.FORM_QR_CODE_TAG})
@AllArgsConstructor
public class FormItemParam implements IJvsParam<ElementVo> {

    FormService formService;
    DataFieldService fieldService;
    DataModelService dataModelService;
    Map<String, IDataFieldHandler> handlerMap;

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        Object read = JvsJsonPath.read((data), paramName);
        if (read instanceof JSONArray) {
            Integer index = SystemThreadLocal.get("index");
            if (ObjectNull.isNull(index)) {
                return read;
            }
            if (((List) read).size() <= index) {
                //开始到结束
                String substring = paramName.substring(0, paramName.lastIndexOf(StrUtil.DOT));
                List tableForm1663233092910 = (List) JvsJsonPath.read(JSONObject.toJSONString(data), substring);
                Map o = (Map) tableForm1663233092910.get(index);
                Object orDefault = o.getOrDefault(paramName.substring(paramName.lastIndexOf(StrUtil.DOT) + 1), 0);
                return orDefault;
            } else {
                return ((JSONArray) read).get(index);
            }
        } else if (read == null) {
            //给定默认值
            String designId = this.getDesignId();
            List<ElementVo> formDesignVo = getFormDesignVo(designId);
            //判断是否有点,如果有点，表示是下级，并判断是否是数组集层级

            //确定返回的类型
            JvsParamType jvsParamType = formDesignVo.stream().filter(e -> e.getId().equals(paramName)).map(e -> e.getJvsParamType()).findAny().get();

            //查询有集合的组件，获取组件id，进行判断是否在获取值中对比
            List<String> first = formDesignVo.stream().filter(e -> e.getJvsParamType().equals(JvsParamType.array)).map(ElementVo::getId).collect(Collectors.toList());
            if (ObjectNull.isNotNull(first)) {
                if (Arrays.stream(paramName.split("\\.")).anyMatch(first::contains)) {
                    //表示返回数组，
                    switch (jvsParamType) {
                        case number:
                            return new int[]{};
                        case text:
                            return new String[]{};
                        default:
                            return new Object[]{};
                    }
                }
            } else {
                //表示返回数组，
                switch (jvsParamType) {
                    case number:
                        return 0;
                    case text:
                        return "";
                    case array:
                        return new String[]{};
                    default:

                }
            }
        }
        return read;
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
        FormPo formPo = null;
        try {
            // 根据表单设计获取数据模型id
            formPo = formService.get(designId);
            dataModelId = formPo.getDataModelId();
        } catch (Exception e) {
            //兼容表单设计规则
        }
        DataModelPo byId = dataModelService.getById(dataModelId);
        // 根据数据模型获取字段
        List<FieldBasicsHtml> fields = fieldService.getFields(byId.getAppId(), dataModelId, designId, true, false)
                .stream().filter(e -> ObjectNull.isNotNull(e.getType()) && ObjectNull.isNotNull(e.getFieldName())).collect(Collectors.toList());

        List<ElementVo> list = new ArrayList<>();
        for (FieldBasicsHtml field : fields) {
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

        FieldBasicsHtml basicsHtml = new FieldBasicsHtml();
        basicsHtml.setFieldKey(Get.name(DynamicDataPo::getId)).setFieldName("数据id").setType(DataFieldType.input);
        list.add(0, fieldDto2ElementVo(basicsHtml));
        //将组件的全真实路径和字段路径进行处理,并放在设计中
        Map<String, String> stringMap = list.stream().collect(Collectors.toMap(ElementVo::getId, ElementVo::getId, (a, b) -> a));
        formPo.setFieldPathMap(stringMap);
        formService.updateById(formPo);
        return list;

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
                .setInfo(fieldDto.getFieldKey() + "  " + fieldDto.getLabel() + "\n" + fieldDto.getType().getDesc())
                .setJvsParamType(JvsParamType.getByClass(fieldDto.getType().getAClass()));
    }

}
