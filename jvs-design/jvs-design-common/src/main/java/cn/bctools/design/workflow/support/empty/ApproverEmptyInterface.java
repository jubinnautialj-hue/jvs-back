package cn.bctools.design.workflow.support.empty;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.support.RuntimeData;

import java.util.List;

/**
 * @author zhuxiaokang
 * 审批人为空处理接口
 */
public interface ApproverEmptyInterface {

    /**
     * 节点类型
     *
     * @return 节点类型
     */
    String getType();

    /**
     * 流转处理
     *
     * @param node        节点
     * @param runtimeData 运行时信息
     * @return 审批人信息
     */
    List<UserDto> execute(Node node, RuntimeData runtimeData);
}
