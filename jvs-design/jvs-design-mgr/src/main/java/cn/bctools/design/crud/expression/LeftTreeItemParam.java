package cn.bctools.design.crud.expression;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 列表页字段
 * 左树右表时左树的按钮支持公式直根据左树的数据接判断是否显示隐藏控制
 *
 * @Author: GuoZi
 */
@Order(-2)
@Component
@JvsExpression(groupName = "左树字段", useCase = {EnvConstant.LEFT_TREE_BUTTON_DISPLAY})
@AllArgsConstructor
public class LeftTreeItemParam implements IJvsParam<ElementVo> {

    CrudPageService pageService;
    DataFieldService fieldService;

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return data.get(paramName);
    }

    @Override
    public List<ElementVo> getAllElements() {
        String designId = this.getDesignId();
        if (StringUtils.isBlank(designId)) {
            return Collections.emptyList();
        }
        // 根据表单设计获取数据模型id
        CrudPage crudPage = pageService.get(designId);
        if (Objects.isNull(crudPage)) {
            return Collections.emptyList();
        }
        String viewJson = crudPage.getViewJson();
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(viewJson);
        Optional<DataTableFieldDesignHtml> enableRetrieval = pageDesignHtml.getDataPage().getAutoTableFields().stream().filter(DataTableFieldDesignHtml::getEnableRetrieval).findFirst();
        if (enableRetrieval.isPresent()) {
            DataTableFieldDesignHtml dataTableFieldDesignHtml = enableRetrieval.get();
            // 根据数据模型获取字段
            Map<String, Object> designJson = dataTableFieldDesignHtml.getDesignJson();
            String modelId = designJson.getOrDefault("formId", "").toString();
            List<FieldBasicsHtml> fields = fieldService.getFields(crudPage.getJvsAppId(), modelId, true, true)
                    .stream().filter(e -> ObjectNull.isNotNull(e.getType()) && ObjectNull.isNotNull(e.getFieldName())).collect(Collectors.toList());
            return fields.stream()
                    .filter(field -> Objects.nonNull(field.getType()))
                    .map(this::fieldDto2ElementVo)
                    .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
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
