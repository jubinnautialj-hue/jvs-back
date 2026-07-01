package cn.bctools.design.notice.handler.send.mq;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.handler.NoticeReceiverHandler;
import cn.bctools.design.notice.handler.bo.NoticeDataExtendBo;
import cn.bctools.design.notice.handler.bo.ReceiverBo;
import cn.bctools.design.notice.handler.enums.NoticeDataExtendOprateTypeEnum;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.handler.send.CompositeSendNotifyHandler;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.rabbit.config.MyRabbitConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 解析配置发送消息消费者
 */

@Slf4j
@Component
@AllArgsConstructor
public class SendNoticeConsumer {

    /**
     * 单次发送消息数量上限
     */
    private static final Integer SEND_USER_MAX = 1000;
    private final NoticeReceiverHandler noticeReceiverHandler;
    private final CompositeSendNotifyHandler compositeSendNotifyHandler;

    @SneakyThrows
    @RabbitListener(queues = SendNoticeConfig.PARSE_NOTICE_SEND_QUEUE, containerFactory = MyRabbitConfig.BATCH_LISTENER_CONTAINER_FACTORY, concurrency = "1-20")
    public void flowExecuteNotice(Channel channel, List<Message> messages) {
        //不在线直接返回
        if (!SpringContextUtil.thisServerStats()) {
            channel.basicNack(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true, true);
            return;
        }
        try {
            List<SendNotice> flowExecuteNotices = messages.stream().map(msg -> JSON.parseObject(msg.getBody(), SendNotice.class)).collect(Collectors.toList());
            for (SendNotice msg : flowExecuteNotices) {
                String tenantId = msg.getTenantId();
                FlowTask flowTask = msg.getFlowTask();
                Map<String, Object> storeData = msg.getData();
                List<String> taskNodeIds = msg.getTaskNodeIds();
                List<QueryDataNoticeRespDto> noticePos = msg.getNoticePos();
                for (QueryDataNoticeRespDto n : noticePos) {
                    TenantContextHolder.setTenantId(tenantId);
                    parse(tenantId, n, storeData, flowTask, taskNodeIds);
                }
            }
        } catch (Exception e) {
            log.error("发送消息失败：{}", e.getMessage());
        } finally {
            channel.basicAck(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 解析消息通知配置
     *
     * @param tenantId    租户id
     * @param dataNotice  消息通知配置
     * @param data        数据
     * @param flowTask    工作流任务
     * @param taskNodeIds 任务节点id
     */
    private void parse(String tenantId, QueryDataNoticeRespDto dataNotice, Map<String, Object> data, FlowTask flowTask, List<String> taskNodeIds) {
        List<ReceiverBo> receiver = dataNotice.getReceiver();
        if (CollectionUtils.isEmpty(receiver)) {
            return;
        }
        // 若有任务节点id，则按节点id分开发送消息（因为要点击消息进入待办页面）
        if (ObjectNull.isNotNull(taskNodeIds)) {
            taskNodeIds.forEach(taskNodeId -> sendMsg(tenantId, dataNotice, receiver, data, flowTask, taskNodeId));
        } else {
            sendMsg(tenantId, dataNotice, receiver, data, flowTask, null);
        }
    }

    private void sendMsg(String tenantId, QueryDataNoticeRespDto dataNotice, List<ReceiverBo> receiver, Map<String, Object> data, FlowTask flowTask, String taskNodeId) {
        // 接收人
        List<String> taskNodeIds = Optional.ofNullable(taskNodeId).map(nodeId -> Collections.singletonList(taskNodeId)).orElse(null);
        List<String> userIds = noticeReceiverHandler.getUserIds(receiver, data, flowTask, taskNodeIds).stream().limit(SEND_USER_MAX).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        NoticeDataExtendBo noticeDataExtend = buildNoticeDataExtend(dataNotice, flowTask, taskNodeId);
        compositeSendNotifyHandler.send(tenantId, dataNotice, data, userIds, noticeDataExtend);
    }

    /**
     * 构造消息通知扩展数据
     *
     * @param dataNotice 消息通知配置
     * @param flowTask   工作流任务
     * @param taskNodeId 任务节点id
     * @return 消息通知扩展数据
     */
    private NoticeDataExtendBo buildNoticeDataExtend(QueryDataNoticeRespDto dataNotice, FlowTask flowTask, String taskNodeId) {
        TriggerTypeEnum triggerType = dataNotice.getTriggerSetting().getType();
        // 若是待办提醒、催办提醒，构造扩展数据，用以从消息通知页面快捷进入待办
        boolean b = ObjectNull.isNotNull(taskNodeId) && (TriggerTypeEnum.FLOW_REMIND.equals(triggerType));
        if (b || TriggerTypeEnum.FLOW_URGE.equals(triggerType)) {
            JSONObject param = new JSONObject();
            param.put("taskId", flowTask.getId());
            param.put("nodeId", taskNodeId);
            param.put("jvsAppId", flowTask.getJvsAppId());
            param.put("flowDesignId", flowTask.getFlowDesignId());
            param.put("dataModelId", flowTask.getDataModelId());
            param.put("dataId", flowTask.getDataId());
            return new NoticeDataExtendBo().setOprateType(NoticeDataExtendOprateTypeEnum.OPEN_FLOW_TASK_TODO).setOprateParam(param);
        }
        return null;
    }
}
