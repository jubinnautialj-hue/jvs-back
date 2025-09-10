package cn.bctools.design.taskNotice.service;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.model.Node;

import java.util.List;
/**
 * 待办提醒通知
 */
public interface FlowTaskNoticeService {
    /**
     * 创建单据
     * @param nextNode 节点内容
     * @param flowTask 工作流信息
     * @param flowTaskPersons 待办人员集合
     * @return
     * @throws Exception
     */
    boolean create(FlowTask flowTask, Node nextNode, List<FlowTaskPerson> flowTaskPersons);
    boolean close(FlowTask flowTask, List<String> bizTaskAndTaskIds);
    boolean recall(FlowTask flowTask, List<String> bizTaskAndTaskIds);
    boolean update(FlowTask flowTask, Node nextNode, List<FlowTaskPerson> flowTaskPersons);
}
