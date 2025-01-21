package cn.bctools.design.index;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.service.ComponentCrudService;
import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import cn.bctools.index.design.render.ComponentCrudRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;

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


    /**
     * 定义实体类对象，用于传递值的对象数据
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class CrudBase extends FormQueryParamsBase {
        @FormFormQuery(label = "模型标识", prop = "modelIdentifier", desc = "选择模型的标识，应用管理[源码标示]进行配置", type = FormAttributeTypeEnum.select)
        public String modelIdentifier;
        @FormFormQuery(label = "聚合字段", prop = "field", desc = "选择字段", type = FormAttributeTypeEnum.selectMultiple, link = {"modelIdentifier"})
        public List<String> fields;
        @FormFormQuery(label = "页码", prop = "current", desc = "")
        public int current;
        @FormFormQuery(label = "大小", prop = "size", desc = "")
        public int size;
    }

    @Override
    public ComponentCrudRender fillData(CrudBase body) {
        ComponentCrudRender render = new ComponentCrudRender();
        if (ObjectNull.isNull(body.getModelIdentifier())) {
            return null;
        } else {
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
                    render.setData(maps);
                }
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
