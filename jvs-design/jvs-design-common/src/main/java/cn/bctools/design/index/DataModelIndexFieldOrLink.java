package cn.bctools.design.index;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.index.design.SelectedAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 * 获取字段，都是获取模型的字段，抽象为一个方法
 */
public class DataModelIndexFieldOrLink {

    /**
     * Field list.
     *
     * @param key             the key
     * @param modelIdentifier the model identifier
     * @return the list
     */
    List<SelectedAttribute> field(String key, String modelIdentifier) {
        //获取所有的模型标识,用户直接选择模型的标识
        IdentificationService identificationService = SpringContextUtil.getBean(IdentificationService.class);
        DataFieldService fieldService = SpringContextUtil.getBean(DataFieldService.class);
        List<SelectedAttribute> dicData = identificationService.getIdentificationModel()
                .stream()
                .filter(e -> e.getDesignType().equals(DesignType.data))
                .map(e -> new SelectedAttribute().setLabel(e.getDesignName()).setValue(e.getIdentifier()))
                .collect(Collectors.toList());
        switch (key) {
            case "modelIdentifier":
                //搜索应用名，只返回对应的数据
                return dicData;
            case "aggregationField":
            case "groupField":
                //获取这个设计的字段
                //获取标识是哪一个应用 id
                return identificationService.getIdentificationModel(modelIdentifier).stream()
                        .findFirst()
                        .map(e -> fieldService.getAllField(e.getJvsAppId(), e.getDesignId(), false, true).stream()
                                .filter(s -> ObjectNull.isNotNull(s.getLabel()))
                                .map(s -> new SelectedAttribute().setLabel(s.getLabel()).setValue(s.getProp()))
                        )
                        .map(e -> e.collect(Collectors.toList())).orElse(new ArrayList<>());
            case "aggregationType":
                return Arrays.stream(AggregateEnumType.values()).map(e -> new SelectedAttribute().setLabel(e.name()).setValue(e.name())).collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }
}
