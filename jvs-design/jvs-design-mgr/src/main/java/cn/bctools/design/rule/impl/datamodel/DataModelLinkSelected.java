package cn.bctools.design.rule.impl.datamodel;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import cn.hutool.core.lang.Dict;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Data model link selected.
 *
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataModelLinkSelected implements LinkFieldSelected<String> {
    /**
     * The Field service.
     */
    DataFieldService fieldService;
    /**
     * The Data model service.
     */
    DataModelService dataModelService;
    Map<String, IDataFieldHandler> handlerMap;

    @Override
    public Object link(String id, String body) {
        fields field = DataModelLinkSelected.fields.valueOf(body);
        DataModelPo model = dataModelService.getModel(id);
        //需要排除一下设计的容器字段 ，和设计的样式字段

        Map<String, FieldBasicsHtml> allField = fieldService.getAllField(model.getAppId(), id, true, true, e -> false)
                .stream()
//                .filter(e -> !DataFieldType.CONTAINER.contains(e.getFieldType()))
                .filter(e -> !e.getFieldType().equals(DataFieldType.p))
                .filter(e -> !e.getFieldType().equals(DataFieldType.pageTable))
                .collect(Collectors.toMap(FieldPublicHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));
        //排除字段重复问题

        FieldBasicsHtml fieldPublicHtml = new FieldBasicsHtml();
        fieldPublicHtml.setFieldKey("jvsFlowTaskProgress");
        if (!allField.containsKey(fieldPublicHtml.getFieldKey())) {
            fieldPublicHtml.setFieldName("工作流进度");
            fieldPublicHtml.setType(DataFieldType.input);
            fieldPublicHtml.setEnabledQueryTypes(Collections.singletonList(DataQueryType.eq));
            allField.put(fieldPublicHtml.getFieldKey(), fieldPublicHtml);
        }

        switch (field) {
            case query:
            case body:
                //只会有一个特殊类型
                return allField.values()
                        .stream()
                        .map(e -> {
                            //如果是单行文本，可以添加包含和不包含规则
                            IDataFieldHandler handler = handlerMap.get(e.getType().getDesc());
                            if (ObjectNull.isNotNull(handler)) {
                                List enabledQueryTypes;
                                if (ObjectNull.isNull(e.getDesignJson())) {
                                    Map generate = handler.generate(e.getLabel(), e.getProp(), null);
                                    enabledQueryTypes = handler.getEnabledQueryTypes(handler.toHtml(generate));
                                } else {
                                    enabledQueryTypes = handler.getEnabledQueryTypes(handler.toHtml(e.getDesignJson()));
                                }
                                if (!enabledQueryTypes.contains(DataQueryType.isNull)) {
                                    enabledQueryTypes.add(DataQueryType.isNull);
                                }
                                e.setEnabledQueryTypes(enabledQueryTypes);
                            }
                            if (!e.getEnabledQueryTypes().contains(DataQueryType.isNull)) {
                                e.getEnabledQueryTypes().add(DataQueryType.isNull);
                            }
                            if (!e.getEnabledQueryTypes().contains(DataQueryType.isNotNull)) {
                                e.getEnabledQueryTypes().add(DataQueryType.isNotNull);
                            }
                            //转中文
                            List<Dict> collect = e.getEnabledQueryTypes().stream().map(s -> Dict.create().set("label", s.getDesc()).set("value", s.name())).collect(Collectors.toList());
                            Map<String, Object> map = BeanToMapUtils.beanToMap(collect);
                            map.put("enabledQueryTypes", collect);
                            return new ParameterOption<String>().setExtend(map).setType(e.getFieldType().name()).setLabel(e.getFieldName() + "_" + e.getFieldKey()).setValue(e.getFieldKey());
                        })
                        .collect(Collectors.toList());
            case fields:
            case orderBy:
            case x:
            case y:
            case xAxis:
            case groupBy2:
            case yAxis:
            case value:
            case groupBy:
                return allField.values()
                        .stream()
                        .map(e -> new ParameterOption<String>().setType(e.getFieldType().name()).setLabel(e.getFieldName()).setValue(e.getFieldKey())).collect(Collectors.toList());
            default:
                break;
        }
        throw new BusinessException("没有找到关联字段");
    }

    @Override
    public List<String> fields() {
        return Arrays.stream(fields.values()).map(Enum::name).collect(Collectors.toList());
    }

    /**
     * 关联选择项的字段，选择了数据模型后，将数据模型和字段进行返回给前端
     */
    enum fields {
        /**
         * Query fields.
         */
        query,
        /**
         * Body fields.
         */
        body,
        /**
         * Fields fields.
         */
        fields,
        /**
         * X fields.
         */
        x,
        /**
         * Y fields.
         */
        y,
        /**
         * X axis fields.
         */
        xAxis,
        /**
         * Y axis fields.
         */
        yAxis,
        /**
         * Group by fields.
         */
        groupBy,
        /**
         * Value fields.
         */
        value,
        /**
         * Group by 2 fields.
         */
        groupBy2,
        /**
         * Order by fields.
         */
        orderBy;
    }

}

