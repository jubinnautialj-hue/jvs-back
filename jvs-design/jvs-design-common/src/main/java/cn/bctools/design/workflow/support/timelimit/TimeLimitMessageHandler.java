package cn.bctools.design.workflow.support.timelimit;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.TimeLimitTypeEnum;
import cn.bctools.design.workflow.model.properties.TimeLimit;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ZhuXiaoKang
 * 发布审批期限超时校验消息
 */
@Slf4j
@Component
@AllArgsConstructor
public class TimeLimitMessageHandler {

    private final StreamBridge streamBridge;

    /**
     * 一分钟毫秒数
     */
    public static final int ONE_MINUTE_MILL = 60 * 1000;

    /**
     * 一小时毫秒数
     */
    public static final int ONE_HOUR_MILL = 60 * ONE_MINUTE_MILL;


    /**
     * 一天毫秒数
     */
    public static final int ONE_DAY_MILL = 24 * ONE_HOUR_MILL;


    /**
     * 发送延迟任务
     * @param node 当前节点
     * @param flowTask 任务
     * @param flowTaskPersons 待办人信息集合
     */
    @Async
    public void delayedTask(Node node, FlowTask flowTask, List<FlowTaskPerson> flowTaskPersons) {
        if (CollectionUtils.isEmpty(flowTaskPersons)) {
            log.info("待办人集合为空,不需要启动延迟任务");
            return;
        }
        // 期限值不存在或不大于0，则不处理
        Integer limit = Optional.ofNullable(node.getProps().getTimeLimit()).orElseGet(TimeLimit::new).getLimit();
        if (ObjectUtil.isNull(limit) || limit <= 0) {
            return;
        }
        // 计算延迟时间
        TimeLimit timeLimit = node.getProps().getTimeLimit();
        Integer timeLimitMill = calculateMill(timeLimit.getLimit(), timeLimit.getType());
        // 只发送待处理状态的审批人信息
        List<TimeLimitMessageDto> messageDtos = flowTaskPersons.stream()
                .filter(f -> ProcessStatusEnum.PENDING.equals(f.getProcessStatus()))
                .map(f -> {
                    TimeLimitMessageDto msgDto = new TimeLimitMessageDto();
                    msgDto.setFlowTaskName(flowTask.getName());
                    msgDto.setSendUserName(flowTask.getCreateBy());
                    msgDto.setFlowTaskPersonId(f.getId());
                    msgDto.setTimeLimit(node.getProps().getTimeLimit());
                    return msgDto;
                }).collect(Collectors.toList());
        // 发送到延迟队列
        messageDtos.forEach(dto -> send(timeLimitMill, dto));
    }

    /**
     * 计算延迟时间
     * @param limit 时限
     * @param typeEnum 时限单位
     * @return 延迟时间
     */
    public Integer calculateMill(Integer limit, TimeLimitTypeEnum typeEnum) {
        switch (typeEnum) {
            case MINUTE:
                // 分钟转毫秒
                return limit * ONE_MINUTE_MILL;
            case HOUR:
                // 小时转毫秒
                return limit * ONE_HOUR_MILL;
            case DAY:
                // 天转毫秒
                return limit * ONE_DAY_MILL;
            default:
                return 0;
        }
    }

    /**
     * 发布审批期限超时校验消息
     * @param timeLimitMill 延迟毫秒
     * @param dto 消息
     */
    public void send(Integer timeLimitMill, TimeLimitMessageDto dto) {
        streamBridge.send(TimeLimitMqConfig.TIME_LIMIT_PRODUCER,
                MessageBuilder
                        .withPayload(dto)
                        .setHeader("x-delay", timeLimitMill)
                        .build());
    }

}
