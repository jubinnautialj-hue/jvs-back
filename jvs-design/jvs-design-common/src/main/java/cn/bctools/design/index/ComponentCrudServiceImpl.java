package cn.bctools.design.index;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.service.ComponentCrudService;
import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import cn.bctools.index.design.render.ComponentCrudRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Service
@Slf4j
@AllArgsConstructor
public class ComponentCrudServiceImpl extends DataModelIndexFieldOrLink implements ComponentCrudService<ComponentCrudServiceImpl.CrudBase> {

    JvsAppService jvsAppService;
    DynamicDataService dynamicDataService;
    DataFieldService dataFieldService;
    IdentificationService identificationService;
    DataModelService dataModelService;
    AppMenuService menuService;


    /**
     * 定义实体类对象，用于传递值的对象数据
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class CrudBase extends FormQueryParamsBase {
        @FormFormQuery(label = "模型标识", prop = "modelIdentifier", desc = "选择模型的标识，应用管理[源码标示]进行配置", type = FormAttributeTypeEnum.select)
        public String modelIdentifier;
        @FormFormQuery(label = "字段", prop = "field", desc = "选择字段", type = FormAttributeTypeEnum.selectMultiple, link = {"modelIdentifier"})
        public List<String> fields;
        //        @FormFormQuery(label = "页码", prop = "current", desc = "")
        public int current;
        //        @FormFormQuery(label = "大小", prop = "size", desc = "")
        public int size;
        /**
         * 列表信息
         */
        public static List<CrudPage> pages;
    }

    @Override
    public ComponentCrudRender fillData(CrudBase body) {
        ComponentCrudRender render = new ComponentCrudRender();
        if (ObjectNull.isNull(body.getModelIdentifier())) {
            return null;
        } else {
            ModeUtils.setMode();
            String modelId = identificationService.getIdentificationModel(body.getModelIdentifier()).stream().findFirst().map(Identification::getDesignId).orElse(null);
            if (ObjectNull.isNotNull(modelId)) {
                DataModelPo model = dataModelService.getModel(modelId);
                List<FieldBasicsHtml> fields = dataFieldService.getFields(model.getAppId(), model.getId(), true, true);
                //加载模型的数据权限
                DynamicDataUtils.dataModelScope(model.getId());
                List<String> fieldList = new ArrayList<>();
                fieldList.add("createTime");
                if (ObjectNull.isNotNull(body.getFields())) {
                    fieldList.addAll(body.getFields());
                    Page page = new Page(body.current, body.size);
                    Page maps = dynamicDataService.queryPage(model.getAppId(), page, modelId, new HashMap<>(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            fieldList,
                            true, false, true,
                            fields);
                    if (ObjectNull.isNotNull(maps.getRecords())) {
                        Map<String, FieldBasicsHtml> text = new HashMap<>();
                        //判断是否需要转换
                        Map<String, FieldBasicsHtml> allField = dataFieldService.getAllField(model.getAppId(), model.getId(), true, false)
                                .stream()
                                .peek(e -> {
                                    if (e.getType().equals(DataFieldType.htmlEditor)) {
                                        text.put(e.getFieldKey(), e);
                                    }
                                })
                                .collect(Collectors.toMap(FieldPublicHtml::getFieldKey, Function.identity()));
                        maps.getRecords().forEach(e -> {
                            e = dynamicDataService.echo((Map<String, Object>) e, allField, true);
                            if (ObjectNull.isNotNull(text)) {
                                for (String a : text.keySet()) {
                                    if (((Map<String, Object>) e).containsKey(a)) {
                                        Object o = ((Map<?, ?>) e).get(a);
                                        if (ObjectNull.isNotNull(o)) {
                                            String s = HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(o.toString()));
                                            ((Map<String, Object>) e).put(a, s);
                                        }
                                    }
                                }
                            }
                        });
                    }
                    render.setData(maps);
                }
                List collect =
                        menuService.list(Wrappers.query(new AppMenu().setDesignType(DesignType.page).setDataModelId(modelId))).stream().map(e -> new CrudPage().setName(e.getName()).setDataModelId(e.getDataModelId()).setJvsAppId(e.getJvsAppId()).setId(e.getDesignId())).collect(Collectors.toList());
                render.setPages(collect);
                return render;
            }
        }
        return render;
    }

    @Override
    public List<SelectedAttribute> fieldInitOrLink(String key, CrudBase paramsDtoMap) {
        return super.field(key, paramsDtoMap.getModelIdentifier());
    }

}
