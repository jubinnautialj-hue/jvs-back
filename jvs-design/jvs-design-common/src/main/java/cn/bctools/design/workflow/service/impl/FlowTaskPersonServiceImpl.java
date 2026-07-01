package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.mapper.FlowTaskPersonMapper;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流任务待办人 服务实现类
 */
@Service
public class FlowTaskPersonServiceImpl extends ServiceImpl<FlowTaskPersonMapper, FlowTaskPerson> implements FlowTaskPersonService {

    @Override
    public void removeTaskAll(String flowTaskId) {
        remove(Wrappers.<FlowTaskPerson>lambdaQuery().eq(FlowTaskPerson::getFlowTaskId, flowTaskId));
    }

    @Override
    public void removeTaskNodeAll(String flowTaskId, String nodeId) {
        remove(Wrappers.<FlowTaskPerson>lambdaQuery().eq(FlowTaskPerson::getFlowTaskId, flowTaskId).eq(FlowTaskPerson::getNodeId, nodeId));
    }

    @Override
    public Integer pendingApprovesCount(List<String> appIds, UserDto userDto) {
        return ((Long) count(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getUserId, userDto.getId())
                .in(FlowTaskPerson::getJvsAppId, appIds)
                .eq(FlowTaskPerson::getTest, Boolean.FALSE)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING))).intValue();
    }

    @Override
    public List<String> getPendingApproveUserIds(String flowTaskId, List<String> nodeIds) {
        List<FlowTaskPerson> flowTaskPersonList = list((Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getTest, Boolean.FALSE)
                .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                .in(FlowTaskPerson::getNodeId, nodeIds)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING))
                .select(FlowTaskPerson::getUserId));
        return flowTaskPersonList.stream().map(FlowTaskPerson::getUserId).collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getPendingApproveUsers(String flowTaskId, String nodeId) {
        List<FlowTaskPerson> flowTaskPersonList = list((Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getTest, Boolean.FALSE)
                .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                .eq(FlowTaskPerson::getNodeId, nodeId)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING))
                .select(FlowTaskPerson::getUserId, FlowTaskPerson::getUserName));
        if (CollectionUtils.isEmpty(flowTaskPersonList)) {
            return Collections.emptyList();
        }
        return flowTaskPersonList.stream().map(p -> new UserDto().setId(p.getUserId()).setRealName(p.getUserName())).collect(Collectors.toList());
    }

    @Override
    public Boolean checkPendingTask(String flowTaskId, String nodeId, String userId) {
        // 查询用户是否有待审批的 任务
        long peddingCount = count(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                .eq(FlowTaskPerson::getNodeId, nodeId)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING)
                .eq(FlowTaskPerson::getUserId, userId));
        return peddingCount > 0;
    }

    @Override
    public List<FlowTaskPerson> listPendingByTaskIds(List<String> taskIds) {
        return list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .in(FlowTaskPerson::getFlowTaskId, taskIds)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING));
    }

    @Override
    public List<FlowTaskPerson> listPerson(String flowTaskId, List<String> nodeIds) {
        return list((Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getTest, Boolean.FALSE)
                .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                .in(FlowTaskPerson::getNodeId, nodeIds)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)
                .select(FlowTaskPerson::getId, FlowTaskPerson::getFlowTaskId, FlowTaskPerson::getNodeId, FlowTaskPerson::getUserId, FlowTaskPerson::getUserName, FlowTaskPerson::getNumber)));
    }

    @Override
    public List<FlowTaskPerson> listPerson(List<String> taskIds) {
        return list((Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getTest, Boolean.FALSE)
                .in(FlowTaskPerson::getFlowTaskId, taskIds)
                .eq(FlowTaskPerson::getHang, Boolean.FALSE)));
    }
}
