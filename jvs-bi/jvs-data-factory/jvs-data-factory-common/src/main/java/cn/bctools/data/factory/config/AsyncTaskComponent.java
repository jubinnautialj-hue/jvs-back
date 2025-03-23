package cn.bctools.data.factory.config;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.StackTraceElementUtils;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import cn.bctools.data.factory.enums.ImPushTypeEnums;
import cn.bctools.data.factory.html.FHtmlGraph;
import cn.bctools.data.factory.im.MessageImPush;
import cn.bctools.data.factory.listener.task.FactoryTaskEvent;
import cn.bctools.data.factory.notice.dto.NoticeCacheDto;
import cn.bctools.data.factory.notice.enums.TriggerTypeEnum;
import cn.bctools.data.factory.service.JvsDataFactoryLogService;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.util.RabbitMqUtils;
import cn.bctools.data.factory.util.SystemTool;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 异步执行任务
 *
 * @author xiaohui
 */
@Slf4j
@Service
@AllArgsConstructor
public class AsyncTaskComponent {
    private final JvsDataFactoryService jvsDataFactoryService;
    private final JvsDataFactoryLogService factoryLogService;
    private final JvsDataFactoryQueueService jvsDataFactoryQueueService;
    private final MessageImPush messageImPush;
    private final RabbitTemplate rabbitTemplate;

    public void executeTask(FactoryTaskEvent factoryTaskEvent) {
        log.info("执行开始，入参为:{}", JSONObject.toJSONString(factoryTaskEvent));
        //转换类
        //设置租户信息
        TenantContextHolder.setTenantId(factoryTaskEvent.getTenantId());
        JvsDataFactory dataFactory = jvsDataFactoryService.getById(factoryTaskEvent.getDataFactoryId());
        //修改队列表中的状态 并判断是否需要停止
        JvsDataFactoryQueue jvsDataFactoryQueue = jvsDataFactoryQueueService.getById(factoryTaskEvent.getDataFactoryQueueId());
        if (jvsDataFactoryQueue.getTaskStatus().equals(QueueTaskStatusEnum.STOP)) {
            log.info("任务被更改为停止状态,直接退出!");
            jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                    .eq(JvsDataFactoryQueue::getId, jvsDataFactoryQueue.getId())
                    .set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.FAIL)
                    .set(JvsDataFactoryQueue::getErrorMsg, "手动停止，此任务")
                    .set(JvsDataFactoryQueue::getEndTime, LocalDateTime.now()));
            return;
        }
        if (!jvsDataFactoryQueue.getTaskStatus().equals(QueueTaskStatusEnum.QUEUE)) {
            log.info("此任务已经执行!");
            return;
        }
        jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                .eq(JvsDataFactoryQueue::getId, jvsDataFactoryQueue.getId())
                .set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.EXECUTE)
                .set(JvsDataFactoryQueue::getExecuteSchedule, "0.00%"));
        //websocket 通知
        messageImPush.notify(ImPushTypeEnums.updateValue, "0.00%", jvsDataFactoryQueue.getId());
        if (dataFactory == null) {
            log.info("------通过消息中的id未找到此数据,数据id为:{}", factoryTaskEvent.getDataFactoryId());
            jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                    .eq(JvsDataFactoryQueue::getId, jvsDataFactoryQueue.getId())
                    .set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.FAIL)
                    .set(JvsDataFactoryQueue::getErrorMsg, "未找到此数据"));
        } else {
            try {
                this.exec(factoryTaskEvent, dataFactory, jvsDataFactoryQueue);
            } catch (Exception e) {
                log.info("------------执行任务错误", e);
                String error = StackTraceElementUtils.logThrowableToString(e);
                jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                        .eq(JvsDataFactoryQueue::getId, factoryTaskEvent.getDataFactoryQueueId())
                        .set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.FAIL)
                        .set(JvsDataFactoryQueue::getErrorMsg, error));
                this.pushMessage(factoryTaskEvent.getUserDto().getRealName(), QueueTaskStatusEnum.FAIL, dataFactory.getId(), factoryTaskEvent.getOperateMethod());
            } finally {
                execFinally(factoryTaskEvent, dataFactory);
            }
        }

    }

    /**
     * 不管主任务是否执行完成 都需要执行的代码
     *
     * @param dataFactory      数据集对象
     * @param factoryTaskEvent 队列消息
     */
    private void execFinally(FactoryTaskEvent factoryTaskEvent, JvsDataFactory dataFactory) {
        //判断是否需要生成下一个任务
        if (factoryTaskEvent.getQueueTaskType().equals(QueueTaskTypeEnum.PREFIX_TASK)) {
            //获取所有前置任务是否执行完成
            List<JvsDataFactoryQueue> list = jvsDataFactoryQueueService.list(new LambdaQueryWrapper<JvsDataFactoryQueue>()
                    .eq(JvsDataFactoryQueue::getQueueTaskType, QueueTaskTypeEnum.PREFIX_TASK)
                    .eq(JvsDataFactoryQueue::getBatchId, factoryTaskEvent.getBatchId()));
            //判断是否已经执行完成
            long count = list.stream().filter(e -> e.getTaskStatus().equals(QueueTaskStatusEnum.EXECUTE) || e.getTaskStatus().equals(QueueTaskStatusEnum.QUEUE)).count();
            if (count == 0) {
                //所有前置执行完成后判断是否所有前置任务都执行成功 都执行成功就生成任务本身
                count = list.stream().filter(e -> !e.getTaskStatus().equals(QueueTaskStatusEnum.ACCOMPLISH)).count();
                if (count > 0) {
                    UserDto userDto = factoryTaskEvent.getUserDto();
                    JvsDataFactoryLog entity = new JvsDataFactoryLog().setDataId(factoryTaskEvent.getTaskItselfId());
                    entity.setDelFlag(false);
                    entity.setOperateMethod(factoryTaskEvent.getOperateMethod());
                    entity.setEndTime(LocalDateTime.now());
                    entity.setFailureReason("前置任务执行失败");
                    entity.setOperatorId(userDto.getId());
                    entity.setOperatorName(userDto.getRealName());
                    entity.setExecutionResults(false);
                    factoryLogService.save(entity);
                    this.pushMessage(factoryTaskEvent.getUserDto().getRealName(), QueueTaskStatusEnum.FAIL, dataFactory.getId(), factoryTaskEvent.getOperateMethod());
                } else {
                    jvsDataFactoryService.sendQueue(jvsDataFactoryService.getById(factoryTaskEvent.getTaskItselfId()), QueueTaskTypeEnum.ITSELF, factoryTaskEvent.getUserDto(), factoryTaskEvent.getBatchId(), factoryTaskEvent.getOperateMethod());
                }
            }
        }
        //本身执行完成后需要判断是否需要执行后置
        if (factoryTaskEvent.getQueueTaskType().equals(QueueTaskTypeEnum.ITSELF)) {
            //判断任务本身是否执行成功
            long count = jvsDataFactoryQueueService.count(new LambdaQueryWrapper<JvsDataFactoryQueue>()
                    .eq(JvsDataFactoryQueue::getQueueTaskType, QueueTaskTypeEnum.ITSELF)
                    .eq(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.ACCOMPLISH)
                    .eq(JvsDataFactoryQueue::getDataFactoryId, factoryTaskEvent.getDataFactoryId())
                    .eq(JvsDataFactoryQueue::getBatchId, factoryTaskEvent.getBatchId()));
            if (count > 0) {
                jvsDataFactoryService.sendQueue(jvsDataFactoryService.getById(factoryTaskEvent.getDataFactoryId()), QueueTaskTypeEnum.REAR_TASK, factoryTaskEvent.getUserDto(), factoryTaskEvent.getBatchId(), factoryTaskEvent.getOperateMethod());
            }
        }
        messageImPush.notify(ImPushTypeEnums.update, "", "");
        //设置可以接受消息
        log.info("------------------执行完成，修改当前服务的可执行状态成功,执行数据id为：{}", factoryTaskEvent.getDataFactoryId());
    }

    /**
     * 主任务执行逻辑
     *
     * @param dataFactory         数据集本身
     * @param factoryTaskEvent    队列消息
     * @param jvsDataFactoryQueue 队列数据
     */
    private void exec(FactoryTaskEvent factoryTaskEvent, JvsDataFactory dataFactory, JvsDataFactoryQueue jvsDataFactoryQueue) {
        UserDto userDto = factoryTaskEvent.getUserDto();
        JvsDataFactoryLog entity = new JvsDataFactoryLog().setDataId(dataFactory.getId());
        entity.setDelFlag(false);
        entity.setOperateMethod(factoryTaskEvent.getOperateMethod());
        entity.setStartTime(LocalDateTime.now());
        //设置当前执行的数据集对象
        SystemTool<JvsDataFactory> tool = new SystemTool<>();
        tool.set(Constant.SYSTEM_NOW_JVS_DATA_FACTORY, dataFactory);
        long st = System.currentTimeMillis();
        //开始遍历
        FHtmlGraph html = JSONObject.parseObject(dataFactory.getViewJson(), FHtmlGraph.class);
        html.setFormal(true);
        html.setQueueLogId(jvsDataFactoryQueue.getId());
        //开始执行
        html.run();
        entity.setDuration(System.currentTimeMillis() - st);
        entity.setEndTime(LocalDateTime.now());
        entity.setFailureReason(html.getError());
        entity.setOperatorId(userDto.getId());
        entity.setOperatorName(userDto.getRealName());
        entity.setOutNumber(html.getNodeDataNumber().toString());
        entity.setExecutionResults(ObjectNull.isNull(html.getError()));
        entity.setNodeLog(html.getNodeLog());
        BigInteger bigInteger = html.getNodeLog().stream().map(JvsDataFactoryLog.NodeLog::getCount)
                .map(BigInteger::valueOf)
                .reduce(BigInteger.ZERO, BigInteger::add);
        entity.setExecNumber(bigInteger.longValue());
        factoryLogService.save(entity);
        jvsDataFactoryService.update(new UpdateWrapper<JvsDataFactory>().lambda().set(JvsDataFactory::getUpdateRunTime, LocalDateTime.now()).eq(JvsDataFactory::getId, dataFactory.getId()));
        //通知数据源同步结构
        jvsDataFactoryService.syncTableStructure(dataFactory);
        //队列日志修改
        QueueTaskStatusEnum statusEnum = QueueTaskStatusEnum.ACCOMPLISH;
        if (StrUtil.isNotBlank(html.getError())) {
            statusEnum = QueueTaskStatusEnum.FAIL;
        }
        jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                .eq(JvsDataFactoryQueue::getId, factoryTaskEvent.getDataFactoryQueueId())
                .set(JvsDataFactoryQueue::getTaskStatus, statusEnum)
                .set(JvsDataFactoryQueue::getEndTime, LocalDateTime.now())
                .set(StrUtil.isNotBlank(html.getError()), JvsDataFactoryQueue::getErrorMsg, html.getError()));
        this.pushMessage(userDto.getRealName(), statusEnum, dataFactory.getId(), factoryTaskEvent.getOperateMethod());
        log.info("-----队列消息异步执行完成");

    }

    /**
     * 消息推送
     *
     * @param operateMethod 执行类型
     * @param id            数据集id
     * @param operateType   执行状态
     * @param operateUser   发起执行人名称
     */
    public void pushMessage(String operateUser, QueueTaskStatusEnum operateType, String id, OperateMethodEnum operateMethod) {
        TriggerTypeEnum triggerTypeEnum = operateType.equals(QueueTaskStatusEnum.ACCOMPLISH) ? TriggerTypeEnum.execSuccess : TriggerTypeEnum.execFailures;
        NoticeCacheDto noticeCacheDto = new NoticeCacheDto()
                .setJvsDataFactoryId(id)
                .setOperateUser(operateUser)
                .setOperateMethod(operateMethod)
                .setTenantId(TenantContextHolder.getTenantId())
                .setTriggerTypeList(Arrays.asList(triggerTypeEnum));
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_DATA_FACTORY_TASK_NOTICE, noticeCacheDto);
    }
}
