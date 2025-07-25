package cn.bctools.design.rule.impl.event;

import cn.bctools.common.utils.R;
import cn.bctools.design.component.RuleComponent;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.TaskLogUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;


/**
 * @author jvs
 */
@Service
@AllArgsConstructor
@Rule(value = "流式事件响应",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 4,
//        iconUrl = "rule-slsrizhifuwu",
        explain = "支持Excel和txt文件内容解析,不支持公式和函数,表头合并."
)
public class EventServiceImpl implements BaseCustomFunctionInterface<EventDto> {

    @SneakyThrows
    @Override
    public Object execute(EventDto eventDto, Map<String, Object> params) {
        try {
            SseEmitter emitter = RuleComponent.sseEmitterTransmittableThreadLocal.get();
            // 发送JSON数据
            emitter.send(
                    SseEmitter.event()
                            .name(eventDto.getName())
                            .data(R.ok(eventDto.getContent()))
            );
            LOG.info("测试和定时任务不支持发送事件消息");
            return eventDto.getContent();
        } catch (Exception e) {
            LOG.error("测试和定时任务不支持发送事件消息，发送消息失败", e);
            return "发送消息失败";
        }
    }
}
