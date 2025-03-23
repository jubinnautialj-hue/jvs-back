package cn.bctools.design.crud.expression;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.vo.PrintOtherField;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;
import cn.bctools.design.workflow.service.ApprovalRecordService;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import cn.bctools.word.utils.WordVariableReplaceUtil;
import cn.hutool.core.util.StrUtil;
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
@JvsExpression(groupName = "流程字段", useCase = {EnvConstant.PRINT_FILE_OTHER_ITEM_VALUE})
@AllArgsConstructor
public class PrintFileOtherItemParam implements IJvsParam<ElementVo> {

    private enum OtherField {
        // 工作流进度（表格）
        workflowProgress
    }

    FormService formService;
    DataFieldService fieldService;
    DataModelService dataModelService;
    Map<String, IDataFieldHandler> handlerMap;
    ApprovalRecordService approvalRecordService;

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
     * @param dataModelPo 数据模型
     * @param elementVos 字段集合
     */
    private void buildWorkflowElement(DataModelPo dataModelPo, List<ElementVo> elementVos) {
        if (StringUtils.isBlank(dataModelPo.getWorkflowId()) || Boolean.FALSE.equals(dataModelPo.getEnableWorkflow())) {
            return;
        }
        // 各节点审批信息字段
        addWorkflowNodeApprove(dataModelPo, elementVos);
        // 工作流进度字段
        addWorkflowProgress(elementVos);
    }

    /**
     * 添加流程进度字段
     * <p>
     *     可按表格形式输出流程信息
     *
     * @param elementVos 字段集合
     */
    private void addWorkflowProgress(List<ElementVo> elementVos) {
        String progressTableKey = WordVariableReplaceUtil.buildTableKey(PrintOtherField.Flow.WORKFLOW_PROGRESS.getKey());
        String nodeNameKey = WordVariableReplaceUtil.buildVariableKey(PrintOtherField.Flow.NODE_NAME.getKey());
        String userNameKey = WordVariableReplaceUtil.buildVariableKey(PrintOtherField.Flow.USER_NAME.getKey());
        String operationKey = WordVariableReplaceUtil.buildVariableKey(PrintOtherField.Flow.OPERATION.getKey());
        String contentKey = WordVariableReplaceUtil.buildVariableKey(PrintOtherField.Flow.CONTENT.getKey());
        String timeKey = WordVariableReplaceUtil.buildVariableKey(PrintOtherField.Flow.TIME.getKey());
        String signKey = WordVariableReplaceUtil.buildVariableKey(WordVariableReplaceUtil.DataType.IMAGE, PrintOtherField.Flow.SIGN.getKey());
        String progressTable = PrintOtherField.Flow.WORKFLOW_PROGRESS.getName();
        String nodeName = progressTable + StrUtil.DOT + PrintOtherField.Flow.NODE_NAME.getName();
        String userName = progressTable + StrUtil.DOT + PrintOtherField.Flow.USER_NAME.getName();
        String operation = progressTable + StrUtil.DOT + PrintOtherField.Flow.OPERATION.getName();
        String content = progressTable + StrUtil.DOT + PrintOtherField.Flow.CONTENT.getName();
        String time = progressTable + StrUtil.DOT + PrintOtherField.Flow.TIME.getName();
        String sign = progressTable + StrUtil.DOT + PrintOtherField.Flow.SIGN.getName();
        elementVos.add(new ElementVo().setId(progressTableKey).setName(progressTable));
        elementVos.add(new ElementVo().setId(nodeNameKey).setName(nodeName));
        elementVos.add(new ElementVo().setId(userNameKey).setName(userName));
        elementVos.add(new ElementVo().setId(operationKey).setName(operation));
        elementVos.add(new ElementVo().setId(contentKey).setName(content));
        elementVos.add(new ElementVo().setId(timeKey).setName(time));
        elementVos.add(new ElementVo().setId(signKey).setName(sign));

    }

    /**
     * 流程节点审批信息字段
     *
     * @param dataModelPo 数据模型
     * @param elementVos 字段集合
     */
    private void addWorkflowNodeApprove(DataModelPo dataModelPo, List<ElementVo> elementVos) {
        List<ApprovalRecordFieldDto> approvalRecordFieldList = approvalRecordService.getApprovalRecordFields(dataModelPo.getWorkflowId());
        if (ObjectNull.isNull(approvalRecordFieldList)) {
            return;
        }
        approvalRecordFieldList.forEach(field -> {
            // 目前只有上传图片类型的字段需要指定打印字段类型，其它字段默认为文本
            WordVariableReplaceUtil.DataType dataType = null;
            if (DataFieldType.imageUpload.equals(field.getDataFieldType()) || DataFieldType.signature.equals(field.getDataFieldType())) {
                dataType = WordVariableReplaceUtil.DataType.IMAGE;
            }
            elementVos.add(new ElementVo().setId(WordVariableReplaceUtil.buildVariableKey(dataType, field.getFieldKey())).setName(field.getFieldName()));
        });
    }

}
