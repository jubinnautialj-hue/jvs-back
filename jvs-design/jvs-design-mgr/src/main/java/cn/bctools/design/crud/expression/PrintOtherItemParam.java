package cn.bctools.design.crud.expression;

import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.vo.PrintFormElementVo;
import cn.bctools.design.crud.vo.PrintOtherElementVo;
import cn.bctools.design.crud.vo.PrintOtherField;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 表单文件打印模板字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "其它字段", useCase = {EnvConstant.PRINT_OTHER_ITEM_VALUE})
@AllArgsConstructor
public class PrintOtherItemParam implements IJvsParam<ElementVo> {

    FormService formService;
    DataModelService dataModelService;

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return null;
    }

    @Override
    public List<ElementVo> getAllElements() {
        String designId = this.getDesignId();
        List<ElementVo> list = getElementVo(designId);
        return list;
    }

    private List<ElementVo> getElementVo(String designId) {
        if (StringUtils.isBlank(designId)) {
            return Collections.emptyList();
        }
        String dataModelId = designId;
        try {
            // 根据表单设计获取数据模型id
            dataModelId = formService.get(designId).getDataModelId();
        } catch (Exception e) {
            //兼容表单设计规则
            log.error("根据表单设计获取数据模型id异常：{}", e.getMessage());
        }
        DataModelPo dataModelPo = dataModelService.getById(dataModelId);
        List<ElementVo> elementVos = new ArrayList<>();
        // 构造工作流相关字段
        buildWorkflowElement(dataModelPo, elementVos);
        return elementVos;
    }

    /**
     * 构造工作流相关打印设计字段
     * @param dataModelPo
     * @param elementVos
     */
    private void buildWorkflowElement(DataModelPo dataModelPo, List<ElementVo> elementVos) {
        if (StringUtils.isBlank(dataModelPo.getWorkflowId()) || Boolean.FALSE.equals(dataModelPo.getEnableWorkflow())) {
            return;
        }
        // 工作流进度字段
        List<ElementVo> workFlowProgressTables = new ArrayList<>();
        workFlowProgressTables.add(new ElementVo().setId(PrintOtherField.Flow.NODE_NAME.getKey()).setName(PrintOtherField.Flow.NODE_NAME.getName()));
        workFlowProgressTables.add(new ElementVo().setId(PrintOtherField.Flow.USER_NAME.getKey()).setName(PrintOtherField.Flow.USER_NAME.getName()));
        workFlowProgressTables.add(new ElementVo().setId(PrintOtherField.Flow.OPERATION.getKey()).setName(PrintOtherField.Flow.OPERATION.getName()));
        workFlowProgressTables.add(new ElementVo().setId(PrintOtherField.Flow.CONTENT.getKey()).setName(PrintOtherField.Flow.CONTENT.getName()));
        workFlowProgressTables.add(new ElementVo().setId(PrintOtherField.Flow.TIME.getKey()).setName(PrintOtherField.Flow.TIME.getName()));
        ElementVo workFlowProgressField = new ElementVo();
        workFlowProgressField.setId(PrintOtherField.Flow.WORKFLOW_PROGRESS.getKey());
        workFlowProgressField.setName("审批过程");
        workFlowProgressField.setShortName(workFlowProgressField.getName());
        workFlowProgressField.setFieldType(DataFieldType.tableForm.name());
        workFlowProgressField.getOther().put(Get.name(PrintOtherElementVo::getDataSourceType), PrintOtherField.DataSourceType.workflowProgress.name());
        workFlowProgressField.getOther().put(Get.name(PrintFormElementVo::getTableFields), workFlowProgressTables);
        elementVos.add(workFlowProgressField);
    }
}
