package cn.bctools.design.index;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.service.ComponentChartLineService;
import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import cn.bctools.index.design.render.ComponentChartLineRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Component chart line service.
 *
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class ComponentChartLineServiceImpl extends DataModelIndexFieldOrLink implements ComponentChartLineService<ComponentChartLineServiceImpl.DataChartLineParamsBase> {

    JvsAppService jvsAppService;
    DynamicDataService dynamicDataService;
    IdentificationService identificationService;
    DataModelService dataModelService;
    DataFieldService dataFieldService;

    /**
     * 定义实体类对象，用于传递值的对象数据
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class DataChartLineParamsBase extends FormQueryParamsBase {
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
    public ComponentChartLineRender fillData(DataChartLineParamsBase body) {
        ComponentChartLineRender render = new ComponentChartLineRender();
        //如果没有设计，直接返回
        if (ObjectNull.isNull(body.getModelIdentifier(), body.getAggregationType())) {
            return null;
        } else {
            ModeUtils.setMode();
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
                List<String> list = new ArrayList<>();
                list.add(body.getGroupField());
                list.add(body.getAggregationField());
                list.add("createTime");
                List<Map<String, Object>> maps = dynamicDataService.queryList(modelId, criteria, list)
                        .stream()
                        .sorted(Comparator.comparing(e1 -> DateUtil.parse(((Map) e1).get("createTime").toString()).toLocalDateTime()).reversed())
                        .collect(Collectors.toList());
                //根据时间进行划分，每一个时间段再进行划分处理
                Map<String, List<Map<String, Object>>> timeMap = maps.stream()
                        .collect(Collectors.groupingBy(e -> DateUtil.parse((String) e.get("createTime")).toDateStr(), Collectors.toList()));

                List<ComponentChartLineRender.SeriesItem> series = new ArrayList<>();
                //时间序列
                List<String> times = timeMap.keySet().stream()
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                //判断是否需要转换
                Map<String, FieldBasicsHtml> allField = dataFieldService.getAllField(model.getAppId(), model.getId(), true, false, e -> !e.getFieldKey().equals(body.getGroupField()))
                        .stream().collect(Collectors.toMap(FieldPublicHtml::getFieldKey, Function.identity()));
                //对其数据进行转换处理
                for (Map<String, Object> map : maps) {
                    map = dynamicDataService.echo(map, allField, true);
                }
                //根据字段分组
                Map<String, Map<String, List<Map<String, Object>>>> fieldMap = maps.stream()
                        .filter(e -> ObjectNull.isNotNull(e.get(body.getGroupField())))
                        .collect(Collectors.groupingBy(e -> e.get(body.getGroupField()).toString(), Collectors.groupingBy(s -> DateUtil.parse((String) s.get("createTime")).toDateStr())));
                //根据字段分组后处理，区分每一个时间段的个数
                for (String field : fieldMap.keySet()) {
                    //获取这个分类下的时间序列
                    Map<String, List<Map<String, Object>>> timesMap = fieldMap.get(field);
                    //每一个时间序列的
                    //求和
                    List collect = times.stream()
                            .map(s -> timesMap.getOrDefault(s, new ArrayList<>()))
                            .map(s -> {
                                switch (body.getAggregationType()) {
                                    case ave:
                                        return s.stream()
                                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                                .average()
                                                .orElse(0);
                                    case sum:
                                        //求和
                                        return s.stream()
                                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                                .sum();
                                    case max:
                                        return s.stream()
                                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                                .max()
                                                .orElse(0);
                                    case min:
                                        return s.stream()
                                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                                .min()
                                                .orElse(0);
                                    case count:
                                        //计数
                                        return s.size();
                                    default:
                                        return 0;
                                }
                            })
                            .collect(Collectors.toList());
                    series.add(new ComponentChartLineRender.SeriesItem().setName(field).setData(collect));
                }
                render.setSeries(series);
                //分级数据集
                render.setXAxis(new ComponentChartLineRender.XAxis().setData(times));
                return render;

            }
        }
        return render;
    }

    @Override
    public List<SelectedAttribute> fieldInitOrLink(String key, DataChartLineParamsBase paramsDtoMap) {
        return super.field(key, paramsDtoMap.getModelIdentifier());
    }
}
