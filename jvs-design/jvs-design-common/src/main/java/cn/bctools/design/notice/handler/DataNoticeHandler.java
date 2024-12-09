package cn.bctools.design.notice.handler;

import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.workflow.entity.FlowTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The interface Data notice handler.
 *
 * @author zhuxiaokang  消息通知
 */
public interface DataNoticeHandler {

    /**
     * 发送消息通知
     *
     * @param tenantId    the tenant id
     * @param appId       应用ID
     * @param triggerType 触发类型
     * @param modelId     模型id
     * @param dataId      数据id
     * @param data        数据
     */
    void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, Map<String, Object> data);

    /**
     * 发送消息通知
     *
     * @param tenantId    the tenant id
     * @param appId       应用ID
     * @param triggerType 触发类型
     * @param modelId     模型id
     * @param dataId      数据id
     * @param data        数据
     * @param changeKey   变更的字段
     */
    void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, Map<String, Object> data, Collection<String> changeKey);

    /**
     * 发送工作流消息通知
     *
     * @param tenantId    the tenant id
     * @param appId       应用id
     * @param triggerType 触发类型
     * @param modelId     模型id
     * @param dataId      数据id
     * @param flowTask    工作流任务消息
     * @param taskNodeIds 待办任务节点id
     */
    void sendNotify(String tenantId, String appId, TriggerTypeEnum triggerType, String modelId, String dataId, FlowTask flowTask, List<String> taskNodeIds);
}
