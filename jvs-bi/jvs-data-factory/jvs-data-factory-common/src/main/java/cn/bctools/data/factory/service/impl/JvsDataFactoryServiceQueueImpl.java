package cn.bctools.data.factory.service.impl;

import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum;
import cn.bctools.data.factory.mapper.JvsDataFactoryQueueMapper;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 数据etl 队列日志记录 服务实现类
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Service
@AllArgsConstructor
public class JvsDataFactoryServiceQueueImpl extends ServiceImpl<JvsDataFactoryQueueMapper, JvsDataFactoryQueue> implements JvsDataFactoryQueueService {
    @Override
    public String isTaskExec(JvsDataFactory dataFactory) {
        //判断队列中是否存在此任务 如果存在就无法保存
        long count = this.count(new LambdaQueryWrapper<JvsDataFactoryQueue>()
                .eq(JvsDataFactoryQueue::getDataFactoryId, dataFactory.getId())
                .notIn(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.ACCOMPLISH, QueueTaskStatusEnum.FAIL));
        if (count > 0) {
            return "此任务正在执行,不支持此操作";
        }
        if (!dataFactory.getPrefixTaskId().isEmpty()) {
            //判断是否存在前置或者后置任务在队列中
            count = this.count(new LambdaQueryWrapper<JvsDataFactoryQueue>()
                    .in(JvsDataFactoryQueue::getDataFactoryId, dataFactory.getPrefixTaskId())
                    .notIn(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.ACCOMPLISH, QueueTaskStatusEnum.FAIL));
            if (count > 0) {
                return "此任务的前置任务存在执行中的任务，不支持此操作";
            }
        }
        if (!dataFactory.getRearTaskId().isEmpty()) {
            //判断是否存在前置或者后置任务在队列中
            count = this.count(new LambdaQueryWrapper<JvsDataFactoryQueue>()
                    .in(JvsDataFactoryQueue::getDataFactoryId, dataFactory.getRearTaskId())
                    .notIn(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.ACCOMPLISH, QueueTaskStatusEnum.FAIL));
            if (count > 0) {
                return "此任务的后置任务存在执行中的任务，不支持此操作";
            }
        }
        return null;
    }
}
