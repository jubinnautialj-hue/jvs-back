package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeProperties;
import cn.bctools.design.workflow.model.enums.CountersignRuleEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.model.properties.AppendApprovalProperties;
import cn.bctools.design.workflow.model.properties.ApprovalModeProperties;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.ModeDto;
import cn.bctools.design.workflow.utils.FlowApprovalCacheUtil;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 审批方式。单人审批 | 多人审批
 */
@Slf4j
@Component
@AllArgsConstructor
public class ModeFunction extends AbstractFunctionHandler<ModeDto, RuntimeData> {

    private final RedisUtils redisUtils;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;

    /**
     * 锁时长（秒）
     */
    private static final Integer LOCK_EXPIRE_TIME = 10000;
    /**
     * 等待获取锁间隔时长（毫秒）
     */
    private static final Integer WAIT_LOCK_TIME = 10;

    /**
     * 审批操作缓存（7天）
     */
    private static final Long APPROVER_EXPIRE = 604800L;

    /**
     * 审批锁key
     */
    private static final String APPROVER_LOCK = "approverlock";

    /**
     * 参与执行逻辑审批方式
     */
    private static final List<NodeOperationTypeEnum> VERIFY_TYPES = Arrays.asList(
            NodeOperationTypeEnum.PASS,
            NodeOperationTypeEnum.REFUSE,
            NodeOperationTypeEnum.BACK);

    /**
     * 其它操作类型
     */
    private static final List<NodeOperationTypeEnum> OTHER_VERIFY_TYPES = Collections.singletonList(NodeOperationTypeEnum.REMOVE_SIGNER);

    /**
     * 单人审批 | 多人审批，是否通过审批
     *
     * @param node        节点
     * @param runtimeData 运行时数据
     * @return TRUE-通过审批，FALSE-未通过审批
     */
    @Override
    public ModeDto invoke(Node node, RuntimeData runtimeData) {
        NodeOperationTypeEnum currentOperationType = runtimeData.getFlowDto().getNodeOperationType();
        if (Boolean.FALSE.equals(VERIFY_TYPES.contains(currentOperationType)) && Boolean.FALSE.equals(OTHER_VERIFY_TYPES.contains(currentOperationType))) {
            return new ModeDto().setEnd(Boolean.FALSE);
        }

        List<ApproverCache> approvers = null;
        List<String> userIds = null;
        // 锁key
        String lockKey = getApproverLockKey(runtimeData.getFlowTask().getId(), runtimeData.getNodeId());
        // 获取已审批用户缓存
        String key = FlowApprovalCacheUtil.getApproverKey(runtimeData.getFlowTask().getId(), runtimeData.getNodeId());
        NodePropertiesModeEnum mode = getNodeMode(node, runtimeData);
        try {
            boolean lock = redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME);
            while (Boolean.FALSE.equals(lock)) {
                Thread.sleep(WAIT_LOCK_TIME);
                lock = redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME);
            }

            // 得到当前节点审批人id集合
            userIds = getUserIds(node, runtimeData);
            // 判断是否可审批
            checkApprovalAuthority(userIds, runtimeData);
            // 缓存当前审批处理
            approvers = getApproverCache(key, runtimeData);
            if (VERIFY_TYPES.contains(currentOperationType)
                    && NodePropertiesModeEnum.NEXT.equals(mode)
                    && Boolean.FALSE.equals(runtimeData.getUser().getId().equals(userIds.get(approvers.size() - 1)))) {
                // 判断是否顺序审批
                throw new BusinessException("等待其他人审批后才能审批");
            }
            redisUtils.set(key, JSON.toJSONString(approvers), APPROVER_EXPIRE);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }

        // 判断是否通过审批
        ModeDto modeDto = new ApproveModeProcess(mode, node.getProps(), runtimeData, approvers, userIds, currentOperationType).process();
        // 是否结束审批
        if (Boolean.TRUE.equals(modeDto.getEnd())) {
            // 设置所有处理结果的状态为结束
            flowTaskNodeService.updateCourseApproveOver(runtimeData.getFlowTaskNode());
            redisUtils.del(key);
        }
        return modeDto;
    }

    /**
     * 获取审批模式
     *
     * @param node 当前节点
     * @param runtimeData 运行时数据
     * @return 当前节点审批模式
     */
    private NodePropertiesModeEnum getNodeMode(Node node, RuntimeData runtimeData) {
        if (ObjectNull.isNotNull(runtimeData.getCurrentNodeMode())) {
            return runtimeData.getCurrentNodeMode();
        }
        return node.getProps().getMode();
    }

    /**
     * 审批锁key
     *
     * @param id     工作流任务id
     * @param nodeId 工作流节点id
     * @return 审批锁key
     */
    private String getApproverLockKey(String id, String nodeId) {
        return SystemConstant.SYSTEM_NAME + ":" + APPROVER_LOCK + ":" + id + ":" + nodeId;
    }

    /**
     * 得到当前节点审批人集合
     *
     * @param node        节点
     * @param runtimeData 运行时数据
     * @return 当前节点审批人集合（包括所有审批状态的审批人）
     */
    private List<String> getUserIds(Node node, RuntimeData runtimeData) {
        return Optional.ofNullable(flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId())
                .eq(FlowTaskPerson::getNodeId, node.getId()))
        ).orElse(new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(FlowTaskPerson::getNumber))
                .map(FlowTaskPerson::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 校验用户是否有权限审核
     *
     * @param userIds     审批人id集合
     * @param runtimeData 运行时数据
     */
    private void checkApprovalAuthority(List<String> userIds, RuntimeData runtimeData) {
        // 审批人为空，则不校验
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        // 非审批操作，不校验是否有审批权限
        if (!VERIFY_TYPES.contains(runtimeData.getFlowDto().getNodeOperationType())) {
            return;
        }
        boolean flag = userIds.stream().anyMatch(id -> id.equals(runtimeData.getUser().getId()));
        if (Boolean.FALSE.equals(flag)) {
            throw new BusinessException("无权审批");
        }
    }

    /**
     * 从数据库获取当前节点处理结果
     *
     * @return 当前节点处理结果
     */
    private List<ApproverCache> getApproverCache(String key, RuntimeData runtimeData) {
        List<ApproverCache> approvers = Optional.ofNullable(redisUtils.get(key)).map(obj -> JSON.parseArray((String) obj, ApproverCache.class)).orElse(new ArrayList<>());
        // 缓存中没数据，则从数据库中查询当前节点的处理信息
        if (CollectionUtils.isEmpty(approvers)) {
            CourseDto course = flowTaskNodeService.getOne(Wrappers.<FlowTaskNode>lambdaQuery()
                    .eq(FlowTaskNode::getFlowTaskId, runtimeData.getFlowTask().getId())
                    .eq(FlowTaskNode::getNodeId, runtimeData.getNodeId())
                    .select(FlowTaskNode::getCourse)).getCourse();
            if (course != null && CollectionUtils.isNotEmpty(course.getApproveResultDtos())) {
                course.getApproveResultDtos().stream()
                        // 排除转交记录
                        .filter(result -> Boolean.FALSE.equals(NodeOperationTypeEnum.TRANSFER.equals(result.getNodeOperationTypeEnum())))
                        // 排除加签记录
                        .filter(result -> Boolean.FALSE.equals(NodeOperationTypeEnum.APPEND.equals(result.getNodeOperationTypeEnum())))
                        // 排除增加审批人记录
                        .filter(result -> Boolean.FALSE.equals(NodeOperationTypeEnum.ADD_SIGNER.equals(result.getNodeOperationTypeEnum())))
                        // 排除移除审批人记录
                        .filter(result -> Boolean.FALSE.equals(NodeOperationTypeEnum.REMOVE_SIGNER.equals(result.getNodeOperationTypeEnum())))
                        // 筛选处理中的记录
                        .filter(result -> Boolean.FALSE.equals(result.isOver()))
                        .sorted(Comparator.comparing(ApproveResultDto::getTime)).forEach(c -> {
                            ApproverCache approve = new ApproverCache();
                            approve.setId(c.getUserId());
                            approve.setOperationType(c.getNodeOperationTypeEnum().getValue());
                            approvers.add(approve);
                        });
            }
        }

        // 非审批操作不缓存审批记录
        if (!VERIFY_TYPES.contains(runtimeData.getFlowDto().getNodeOperationType())) {
            return approvers;
        }
        // 当前节点的处理结果
        ApproverCache currentApprove = new ApproverCache();
        currentApprove.setId(runtimeData.getUser().getId());
        currentApprove.setOperationType(runtimeData.getFlowDto().getNodeOperationType().getValue());
        approvers.add(currentApprove);
        return approvers;
    }

    /**
     * 审批处理
     */
    private static class ApproveModeProcess {
        // 审批模式
        private NodePropertiesModeEnum mode;
        // 审批模式配置
        private ApprovalModeProperties modeProps;
        // 运行时数据
        private RuntimeData runtimeData;
        // 已审核结果集合
        private List<ApproverCache> approvers;
        // 审核人id集合
        private List<String> userIds;
        // 加签配置
        private AppendApprovalProperties appendApprovalProperties;
        // 审批操作类型
        NodeOperationTypeEnum operationType;

        public ApproveModeProcess(NodePropertiesModeEnum mode, NodeProperties props, RuntimeData runtimeData, List<ApproverCache> approvers, List<String> userIds, NodeOperationTypeEnum operationType) {
            this.mode = mode;
            this.modeProps = props.getModeProps();
            this.runtimeData = runtimeData;
            this.approvers = approvers;
            this.userIds = userIds;
            this.appendApprovalProperties = props.getAppendApproval();
            this.operationType = operationType;
        }

        /**
         * 审批处理
         *
         * @return 处理结果
         */
        public ModeDto process() {
            ModeDto modeDto = new ModeDto();
            // 先判断是否有回退，若有则结束审批 (不论当前是否是加签审批，加签审批结果是否生效，都不影响回退操作生效)
            boolean back = approvers.stream().anyMatch(e -> NodeOperationTypeEnum.BACK.getValue().equals(e.getOperationType()));
            if (Boolean.TRUE.equals(back)) {
                back(modeDto);
                return modeDto;
            }

            // 非回退操作（同意、拒绝）判断
            // 当前审批是加签审批，且加签审批结果不生效
            if (FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL.equals(runtimeData.getFlowTaskNode().getApprovalType())) {
                // 加签审批结果不生效，所有加签审批人都审批就结束，默认通过审批
                if (Boolean.FALSE.equals(appendApprovalProperties.getValidApproval())) {
                    all(modeDto);
                    return modeDto;
                }
            }

            // 正常审批 || 加签审批且审批结果生效
            switch (mode) {
                case AND:
                    countersign(modeDto);
                    break;
                case NEXT:
                    successively(modeDto);
                    break;
                case OR:
                    or(modeDto);
                    break;
                default:
                    defaultMode(modeDto);
                    break;
            }
            return modeDto;
        }

        /**
         * 默认审批模式
         *
         * @param modeDto 处理结果
         */
        private void defaultMode(ModeDto modeDto) {
            // TRUE-未完全通过(直接结束)，FALSE-完全通过
            boolean noTallPassed = approvers.stream().anyMatch(e -> !NodeOperationTypeEnum.PASS.getValue().equals(e.getOperationType()));
            modeDto.setEnd(noTallPassed || userIds.size() == approvers.size());
            modeDto.setPass(!noTallPassed);
        }

        /**
         * 会签（并行）
         *
         * @param modeDto 处理结果
         */
        private void countersign(ModeDto modeDto) {
            // 未配置会签规则，则默认执行的规则为 “所有审批人都要完成审批”
            if (ObjectNull.isNull(modeProps)) {
                defaultMode(modeDto);
            } else {
                // 执行会签规则
                countersignRule(modeDto);
            }
        }

        /**
         * 按选择顺序依次审批
         *
         * @param modeDto 处理结果
         */
        private void successively(ModeDto modeDto) {
            // 正常审批操作流转校验逻辑
            if (VERIFY_TYPES.contains(operationType)) {
                // 不是通过 或 所有人都执行了审批，则该节点结束审批
                boolean pass = NodeOperationTypeEnum.PASS.equals(operationType);
                modeDto.setEnd(Boolean.FALSE.equals(pass) || userIds.size() == approvers.size());
                modeDto.setPass(pass);
                return;
            }
            // 移除审批人操作流转校验逻辑
            if (NodeOperationTypeEnum.REMOVE_SIGNER.equals(operationType)) {
                modeDto.setEnd(userIds.size() == approvers.size());
                modeDto.setPass(true);
            }

        }

        /**
         * 或签
         *
         * @param modeDto 处理结果
         */
        private void or(ModeDto modeDto) {
            if (modeProps.getEndNow()) {
                // 只要是通过|拒绝就结束审批
                boolean pass = NodeOperationTypeEnum.PASS.equals(operationType);
                modeDto.setEnd(pass || NodeOperationTypeEnum.REFUSE.equals(operationType));
                modeDto.setPass(pass);
            } else {
                // 有一人同意即为通过
                modeDto.setPass(approvers.stream().anyMatch(e -> NodeOperationTypeEnum.PASS.getValue().equals(e.getOperationType())));
                // 有一人同意 或 所有人都执行了审批，则该节点结束审批
                modeDto.setEnd(Boolean.TRUE.equals(modeDto.getPass()) ? Boolean.TRUE : approvers.size() == userIds.size());
            }
        }

        /**
         * 所有人都审批就结束，默认通过审批
         *
         * @param modeDto 处理结果
         */
        private void all(ModeDto modeDto) {
            modeDto.setEnd(userIds.size() == approvers.size());
            modeDto.setPass(Boolean.TRUE);
        }

        /**
         * 回退
         *
         * @param modeDto 处理结果
         */
        private void back(ModeDto modeDto) {
            modeDto.setEnd(Boolean.TRUE);
            modeDto.setPass(Boolean.FALSE);
        }

        /**
         * 执行会签规则
         *
         * @param modeDto 处理结果
         */
        private void countersignRule(ModeDto modeDto) {
            // 按比例执行判断
            if (CountersignRuleEnum.RATIO.equals(modeProps.getCountersignRule())) {
                countersignRuleRatio(modeDto);
            }
        }

        /**
         * 会签规则 —— 比例
         *
         * @param modeDto 处理结果
         */
        private void countersignRuleRatio(ModeDto modeDto) {
            // 计算已通过比例
            long passedCount = approvers.stream().filter(e -> NodeOperationTypeEnum.PASS.getValue().equals(e.getOperationType())).count();
            long passRatio = Math.round((double) passedCount / userIds.size() * 100);
            // 计算剩余未审批比例
            long surplusRatio = Math.round((double) (userIds.size() - approvers.size()) / userIds.size() * 100);
            // true-不可能达到阈值，false-还有机会达到阈值
            // 比例阈值 > 已通过比例 + 剩余未审批比例。表示已不可能达到阈值。审批结果必然是不通过
            boolean impossiblePass = modeProps.getValue() > passRatio + surplusRatio;
            // true-已达到比例阈值，false-未达到比例阈值
            boolean thresholdReached = passRatio >= modeProps.getValue();
            // 当前节点审批结束：((不可能达到阈值 || 已达到阈值) && 满足条件立即结束） || 已全部审批
            boolean end = ((impossiblePass || thresholdReached) && modeProps.getEndNow()) || userIds.size() == approvers.size();
            modeDto.setEnd(end);
            modeDto.setPassRate(passRatio);
            // 已结束，设置审批结果
            if (end) {
                modeDto.setPass(thresholdReached);
            }
        }

    }

    /**
     * 用户审核操作缓存
     */
    private static class ApproverCache {
        // 用户id
        private String id;
        // 审核操作类型：NodeOperationTypeEnum
        private String operationType;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }
    }

}
