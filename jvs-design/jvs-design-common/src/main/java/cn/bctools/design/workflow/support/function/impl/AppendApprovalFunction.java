package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.dto.AppendApprovalOperationDto;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.*;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.TaskPersonService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.TransferRuntimeDto;
import cn.bctools.design.workflow.utils.FlowApprovalCacheUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 保存加签设置（为当前节点设置加签）
 */
@Slf4j
@Component
@AllArgsConstructor
public class AppendApprovalFunction extends AbstractFunctionHandler<Boolean, RuntimeData> {

    private final RedisUtils redisUtils;
    private final FlowTaskNodeService flowTaskNodeService;
    private final TaskPersonService taskPersonService;
    private final TransferFunction transferFunction;
    private static final Integer LOCK_EXPIRE_TIME = 10000;

    /**
     * 加签锁key
     */
    private static final String APPEND_LOCK = "appendlock";

    @Override
    public Boolean invoke(Node node, RuntimeData runtimeData) {
        FlowReqDto flowReqDto = runtimeData.getFlowDto();
        // 参数校验
        checkParam(flowReqDto.getAppendApproval());

        // 防止并发加签
        String lockKey = getLockKey(flowReqDto.getId(), flowReqDto.getNodeId());
        try {
            boolean lock = redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME);
            if (Boolean.FALSE.equals(lock)) {
               throw new BusinessException("已加签请勿重复操作");
            }
            // 加签处理
            handle(flowReqDto.getId(), flowReqDto.getNodeId(), flowReqDto.getAppendApproval(), runtimeData);

            // 根据加签配置修改审批人
            // 后加签：不处理
            // 前加签：根据当前加签的数据，设置审批人
            if (AppendApprovalPointEnum.BEFORE.equals(flowReqDto.getAppendApproval().getPoint())) {
                List<UserDto> userDtos = flowReqDto.getAppendApproval().getPersonnels()
                        .stream()
                        .map(personnel -> new UserDto().setId(personnel.getId()).setRealName(personnel.getName()))
                        .collect(Collectors.toList());
                taskPersonService.saveTaskPerson(node, runtimeData, userDtos);
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }
        return true;
    }

    /**
     * 加签参数校验
     *
     * @param appendApproval 加签配置
     */
    private void checkParam(AppendApprovalOperationDto appendApproval) {
        if (ObjectNull.isNull(appendApproval)) {
            throw new BusinessException("请设置加签参数");
        }
        if (ObjectNull.isNull(appendApproval.getPoint())) {
            throw new BusinessException("请选择加签方式");
        }
        if (CollectionUtils.isEmpty(appendApproval.getPersonnels())) {
            throw new BusinessException("请选择加签人员");
        }
    }


    /**
     * 保存加签
     *
     * @param flowTaskId     任务id
     * @param nodeId         节点id
     * @param appendApproval 加签
     * @param runtimeData    运行时数据
     */
    private void handle(String flowTaskId, String nodeId, AppendApprovalOperationDto appendApproval, RuntimeData runtimeData) {
        UserDto userDto = runtimeData.getUser();
        FlowTaskNode flowTaskNode = flowTaskNodeService.getOne(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, flowTaskId).eq(FlowTaskNode::getNodeId, nodeId));
        // 当前节点已存在的加签数据
        AppendApprovalDto oldAppendApproval = flowTaskNode.getAppendApproval();
        // 根据当前审批类型，确定加签目标id
        String appendTargetId = "";
        if (FlowTaskNodeApprovalTypeEnum.APPROVAL.equals(flowTaskNode.getApprovalType())) {
            // 正常审批，设置当前审批节点为加签目标id
            appendTargetId = nodeId;
        } else {
            // 加签审批，设置当前加签节点为加签目标id
            appendTargetId = oldAppendApproval.getCurrentId();
        }
        // 校验是否可以加签
        checkCanAppendApproval(appendTargetId, oldAppendApproval);
        // 构造加签信息
        AppendApprovalDto appendApprovalInfo = buildAppendApproval(runtimeData, appendTargetId, oldAppendApproval, appendApproval);
        flowTaskNode.setAppendApproval(appendApprovalInfo);

        // 前加签
        if (AppendApprovalPointEnum.BEFORE.equals(appendApprovalInfo.getAppendApprovalPoint())) {
            // 设置当前节点审批类型为“加签审批”
            flowTaskNode.setApprovalType(FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL);
            // 将所有审批记录置为“审批结束”
            flowTaskNode.getCourse().getApproveResultDtos().forEach(dto -> dto.setOver(Boolean.TRUE));
        }
        flowTaskNodeService.updateById(flowTaskNode);
        // 保存加签操作记录
        saveAppendApproval(userDto, runtimeData, appendApproval);
        // 清空当前节点审批结果缓存
        redisUtils.del(FlowApprovalCacheUtil.getApproverKey(flowTaskId, nodeId));
    }

    /**
     * 加签锁key
     *
     * @param taskId 工作流任务id
     * @param nodeId 工作流节点id
     * @return 加签锁key
     */
    private String getLockKey(String taskId, String nodeId) {
        return SystemConstant.SYSTEM_NAME + ":" + APPEND_LOCK + ":" + taskId + ":" + nodeId;
    }

    /**
     * 校验是否可以加签
     * 一个加签目标id，只能存在一个未结束的加签
     *
     * @param appendTargetId    加签目标id
     * @param appendApprovalDto 当前节点已存在的加签数据
     */
    private void checkCanAppendApproval(String appendTargetId, AppendApprovalDto appendApprovalDto) {
        // 当前节点不存在加签数据，则可加签
        if (ObjectNull.isNull(appendApprovalDto)) {
            return;
        }
        // 当前节点已存在加签数据、但加签审批未结束、且加签目标id没有加签数据，则可加签，否则不能加签
        boolean exists = appendApprovalDto.getDetails().stream()
                .anyMatch(e -> appendTargetId.equals(e.getAppendTargetId()) && Boolean.FALSE.equals(e.getOver()));
        if (Boolean.TRUE.equals(exists)) {
            throw  new BusinessException("已加签请勿重复操作");
        }
    }

    /**
     * 构造加签信息
     *
     * @param runtimeData       运行时数据
     * @param appendTargetId    加签目标id
     * @param oldAppendApproval 当前节点已存在的加签数据
     * @param appendApproval    新的加签数据
     * @return 加签信息
     */
    private AppendApprovalDto buildAppendApproval(RuntimeData runtimeData, String appendTargetId, AppendApprovalDto oldAppendApproval, AppendApprovalOperationDto appendApproval) {
        AppendApprovalDto newAppendDto = null;
        // 生成加签id
        String id = IdGenerator.getIdStr();
        // 当前节点没有加签数据
        if (ObjectNull.isNull(oldAppendApproval)) {
            newAppendDto = new AppendApprovalDto();
            newAppendDto.setAppendApprovalPoint(appendApproval.getPoint());
            newAppendDto.setCurrentId(id);
            AppendApprovalDetailDto detailDto = buildDetailDto(runtimeData, appendTargetId, id, null, appendApproval);
            newAppendDto.setDetails(Collections.singletonList(detailDto));
        } else {
            // 将新的加签设置加入加签集合
            String currentId = oldAppendApproval.getCurrentId();
            newAppendDto = oldAppendApproval;
            newAppendDto.setCurrentId(id);
            List<AppendApprovalDetailDto> detailDtos = newAppendDto.getDetails();
            detailDtos.add(buildDetailDto(runtimeData, appendTargetId, id, currentId, appendApproval));
            newAppendDto.setDetails(detailDtos);
        }
        return newAppendDto;
    }

    /**
     * 构造加签详情
     *
     * @param appendTargetId 加签目标id
     * @param id             加签id
     * @param afterId        后续加签id
     * @param appendApproval 加签入参
     * @return 加签详情
     */
    private AppendApprovalDetailDto buildDetailDto(RuntimeData runtimeData, String appendTargetId, String id, String afterId, AppendApprovalOperationDto appendApproval) {
        // 加签的用户如果被代理了，就不允许加签
        List<String> userIds = appendApproval.getPersonnels().stream().map(PersonnelDto::getId).collect(Collectors.toList());
        TransferRuntimeDto transfer = new TransferRuntimeDto(runtimeData.getUser(), runtimeData.getFlowTask(), null, userIds);
        List<ProxyDto> transfers = transferFunction.invoke(runtimeData.getCurrentNode(), transfer);
        if (CollectionUtils.isNotEmpty(transfers)) {
            throw new BusinessException("您选择的用户已将任务转交他人请重新选择", transfers.stream().map(TransferDto::getUserName).collect(Collectors.joining(",")));
        }
        AppendApprovalDetailDto detailDto = new AppendApprovalDetailDto();
        detailDto.setAppendTargetId(appendTargetId);
        detailDto.setId(id);
        detailDto.setPersonnels(appendApproval.getPersonnels());
        detailDto.setDescription(appendApproval.getDescription());
        detailDto.setAfterId(afterId);
        return detailDto;
    }


    /**
     * 暂存加签操作
     *
     * @param userDto     用户
     * @param runtimeData 任务节点
     */
    void saveAppendApproval(UserDto userDto, RuntimeData runtimeData, AppendApprovalOperationDto appendApproval) {
        ApproveResultDto approveResultDto = new ApproveResultDto()
                .setUserId(userDto.getId())
                .setUserName(userDto.getRealName())
                .setNodeOperationTypeEnum(NodeOperationTypeEnum.APPEND)
                .setAppendApproval(appendApproval)
                .setTime(LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN))
                .setOver(Boolean.TRUE);
        runtimeData.getApproveResults().add(approveResultDto);
    }

}
