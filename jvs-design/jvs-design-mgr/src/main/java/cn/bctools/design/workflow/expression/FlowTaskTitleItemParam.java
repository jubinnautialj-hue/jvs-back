package cn.bctools.design.workflow.expression;

import cn.bctools.common.utils.function.Get;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流自定义流程任务标题设计可用流程任务字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "流程字段", useCase = {EnvConstant.FLOW_TASK_TITLE_ITEM_VALUE})
@AllArgsConstructor
public class FlowTaskTitleItemParam implements IJvsParam<ElementVo> {
    private static final List<ElementVo> PARAMS = new ArrayList<>();

    static {
        PARAMS.addAll(buildWorkflowElement().stream().map(param ->
                new ElementVo()
                        .setId(param.getId())
                        .setName(param.getName())
                        .setInfo(param.getInfo())
                        .setJvsParamType(param.getJvsParamType())
        ).collect(Collectors.toList()));
    }

    @Override
    public List<ElementVo> getAllElements() {
        return PARAMS;
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return null;
    }


    /**
     * 构造工作流字段
     *
     * @return 工作流字段
     */
    private static List<ElementVo> buildWorkflowElement() {
        List<ElementVo> elementVos = new ArrayList<>();
        Function<String, String> keyFunction = keyName -> SystemConstant.TASK_DATA_FIELD + StringPool.DOT + keyName;
        elementVos.add(new ElementVo().setId(keyFunction.apply(Get.name(FlowTask::getName))).setName("流程名称"));
        elementVos.add(new ElementVo().setId(keyFunction.apply(Get.name(FlowTask::getTaskCode))).setName("流程编号"));
        elementVos.add(new ElementVo().setId(keyFunction.apply(Get.name(FlowTask::getCreateBy))).setName("发起人"));
        elementVos.add(new ElementVo().setId(keyFunction.apply(Get.name(FlowTask::getCreateTime))).setName("发起时间"));
        return elementVos;
    }

}
