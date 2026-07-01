package cn.bctools.design.notice.handler;

import cn.bctools.design.notice.handler.bo.ReceiverBo;
import cn.bctools.design.workflow.entity.FlowTask;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 消息通知-获取接收人
 */
public interface NoticeReceiverHandler {

    /**
     * 获取接收用户id
     *
     * @param receiver 接收人配置
     * @param data 数据
     * @param flowTask 工作流任务
     * @param taskNodeIds 待办节点id
     * @return 接收人id集合
     */
    List<String> getUserIds(List<ReceiverBo> receiver, Map<String, Object> data, FlowTask flowTask, List<String> taskNodeIds);
}
