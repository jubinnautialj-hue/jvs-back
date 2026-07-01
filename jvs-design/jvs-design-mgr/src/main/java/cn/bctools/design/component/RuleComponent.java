package cn.bctools.design.component;

import cn.bctools.common.utils.R;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.use.api.RuleApi;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * @author jvs
 * The type Rule component.
 */
@Slf4j
@RestController
@Api(tags = "[Feign]逻辑引擎")
@AllArgsConstructor
public class RuleComponent implements RuleApi {


    public static ThreadLocal<SseEmitter> sseEmitterTransmittableThreadLocal = new TransmittableThreadLocal<>();

    /**
     * The Rule run service.
     */
    RuleRunService ruleRunService;

    RuleDesignService ruleService;

    @Override
    public R run(String key, Map<String, Object> dataMap) {
        RuleExecuteDto run = ruleRunService.run(key, dataMap);
        return R.ok(run);
    }

    /**
     * 事件返回逻辑的结果值
     */
    @SneakyThrows
    public Object event(String key, Map<String, Object> dataMap) {
        SseEmitter emitter = new SseEmitter();
        sseEmitterTransmittableThreadLocal.set(emitter);
        RuleDesignPo po = ruleService.getEnableDesign(key);
        // 发送JSON数据
        emitter.send(
                SseEmitter.event()
                        .name("message")
                        .data(R.ok("开始处理逻辑"))
        );
        ruleRunService.run(key, dataMap);
        //表示完成
        emitter.complete();
        sseEmitterTransmittableThreadLocal.remove();
        return emitter;
    }

}
