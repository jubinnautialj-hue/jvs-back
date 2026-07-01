package cn.bctools.design.rule.impl.workflow.termination;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.StopTaskReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.TaskService;
import cn.bctools.design.workflow.service.TaskStopService;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
@Rule(value = "终止流程",
        group = RuleGroup.服务插件,
        returnType = ClassType.对象,
        order = 2,
//        iconUrl = "rule-jituanOAliuchengxinzengxiudingshangxianshenpiliucheng",
        explain = "终止未结束的流程任务。 流程触发的逻辑，不支持执行终止流程节点，请在列表页或表单中配置终止流程"
)
@AllArgsConstructor
public class FlowTerminationServiceImpl implements BaseCustomFunctionInterface<FlowTerminationDto> {

    private static final String ID = "id";

    private final TaskService taskService;
    private final TaskStopService taskStopService;
    private final FlowDynamicDataService flowDynamicDataService;


    @Override
    public Object execute(FlowTerminationDto flowTerminationDto, Map<String, Object> params) {
        if (ObjectNull.isNull(flowTerminationDto.getData())) {
            throw new BusinessException("未配置流程参数值");
        }
        String dataId = Optional.ofNullable(flowTerminationDto.getData().get(ID)).map(String::valueOf).orElse(null);
        if (ObjectNull.isNull(flowTerminationDto.getData())) {
            throw new BusinessException("There_is_没有数据_id_in_the_process_parameter_value");
        }
        FlowTask flowTask = taskService.getPendingTask(dataId);
        // 流程任务已结束或不存在，不能执行终止
        if (ObjectNull.isNull(flowTask)) {
            return null;
        }
        FlowContext flowContext = FlowContextUtil.context().getContext();
        if (ObjectNull.isNotNull(flowContext)) {
            throw new BusinessException("流程触发的逻辑不支持执行终止流程节点请在列表页或表单中配置终止流程");
        }
        // 终止任务
        StopTaskReqDto stopTaskReqDto = new StopTaskReqDto();
        stopTaskReqDto.setReason(flowTerminationDto.getReason());
        FlowTask task = taskStopService.terminationTask(UserCurrentUtils.getCurrentUser(), flowTask, stopTaskReqDto);
        // 执行终止任务后，更新任务进度到集合（这里直接更新，不用事件通知更新，是为了支持结束流程之后，在同一个逻辑中可以再发起流程任务，避免流程状态同步错乱）
        flowDynamicDataService.saveTaskToModel(Collections.singletonList(task));
        return null;
    }
}
