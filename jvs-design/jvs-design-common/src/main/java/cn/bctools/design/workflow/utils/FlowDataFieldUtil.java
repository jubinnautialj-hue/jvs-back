package cn.bctools.design.workflow.utils;

import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流数据模型字段
 */
@Component
@AllArgsConstructor
public class FlowDataFieldUtil {

    private final DataFieldService dataFieldService;

    /**
     * 修改工作流数据模型字段
     *
     * @param designId    工作流设计id
     * @param dataModelId 数据模型id
     */
    public void updateFlowDefaultFields(String appId, String designId, String dataModelId) {
        dataFieldService.updateFields(appId, designId, DesignType.workflow, dataModelId, buildFieldDtos());
    }

    /**
     * 构造字段集合
     *
     * @return 工作流字段集合
     */
    private List<DataFieldPo> buildFieldDtos() {
        List<DataFieldPo> fields = new ArrayList<>();
        fields.add(buildFlowTaskStateField());
        fields.add(buildFlowTaskProgressField());
        return fields;
    }

    /**
     * 构建工作流任务状态字段
     *
     * @return 工作流任务状态字段
     */
    private DataFieldPo buildFlowTaskStateField() {
        DataFieldPo fieldPo = new DataFieldPo();
        fieldPo.setFieldKey(FlowDataFieldEnum.TASK_STATE.getFieldKey())
                .setFieldName(FlowDataFieldEnum.TASK_STATE.getFieldName())
                .setFieldType(DataFieldType.select);
        JSONArray enabledQueryTypes = new JSONArray();
        enabledQueryTypes.add(DataQueryType.eq);
        return fieldPo.setEnabledQueryTypes(enabledQueryTypes);
    }

    /**
     * 构建工作流任务进度描述
     *
     * @return 构建工作流任务进度描述字段
     */
    private DataFieldPo buildFlowTaskProgressField() {
        DataFieldPo fieldPo = new DataFieldPo();
        fieldPo
                .setFieldKey(FlowDataFieldEnum.TASK_PROGRESS.getFieldKey())
                .setFieldName(FlowDataFieldEnum.TASK_PROGRESS.getFieldName())
                .setFieldType(DataFieldType.input);
        JSONArray enabledQueryTypes = new JSONArray();
        enabledQueryTypes.add(DataQueryType.like);
        return fieldPo.setEnabledQueryTypes(enabledQueryTypes);
    }

}
