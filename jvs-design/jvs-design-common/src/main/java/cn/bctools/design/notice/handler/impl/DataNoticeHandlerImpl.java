package cn.bctools.design.notice.handler.impl;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.bo.SendNoticeReqBo;
import cn.bctools.design.notice.handler.bo.TriggerSettingBo;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.handler.send.mq.SendNotice;
import cn.bctools.design.notice.handler.send.mq.SendNoticeConfig;
import cn.bctools.design.notice.handler.util.NoticeTriggerUtils;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class DataNoticeHandlerImpl implements DataNoticeHandler {


    private final DataNoticeService dataNoticeService;
    private final DynamicDataService dynamicDataService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final RabbitTemplate rabbitTemplate;
    private final FlowDynamicDataService flowDynamicDataService;


    @Async
    @Override
    public void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, Map<String, Object> data) {
        SendNoticeReqBo req = new SendNoticeReqBo()
                .setTenantId(tenantId)
                .setAppId(appId)
                .setModelId(modelId)
                .setDataId(dataId)
                .setData(data)
                .setTriggerType(triggerType);
        sendNotify(req);
    }


    @Async
    @Override
    public void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, List<Object> objects) {
        objects.forEach(e -> {
            Map<String, Object> data = (Map<String, Object>) e;
            SendNoticeReqBo req = new SendNoticeReqBo()
                    .setTenantId(tenantId)
                    .setAppId(appId)
                    .setModelId(modelId)
                    .setDataId(String.valueOf(data.get("dataId")))
                    .setData(data)
                    .setTriggerType(triggerType);
            sendNotify(req);
        });

    }

    @Async
    @Override
    public void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, Map<String, Object> data, Collection<String> changeKey) {
        SendNoticeReqBo req = new SendNoticeReqBo()
                .setTenantId(tenantId)
                .setAppId(appId)
                .setModelId(modelId)
                .setDataId(dataId)
                .setData(data)
                .setTriggerType(triggerType)
                .setChangeKey(changeKey);
        sendNotify(req);
    }

    @Async
    @Override
    public void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, FlowTask flowTask, List<String> taskNodeIds) {
        SendNoticeReqBo req = new SendNoticeReqBo()
                .setTenantId(tenantId)
                .setAppId(appId)
                .setModelId(modelId)
                .setDataId(dataId)
                .setFlowTask(flowTask)
                .setTaskNodeIds(taskNodeIds)
                .setTriggerType(triggerType);
        sendNotify(req);
    }

    /**
     * 发送消息
     *
     * @param req 发送通知请求参数
     */
    public void sendNotify(SendNoticeReqBo req) {
        String tenantId = req.getTenantId();
        String appId = req.getAppId();
        String modelId = req.getModelId();
        String dataId = req.getDataId();
        TenantContextHolder.setTenantId(tenantId);
        // 获取模型消息通知配置
        List<QueryDataNoticeRespDto> dataNoticePos = dataNoticeService.getEffective(appId, modelId, req.getFlowTask());
        if (CollectionUtils.isEmpty(dataNoticePos)) {
            return;
        }
        // 得到待发送消息通知的配置集
        List<QueryDataNoticeRespDto> noticePos = checkSendNotify(dataNoticePos, req);
        if (CollectionUtils.isEmpty(noticePos)) {
            return;
        }
        // 数据
        Map<String, Object> storeData = req.getData();
        if (Boolean.FALSE.equals(TriggerTypeEnum.DELETED.equals(req.getTriggerType()))) {
            // 查询已持久化的数据
            storeData = dynamicDataService.querySingle(appId, modelId, dataId);
            storeData = dynamicDataService.paresMapWithEcho(appId, storeData, modelId, null, false);
            // 将流程相关数据组添加到数据
            JSONObject flowTaskData = Optional.ofNullable(flowDynamicDataService.getFlowTaskDataObj(dataId)).orElseGet(JSONObject::new);
            storeData.putAll(flowTaskData);
        }
        // 解析配置发送消息
        rabbitTemplate.convertAndSend(SendNoticeConfig.PARSE_NOTICE_SEND_EXCHANGE, SendNoticeConfig.PARSE_NOTICE_SEND_ROUTING,
                new SendNotice()
                        .setTenantId(tenantId)
                        .setData(storeData)
                        .setNoticePos(noticePos)
                        .setFlowTask(req.getFlowTask())
                        .setTaskNodeIds(req.getTaskNodeIds()));
    }

    /**
     * 得到待发送消息通知的配置集
     *
     * @param dataNoticePos 消息配置
     * @param req           发送通知请求
     * @return 待发送消息通知的配置集
     */
    private List<QueryDataNoticeRespDto> checkSendNotify(List<QueryDataNoticeRespDto> dataNoticePos, SendNoticeReqBo req) {
        TriggerTypeEnum triggerType = req.getTriggerType();
        // 获取指定触发类型的消息通知配置
        List<QueryDataNoticeRespDto> noticePos = dataNoticePos.stream().filter(n -> triggerType.equals(n.getTriggerSetting().getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(noticePos)) {
            return Collections.emptyList();
        }
        // 返回满足发送消息通知的配置
        return noticePos.stream().filter(n -> {
            TriggerSettingBo triggerSetting = n.getTriggerSetting();
            switch (triggerType) {
                case CREATED:
                case FLOW_CREATED:
                case DELETED:
                case FLOW_URGE:
                case FLOW_REMIND:
                    // 数据创建成功、数据删除成功、流程启动成功，工作流待办提醒，催办 直接发送消息通知
                    return Boolean.TRUE;
                case EDITED:
                    return NoticeTriggerUtils.checkTriggerEdited(triggerSetting, req.getChangeKey());
                case FLOW_APPROVAL_RESULTS:
                    return NoticeTriggerUtils.checkTriggerFlowApprovalResults(triggerSetting, req.getFlowTask());
                case FLOW_APPROVAL_NODE:
                    List<FlowTaskNode> flowTaskNodes = flowTaskNodeService.getCurrentNodes(req.getFlowTask().getId(), req.getTaskNodeIds());
                    return NoticeTriggerUtils.checkTriggerFlowApprovalNode(triggerSetting, flowTaskNodes);
                default:
                    log.warn("消息通知发送失败,不支持的触发条件");
                    return Boolean.FALSE;
            }
        }).collect(Collectors.toList());
    }
}
