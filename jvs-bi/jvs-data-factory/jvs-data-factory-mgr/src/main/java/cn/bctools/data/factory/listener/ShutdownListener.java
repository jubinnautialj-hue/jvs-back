package cn.bctools.data.factory.listener;

import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@Order(1)
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    JvsDataFactoryQueueService jvsDataFactoryQueueService;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
//        //关闭系统时 判断是否存在任务 在队列获取在执行中 如果存在直接改为失败
//        if (discoveryClient.getInstances(SpringContextUtil.getApplicationContextName()).size() == 1) {
//            TenantContextHolder.clear();
//            jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
//                    .set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.FAIL)
//                    .set(JvsDataFactoryQueue::getErrorMsg, "系统停止运行")
//                    .eq(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.EXECUTE));
//        }
    }
}
