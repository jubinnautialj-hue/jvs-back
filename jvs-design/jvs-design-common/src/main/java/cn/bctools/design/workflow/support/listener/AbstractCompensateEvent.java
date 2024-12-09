package cn.bctools.design.workflow.support.listener;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.workflow.entity.FlowTaskEventCompensate;
import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import cn.bctools.design.workflow.service.FlowTaskEventCompensateService;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 事件补偿
 */
@Slf4j
public abstract class AbstractCompensateEvent {

    private static final Integer BATCH_SIZE = 1000;
    private static final Integer BATCH_MAX_TOTAL_SIZE = BATCH_SIZE * 20;
    private static final String COMPENSATE_CACHE = "flow:event:compensate:";
    private static final String COMPENSATE_LOCK = COMPENSATE_CACHE + "lock:";
    private final RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
    private final FlowTaskEventCompensateService flowTaskEventCompensateService = SpringContextUtil.getBean(FlowTaskEventCompensateService.class);

    /**
     * 定时执行事件补偿
     */
    public void timingCompensate(FlowTaskEventTypeEnum eventType) {
        // 每60秒查询一次
        CronUtil.schedule("*/59 * * * * *", (Task) () -> {
            try {
                compensate(eventType);
            } catch (Exception e) {
                log.error("定时执行事件补偿异常：{}", e.getMessage());
            }
        });
    }

    private void compensate(FlowTaskEventTypeEnum eventType) {
        String compensateKey = COMPENSATE_CACHE + eventType.getValue();
        // 已有服务正在执行事件补偿，直接返回
        if (redisUtils.exists(compensateKey)) {
            return;
        }
        log.debug("开始执行流程事件补偿服务...");
        iteratorCompensateEvent(eventType, 0);
    }

    /**
     * 迭代执行事件补偿
     *
     * @param eventType 事件类型
     * @param size 已处理事件数量
     */
    private void iteratorCompensateEvent(FlowTaskEventTypeEnum eventType, int size) {
        String compensateKey = COMPENSATE_CACHE + eventType.getValue();
        Page<FlowTaskEventCompensate> page = new Page<>(1, BATCH_SIZE);
        flowTaskEventCompensateService.page(page, Wrappers.<FlowTaskEventCompensate>lambdaQuery()
                .eq(FlowTaskEventCompensate::getEventType, eventType)
                .orderByAsc(FlowTaskEventCompensate::getCreateTime));
        if (size >= BATCH_MAX_TOTAL_SIZE || page.getTotal() == 0) {
            redisUtils.del(compensateKey);
            return;
        }

        List<String> compensateIds = new ArrayList<>();
        for (FlowTaskEventCompensate compensate : page.getRecords()) {
            String id = compensate.getId();
            String lockKey = COMPENSATE_LOCK + id;
            boolean lock = redisUtils.tryLock(lockKey, 10 * 1000);
            try {
                if (Boolean.FALSE.equals(lock)) {
                    continue;
                }
                // 已存在,表示已处理
                if (redisUtils.sHasKey(COMPENSATE_CACHE, id)) {
                    continue;
                }
                redisUtils.sSet(COMPENSATE_CACHE, id);
                // 执行补偿
                compensateEvent(compensate.getEventBody());
                compensateIds.add(id);
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                redisUtils.unLock(lockKey);
            }
        }
        // 删除已执行补偿的事件
        if (ObjectNull.isNotNull(compensateIds)) {
            flowTaskEventCompensateService.removeByIds(compensateIds);
        }
        iteratorCompensateEvent(eventType, size + page.getRecords().size());
    }

    /**
     * 执行补偿
     *
     * @param eventBody 事件内容
     */
    public abstract void compensateEvent(String eventBody);
}
