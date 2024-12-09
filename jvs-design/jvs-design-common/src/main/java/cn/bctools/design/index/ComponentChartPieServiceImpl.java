package cn.bctools.design.index;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.service.ComponentChartPieService;
import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import cn.bctools.index.design.render.ComponentChartPieRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Service
@Slf4j
@AllArgsConstructor
public class ComponentChartPieServiceImpl extends DataModelIndexFieldOrLink implements ComponentChartPieService<ComponentChartPieServiceImpl.ChartPieBase> {


    JvsAppService jvsAppService;
    DynamicDataService dynamicDataService;
    DataFieldService dataFieldService;
    IdentificationService identificationService;
    DataModelService dataModelService;


    /**
     * 定义实体类对象，用于传递值的对象数据
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class ChartPieBase extends FormQueryParamsBase {
        @FormFormQuery(label = "模型标识", prop = "modelIdentifier", desc = "选择模型的标识，应用管理[源码标示]进行配置", type = FormAttributeTypeEnum.select)
        public String modelIdentifier;
        @FormFormQuery(label = "聚合字段", prop = "aggregationField", desc = "选择聚合处理的字段", type = FormAttributeTypeEnum.select, link = {"modelIdentifier"})
        public String aggregationField;
        @FormFormQuery(label = "分组字段", prop = "groupField", desc = "选择分组字段", type = FormAttributeTypeEnum.select, link = {"modelIdentifier"})
        public String groupField;
        @FormFormQuery(label = "聚合方式", prop = "aggregationField", desc = "选择聚合的方式，包含最大、最小、平均、求和等", type = FormAttributeTypeEnum.select)
        public AggregateEnumType aggregationType;
    }

    @Override
    public ComponentChartPieRender fillData(ChartPieBase body) {
        ComponentChartPieRender render = new ComponentChartPieRender();
        render.setRadius(Arrays.asList("20%", "100%"));
        if (ObjectNull.isNull(body.getModelIdentifier())) {
            return null;
        } else {
            String modelId = identificationService.getIdentificationModel(body.getModelIdentifier()).stream().findFirst().map(Identification::getDesignId).orElse(null);
            if (ObjectNull.isNotNull(modelId)) {
                DataModelPo model = dataModelService.getModel(modelId);
                //加载模型的数据权限
                List<QueryConditionDto> queryConditions = new ArrayList<>();
                //加载模型的数据权限
                DynamicDataUtils.handleDesignDataScope(model.getRoles());
                Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
                List<Criteria> criteriaList = DynamicDataUtils.getAuthCriteria();
                if (ObjectNull.isNotNull(criteriaList)) {
                    //添加数据权限操作
                    criteria = DynamicDataUtils.initCriteria(criteria).andOperator(criteriaList);
                }
                List<String> fieldList = new ArrayList<>();
                fieldList.add(body.getGroupField());
                fieldList.add(body.getAggregationField());
                fieldList.add("createTime");
                List<Map<String, Object>> maps = dynamicDataService.queryList(modelId, criteria, fieldList)
                        .stream()
                        .sorted(Comparator.comparing(e1 -> DateUtil.parse(((Map) e1).get("createTime").toString()).toLocalDateTime()).reversed())
                        .collect(Collectors.toList());


                Map<String, List<Map<String, Object>>> collect = maps.stream()
                        .filter(e -> ObjectNull.isNotNull(e.get(body.getGroupField())))
                        .collect(Collectors.groupingBy(e -> ((Map<?, ?>) e).get(body.getGroupField()).toString()));

                List<ComponentChartPieRender.PieData> data = collect.keySet().stream().map(e -> {
                    List<Map<String, Object>> maps1 = collect.get(e);
                    DoubleSummaryStatistics wenDuXianShi = maps1.stream().map(s -> s.get(body.getAggregationField()))
                            .filter(ObjectNull::isNotNull)
                            .filter(v -> v instanceof Number || (v instanceof CharSequence && NumberUtil.isNumber((String) v)))
                            .map(s -> (Number) s).collect(Collectors.summarizingDouble(Number::doubleValue));
                    //判断求和，还是计数，还是平均值
                    if (ObjectNull.isNull(body.getAggregationType())) {
                        return null;
                    }
                    switch (body.getAggregationType()) {
                        case count:
                            return new ComponentChartPieRender.PieData().setName(e).setValue(wenDuXianShi.getCount());
                        case sum:
                            return new ComponentChartPieRender.PieData().setName(e).setValue(wenDuXianShi.getSum());
                        case ave:
                            return new ComponentChartPieRender.PieData().setName(e).setValue(wenDuXianShi.getAverage());
                        default:
                            return null;
                    }
                }).collect(Collectors.toList());
                render.setData(data);
                return render;
            }
        }
        return render;
    }

    @Override
    public List<SelectedAttribute> fieldInitOrLink(String key, ChartPieBase paramsDtoMap) {
        return super.field(key, paramsDtoMap.getModelIdentifier());
    }

}
