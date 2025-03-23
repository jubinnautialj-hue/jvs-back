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
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.ComponentDataCard;
import cn.bctools.index.design.component.service.ComponentBaseService;
import cn.bctools.index.design.component.service.ComponentDataCardService;
import cn.bctools.index.design.enums.ComponentType;
import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import cn.bctools.index.design.render.ComponentDataCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 * 数据统计图组件
 */
@Slf4j
@Service
@AllArgsConstructor
public class ComponentDataCardServiceImpl extends DataModelIndexFieldOrLink implements ComponentDataCardService<ComponentDataCardServiceImpl.DataStatisticsParamsBase> {

    JvsAppVersionService appVersionService;
    IdentificationService identificationService;
    JvsAppService jvsAppService;
    DataFieldService fieldService;
    DataModelService dataModelService;
    DynamicDataService dynamicDataService;

    /**
     * 定义实体类对象，用于传递值的对象数据
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class DataStatisticsParamsBase extends FormQueryParamsBase {
        @FormFormQuery(label = "模型标识", prop = "modelIdentifier", desc = "选择模型的标识，应用管理[源码标示]进行配置", type = FormAttributeTypeEnum.select)
        public String modelIdentifier;
        @FormFormQuery(label = "聚合字段", prop = "aggregationField", desc = "选择聚合处理的字段", type = FormAttributeTypeEnum.select, link = {"modelIdentifier"})
        public String aggregationField;
        @FormFormQuery(label = "聚合方式", prop = "aggregationType", desc = "选择聚合的方式，包含最大、最小、平均、求和等", type = FormAttributeTypeEnum.select)
        public AggregateEnumType aggregationType;
    }

    @Override
    public ComponentDataCardRender fillData(DataStatisticsParamsBase body) {
        ModeUtils.setMode();
        ComponentDataCardRender componentDataCardRender = new ComponentDataCardRender();
        if (ObjectNull.isNull(body.getModelIdentifier(), body.getAggregationType())) {
            return null;
        } else {
            String modelId = identificationService.getIdentificationModel(body.getModelIdentifier()).stream().findFirst().map(Identification::getDesignId).orElse(null);
            if (ObjectNull.isNotNull(modelId)) {
                List<QueryConditionDto> queryConditions = new ArrayList<>();
                DataModelPo model = dataModelService.getModel(modelId);
                //加载模型的数据权限
                DynamicDataUtils.handleDesignDataScope(model.getRoles());
                Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
                List<Criteria> criteriaList = DynamicDataUtils.getAuthCriteria();
                if (ObjectNull.isNotNull(criteriaList)) {
                    //添加数据权限操作
                    criteria = DynamicDataUtils.initCriteria(criteria).andOperator(criteriaList);
                }
                List<String> fieldList = new ArrayList<>();
                fieldList.add(body.getAggregationField());
                fieldList.add("createTime");
                List<Map<String, Object>> list = dynamicDataService.queryList(modelId, criteria, fieldList);
                switch (body.getAggregationType()) {
                    case ave:
                        double asDouble = list.stream().mapToDouble(e -> Double.parseDouble(e.getOrDefault(body.getAggregationField(), 0).toString())).average().orElse(0);
                        componentDataCardRender.setData(new BigDecimal(asDouble).setScale(2, 2));
                        return componentDataCardRender;
                    case sum:
                        double sum = list.stream().mapToDouble(e -> Double.parseDouble(e.getOrDefault(body.getAggregationField(), 0).toString()))
                                .sum();
                        componentDataCardRender.setData(new BigDecimal(sum).setScale(2, 2));
                        return componentDataCardRender;
                    case max:
                        double max = list.stream()
                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                .max()
                                .orElse(0);
                        componentDataCardRender.setData(new BigDecimal(max).setScale(2, 2));
                        return componentDataCardRender;
                    case min:
                        double min = list.stream()
                                .mapToDouble(a -> Double.parseDouble(a.getOrDefault(body.getAggregationField(), 0).toString()))
                                .min()
                                .orElse(0);
                        componentDataCardRender.setData(new BigDecimal(min).setScale(2, 2));
                        return componentDataCardRender;
                    case count:
                        componentDataCardRender.setData(new BigDecimal(list.size()).setScale(0));
                        return componentDataCardRender;
                    default:
                }
            }
        }
        return componentDataCardRender;
    }

    @Override
    public List<SelectedAttribute> fieldInitOrLink(String key, DataStatisticsParamsBase body) {
        return super.field(key, body.getModelIdentifier());
    }

    @Override
    public ComponentDataCard generate() {
        Class<? extends ComponentBaseInfo> aClass = ComponentType.serverClass((Class<? extends ComponentBaseService>) this.getClass().getInterfaces()[0]);
        ComponentDataCard componentDataCard = new ComponentDataCard();
        ComponentType componentType = ComponentType.bClass(aClass);
        componentDataCard.setType(componentType);
        componentDataCard.setName(componentType.getDesc());
        componentDataCard.setKey("model_key");
        componentDataCard.setEnableCache(true);
        componentDataCard.setIntervalTime(Long.valueOf(60 * 30));
        return componentDataCard;
    }
}
