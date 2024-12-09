package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.support.RuntimeData;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流任务审批人服务
 */
public interface TaskPersonService {

    /**
     * 保存节点待审批人
     *
     * @param nextNode    下一节点
     * @param runtimeData 运行时数据
     * @param users       指定用户为下一节点待审批人（若不为空，则以此字段数据为下一节点审批人）
     */
    void saveTaskPerson(Node nextNode, RuntimeData runtimeData, List<UserDto> users);
}
