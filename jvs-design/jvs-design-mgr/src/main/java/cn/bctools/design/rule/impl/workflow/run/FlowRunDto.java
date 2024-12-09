package cn.bctools.design.rule.impl.workflow.run;

import cn.bctools.design.rule.impl.DataModelFormLinkSelected;
import cn.bctools.design.rule.impl.datamodel.DataModelSelected;
import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FlowRunDto {

    @ParameterValue(info = "请选OA流程", type = InputType.selected, explain = "只能选择当前应用下的OA流程,支持表单、列表按钮、定时任务执行指定流程", cls = FlowRunParameterSelected.class)
    @NotNull(message = "OA流程不能为空")
    public String workflow;

    @ParameterValue(info = "流程参数值", type = InputType.map, explain = "流程请求参数对象,启动流程参数包含基础数据、模型、发起人表单")
    public Map<String, Object> data;

    @ParameterValue(info = "关联动态流程对象", necessity = false, type = InputType.flowNode, explain = "通过具体的数据指定每一个节点的审核人。示例数据:<br/>[{\"approvers\":[{\"id\":\"1\",\"name\":\"用户 1\",\"type\":\"user\"}],\"nodeId\":\"nodeId\"}]")
    public List<SelfSelectApprover> approvers;

    @ParameterValue(info = "关联动态节点", necessity = false, type = InputType.expressionText, explain = "流程动态节点,用于开启或重启时动态节点数据,建议使用公式直接获取字段公式")
    public Object node;

    @ParameterValue(info = "数据模型", explain = "请选择流程需要操作的数据模型,在流程参数值中包含数据模型时可以忽略此配置信息，默认将使用流程参数值的模型。", necessity = false, type = InputType.selected, cls = DataModelSelected.class, linkFields = {"sendFormId"}, linkCls =
            DataModelFormLinkSelected.class)
    public String dataModelId;

    @ParameterValue(info = "流程发起节点表单", explain = "指定发起人表单,如果逻辑为表单触发或已经配置好发起人表单可以忽略此配置信息。", necessity = false, type = InputType.selected)
    public String sendFormId;

    @ParameterValue(info = "流程发起人", necessity = false, type = InputType.user, explain = "流程发起人通常是登陆后的操作人,在这里指定发起人,则会以指定的人员发起流程")
    public String userId;

}

