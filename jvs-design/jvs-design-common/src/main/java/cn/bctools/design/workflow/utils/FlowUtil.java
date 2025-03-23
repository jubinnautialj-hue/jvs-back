package cn.bctools.design.workflow.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.model.NodeProperties;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeGroupEnum;
import cn.bctools.design.workflow.model.properties.BackProperties;
import cn.bctools.design.workflow.model.properties.FlowButton;
import cn.bctools.design.workflow.model.properties.PersonnelScope;
import cn.bctools.design.workflow.model.properties.Target;
import cn.bctools.design.workflow.support.function.dto.BackResubmitDto;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流工具
 */
@Slf4j
public class FlowUtil {
    private FlowUtil() {
    }

    private static final MD5 MD_5 = MD5.create();

    /**
     * 工作流节点缓存
     * Map<节点id, 节点>
     */
    private static ThreadLocal<Map<String, Node>> nodeCache = new TransmittableThreadLocal<>();

    /**
     * 设置自选审批人时，达到最小阈值，设置为会签
     */
    private static final Integer MIN_SELF_SELECT_COUNTERSIGN_THRESHOLD = 2;

    /**
     * 设置工作流节点缓存
     *
     * @param node 节点
     */
    private static void setNodeCache(Node node) {
        Node currentNode = BeanCopyUtil.copy(node, Node.class);
        // 不保留下级节点的下级节点信息
        if (currentNode.getNode() != null) {
            currentNode.getNode().setNode(null);
        }
        Map<String, Node> nodeMap = Optional.ofNullable(getNodeCache()).orElse(new HashMap<>(1));
        nodeMap.put(currentNode.getId(), currentNode);
        nodeCache.set(nodeMap);
    }

    /**
     * 获取工作流节点缓存
     *
     * @return Map<节点id, 节点对象>
     */
    public static Map<String, Node> getNodeCache() {
        return nodeCache.get();
    }

    /**
     * 清除当前工作流设计的缓存
     */
    public static void clearNodeCache() {
        nodeCache.remove();
    }

    /**
     * 解析工作流JSON字符串，并将所有节点缓存到本地线程
     * 注：业务处理完成后，需要清空缓存
     *
     * @param flow 流程设计
     * @return 流程节点对象
     */
    public static Node parseNodeJsonAndCache(String flow) {
        Node node = parseNodeJson(flow);
        updateNode(node, null);
        return node;
    }

    /**
     * 工作流json转换
     *
     * @param flow 工作流设计json
     * @return 流程节点对象
     */
    public static Node parseNodeJson(String flow) {
        if (StringUtils.isBlank(flow)) {
            throw new BusinessException("流程设计不存在");
        }
        // JSON转换
        Node node = JSON.parseObject(flow, Node.class);
        if (node.getId() == null) {
            throw new BusinessException("工作流解析失败");
        }
        return node;
    }

    /**
     * 修改Node结构,并将所有节点缓存到本地线程
     *
     * @param node 流程节点对象
     * @param branchNextNodeId 多分支节点的下级节点id
     */
    private static void updateNode(Node node, String branchNextNodeId) {
        Node nextNode = node.getNode();
        if (nextNode != null && ObjectNull.isNotNull(nextNode.getId())) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                String nextNodeId = node.getNode().getId().equals(branchNextNodeId) ? branchNextNodeId : node.getNode().getId();
                node.getConditions().forEach(n -> {
                    n.setBranchNextNodeId(nextNodeId);
                    updateNode(n, n.getBranchNextNodeId());
                });
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                String nextNodeId = node.getNode().getId().equals(branchNextNodeId) ? branchNextNodeId : node.getNode().getId();
                node.getParallels().forEach(n -> {
                    n.setBranchNextNodeId(nextNodeId);
                    updateNode(n, n.getBranchNextNodeId());
                });
            }

            nextNode.setBranchNextNodeId(branchNextNodeId);
            updateNode(nextNode, branchNextNodeId);
        }
        setNodeCache(node);
    }

    /**
     * 是否null节点
     *
     * @param node 节点
     * @return TRUE-null节点，FALSE-正常节点
     */
    public static Boolean isNullNode(Node node) {
        return node == null || StringUtils.isBlank(node.getId());
    }

    /**
     * 获取节点
     *
     * @param flow   工作流设计json
     * @param nodeId 节点id
     * @return 节点
     */
    public static Node findNode(String flow, String nodeId) {
        Node resultNode;
        // 解析工作流设计json
        parseNodeJsonAndCache(flow);
        // 获取指定节点
        resultNode = findNode(nodeId);
        return resultNode;
    }

    /**
     * 获取节点
     * 从本地线程缓存中获取指定节点
     *
     * @param nodeId 节点id
     * @return 节点
     */
    public static Node findNode(String nodeId) {
        return findNode(nodeId, Boolean.FALSE);
    }

    /**
     * 获取节点
     * 从本地线程缓存中获取指定节点
     *
     * @param nodeId   节点id
     * @param rootNode TRUE-获取根节点，FALSE-根据节点id获取节点
     * @return 节点
     */
    public static Node findNode(String nodeId, boolean rootNode) {
        Map<String, Node> nodeCache = getNodeCache();
        if (MapUtils.isEmpty(nodeCache)) {
            log.warn("调用FlowUtil.findNode(String nodeId, boolean rootNode)方法，需先缓存工作流设计节点到本地线程");
            return null;
        }
        // 返回根节点
        if (Boolean.TRUE.equals(rootNode)) {
            return nodeCache.entrySet().stream()
                    .filter(e -> NodeTypeEnum.ROOT.equals(e.getValue().getType())).findFirst()
                    .orElseThrow(() -> new BusinessException("未找到开始节点"))
                    .getValue();
        }
        return nodeCache.get(nodeId);
    }

    /**
     * 获取开始节点
     *
     * @param flow 流程设计
     * @return 根节点
     */
    public static Node getRootNode(String flow) {
        Map<String, Node> nodeCache = getNodeCache();
        if (MapUtils.isNotEmpty(nodeCache)) {
            return nodeCache.entrySet().stream().filter(e -> NodeTypeEnum.ROOT.equals(e.getValue().getType())).findAny().get().getValue();
        }
        Node node = parseNodeJson(flow);
        node.setNode(null);
        return node;
    }

    /**
     * 得到发起人表单信息
     *
     * @param flow 工作流设计json
     * @return 发起人表单信息
     */
    public static NodeForm getSendUserForm(String flow) {
        Node node = parseNodeJson(flow);
        NodeForm nodeForm = Optional.ofNullable(node.getNodeForm()).orElse(NodeForm.buildDefault());
        if (StringUtils.isBlank(nodeForm.getFormId())) {
            nodeForm.setFormId(null);
            nodeForm.setVersion(null);
        }
        return nodeForm;
    }

    /**
     * 得到节点表单信息
     *
     * @param flow   工作流设计
     * @param nodeId 节点id
     * @return 节点表单信息
     */
    public static NodeForm getNodeForm(String flow, String nodeId) {
        if (StringUtils.isBlank(nodeId)) {
            return NodeForm.buildDefault();
        }
        Node node = findNode(nodeId);
        node = FlowUtil.isNullNode(node) ? findNode(flow, nodeId) : node;
        if (FlowUtil.isNullNode(node)) {
            return NodeForm.buildDefault();
        }
        return Optional.ofNullable(node.getNodeForm()).orElse(NodeForm.buildDefault());
    }

    /**
     * 得到节点表单信息
     *
     * @param node 节点
     * @return 节点表单信息
     */
    public static NodeForm getNodeForm(Node node) {
        if (FlowUtil.isNullNode(node)) {
            return NodeForm.buildDefault();
        }
        return Optional.ofNullable(node.getNodeForm()).orElse(NodeForm.buildDefault());
    }

    /**
     * 获取人工节点集合
     *
     * @param flow 工作流设计json
     * @return 人工节点集合
     */
    public static List<Node> getManualNodes(String flow) {
        Node node = parseNodeJson(flow);
        List<Node> manualNodes = new ArrayList<>();
        getManualNodes(node, manualNodes);
        return manualNodes;
    }

    /**
     * 得到人工节点集合
     *
     * @param node  节点信息
     * @param nodes 人工节点集合
     */
    private static void getManualNodes(Node node, List<Node> nodes) {
        // 人工节点添加到集合中
        if (node.getType() != null && NodeTypeGroupEnum.MANUAL.equals(node.getType().getGroup())) {
            nodes.add(node);
        }
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n ->
                        getManualNodes(n, nodes)
                );
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n ->
                        getManualNodes(n, nodes)
                );
            }
            getManualNodes(nextNode, nodes);
        }
    }

    /**
     * 自选审批人设置
     * 修改工作流设计中的审批人信息
     *
     * @param start     启动流程
     * @param flow      工作流设计json
     * @param extend    工作流设计高级配置
     * @param approvers 自选审批人集合
     * @return 将自选审批人填充到设计后，返回新的流程设计
     */
    public static String setSelfSelectApprover(boolean start, String flow, FlowExtendDto extend, List<SelfSelectApprover> approvers) {
        Node node = parseNodeJson(flow);
        setSelfSelectApprover(start, node, extend, approvers);
        return JSON.toJSONString(node);
    }

    /**
     * 自选审批人设置
     * 修改工作流设计中的审批人信息
     *
     * @param start     启动流程
     * @param node      节点信息
     * @param extend    工作流设计高级配置
     * @param approvers 自选审批人集合
     */
    private static void setSelfSelectApprover(boolean start, Node node, FlowExtendDto extend, List<SelfSelectApprover> approvers) {
        if (StringUtils.isNotBlank(node.getId())) {
            NodeProperties nodeProperties = Optional.ofNullable(node.getProps()).orElseGet(NodeProperties::new);
            NodePropertiesTypeEnum propertiesType = nodeProperties.getType();
            Target target = Optional.ofNullable(nodeProperties.getTargetObj()).orElseGet(Target::new);
            Optional<SelfSelectApprover> selfSelectOptional = Optional.ofNullable(approvers).orElseGet(ArrayList::new)
                    .stream()
                    .filter(self -> node.getId().equals(self.getNodeId()) && CollectionUtils.isNotEmpty(self.getApprovers()))
                    .findFirst();
            // 审批人类型为“发起人自选”的节点必须在启动流程时设置审批人
            String msg = NodeTypeEnum.SP.equals(node.getType()) ? "节点未设置审批人" : "环节未设置抄送人请检查设计";
            if (NodePropertiesTypeEnum.SELF_SELECT.equals(propertiesType) && CollectionUtils.isEmpty(target.getPersonnels())) {
                if (Boolean.FALSE.equals(selfSelectOptional.isPresent())) {
                    throw new BusinessException(msg, node.getName());
                }
            }
            // 修改节点审批人信息
            selfSelectOptional.ifPresent(selfSelect -> {
                // 是发起流程，且是发起人自选，可设置审批人
                // 不是发起流程，则启用了”允许动态选择审批人“开关，且节点未禁止动态选择审批人，才能动态设置人员
                boolean canUpdatePersonnel = (start && NodePropertiesTypeEnum.SELF_SELECT.equals(propertiesType))
                        || (extend.getEnableDynamicApprover() && !FlowUtil.getDisableDynamicApprover(nodeProperties));
                if (canUpdatePersonnel) {
                    // 修改节点审批人信息
                    target.setPersonnels(selfSelect.getApprovers());
                    // 校验节点审批人类型
                    target.checkPersonType(propertiesType);
                    // 若审批节点的审批模式是默认的，且审批人大于1个，则修改审批模式为会签
                    boolean countersign = selfSelect.getApprovers().size() >= MIN_SELF_SELECT_COUNTERSIGN_THRESHOLD
                            && (NodePropertiesModeEnum.DEFAULT.equals(nodeProperties.getMode()) || ObjectNull.isNull(nodeProperties.getMode()));
                    if (countersign) {
                        node.getProps().setMode(NodePropertiesModeEnum.AND);
                    }
                } else {
                    log.warn("自选审批人失败：未启用允许动态选择审批人");
                }
            });
            // 未启用动态选择审批人，且未配置默认审批人，不允许启动流程
            boolean approverIsBlank = start && CollectionUtils.isEmpty(target.getPersonnels()) && NodeTypeEnum.SP.equals(node.getType()) &&
                    (NodePropertiesTypeEnum.SELF_SELECT.equals(propertiesType) || NodePropertiesTypeEnum.ASSIGN_USER.equals(propertiesType));
            if (approverIsBlank) {
                throw new BusinessException(msg, node.getName());
            }
        }
        // 继续遍历后续节点
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n ->
                        setSelfSelectApprover(start, n, extend, approvers)
                );
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n ->
                        setSelfSelectApprover(start, n, extend, approvers)
                );
            }
            setSelfSelectApprover(start, nextNode, extend, approvers);
        }
    }

    /**
     * 根据节点类型获取节点集合
     *
     * @param flowDesign    工作流设计
     * @param nodeTypeEnums 节点类型
     * @return 节点集合
     */
    public static List<Node> getNodesByNodeType(String flowDesign, List<NodeTypeEnum> nodeTypeEnums) {
        clearNodeCache();
        parseNodeJsonAndCache(flowDesign);
        Map<String, Node> nodeMap = getNodeCache();
        if (MapUtils.isEmpty(nodeMap)) {
            return Collections.emptyList();
        }
        // 得到所有节点
        List<Node> nodes = nodeMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        // 若节点类型不为空，则根据节点类型筛选
        if (CollectionUtils.isNotEmpty(nodeTypeEnums)) {
            nodes = nodes.stream().filter(node -> nodeTypeEnums.contains(node.getType())).collect(Collectors.toList());
        }
        return nodes;
    }

    /**
     * 获取指定节点的下级节点
     *
     * @param node 节点
     * @return 下级节点
     */
    public static Node getNextNode(Node node) {
        // 若有下级node，则返回下级节点
        // 若无下级node，判断是否有分支外层nodeId，若有则根据nodeId获取节点
        Node nextNode = node.getNode();
        if (isNullNode(nextNode)) {
            nextNode = StringUtils.isNotBlank(node.getBranchNextNodeId()) ? FlowUtil.findNode(node.getBranchNextNodeId()) : null;
        }
        return nextNode;
    }

    /**
     * 获取指定节点所属并行分支
     *
     * @param node 节点
     * @return 并行分支
     */
    public static Node getParallelBranchNode(Node node) {
        Node parallelBranchNode = null;
        boolean flag = true;
        while (flag) {
            Node parentNode = findNode(node.getPid());
            if (isNullNode(parentNode)) {
                flag = false;
                continue;
            }
            if (NodeTypeEnum.PB.equals(parentNode.getType())) {
                parallelBranchNode = parentNode;
                flag = false;
            }
            node = parentNode;
        }
        return parallelBranchNode;
    }

    /**
     * 得到工作流设计的节点有序集合
     *
     * @param design        工作流设计json
     * @param nodeTypeEnums 节点类型
     * @return 工作流设计节点集合（按顺序；只有基本数据，若要更多数据，需要再通过节点id获取）
     */
    public static List<Node> getOrderNodes(String design, List<NodeTypeEnum> nodeTypeEnums) {
        if (StringUtils.isBlank(design)) {
            return Collections.emptyList();
        }
        List<List<Node>> paths = FlowPathUtil.getNodePaths(design);
        return getOrderNodes(paths, nodeTypeEnums);
    }

    /**
     * 得到指定节点所有后续节点的有序集合
     *
     * @param design        工作流设计json
     * @param specifyNodeId 指定节点id
     * @param nodeTypeEnums 节点类型
     * @return 指定节点所有后续节点的有序集合（按顺序；只有基本数据，若要更多数据，需要再通过节点id获取）
     */
    public static List<Node> getOrderNodes(String design, String specifyNodeId, List<NodeTypeEnum> nodeTypeEnums) {
        if (StringUtils.isBlank(design)) {
            return Collections.emptyList();
        }
        Node specifyNode = findNode(design, specifyNodeId);
        return getOrderNodes(design, specifyNode, nodeTypeEnums);
    }

    /**
     * 得到指定节点所有后续节点的有序集合
     *
     * @param design        工作流设计json
     * @param specifyNode   指定节点
     * @param nodeTypeEnums 节点类型
     * @return 指定节点所有后续节点的有序集合（按顺序；只有基本数据，若要更多数据，需要再通过节点id获取）
     */
    public static List<Node> getOrderNodes(String design, Node specifyNode, List<NodeTypeEnum> nodeTypeEnums) {
        if (StringUtils.isBlank(design)) {
            return Collections.emptyList();
        }
        List<List<Node>> paths = FlowPathUtil.getNodePaths(design, specifyNode);
        return getOrderNodes(paths, nodeTypeEnums);
    }

    /**
     * 得到节点有序集合
     *
     * @param paths         节点路径
     * @param nodeTypeEnums 节点类型
     * @return 节点有序集合
     */
    private static List<Node> getOrderNodes(List<List<Node>> paths, List<NodeTypeEnum> nodeTypeEnums) {
        if (CollectionUtils.isEmpty(paths)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isNotEmpty(nodeTypeEnums)) {
            return paths.stream()
                    .flatMap(nodes -> nodes.stream().filter(node -> nodeTypeEnums.contains(node.getType())))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return paths.stream().flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

    /**
     * 得到节点的人员选择范围
     *
     * @param node 节点
     * @return 节点的人员选择范围
     */
    public static PersonnelScope getPersonnelScope(Node node) {
        // 审批节点，可返回人员范围
        if (NodeTypeEnum.SP.equals(node.getType())) {
            return ObjectNull.isNull(node.getProps().getPersonnelScope()) ? new PersonnelScope() : node.getProps().getPersonnelScope();
        }
        return null;
    }

    /**
     * 校验设计是否未变更
     *
     * @param design1 设计1
     * @param design2 设计2
     * @return TRUE-设计未变更，FALSE-设计已变更
     */
    public static Boolean checkDesignChange(String design1, String design2) {
        if (ObjectNull.isNull(design1) && ObjectNull.isNull(design2)) {
            return Boolean.TRUE;
        }
        if (ObjectNull.isNull(design1) || ObjectNull.isNull(design2)) {
            return Boolean.FALSE;
        }
        return MD_5.digestHex(design1).equals(MD_5.digestHex(design2));
    }


    /**
     * 获取节点回退配置
     *
     * @param node 节点
     * @return 回退配置
     */
    public static BackProperties getNodeBackProps(Node node) {
        List<FlowButton> btns = node.getProps().getBtn();
        if (CollectionUtils.isEmpty(btns)) {
            return new BackProperties();
        }
        FlowButton flowButton = btns.stream()
                .filter(btn -> NodeOperationTypeEnum.BACK.equals(btn.getOperation()))
                .findFirst()
                .get();
        return Optional.ofNullable(flowButton.getBack()).orElseGet(BackProperties::new);
    }

    /**
     * 根据"被回退的数据重新提交配置"确定下一步流转的节点
     * <p>
     * - 逆序遍历集合，判断当前审批操作是否是审批回退后的第一次提交
     * - 若是审批回退后的第一次提交：根据回退节点的回退配置，返回下一步流转的节点
     *
     * @param currentNodeId 当前节点
     * @param flowTask      流程任务
     * @return 下一步流转的节点以及回退相关配置
     */
    public static BackResubmitDto parseBackTaskResubmitNextNode(String currentNodeId, FlowTask flowTask) {
        LinkedList<CourseDto> courses = Optional.ofNullable(flowTask.getCourses()).orElseGet(LinkedList::new);
        // 找到上一步回退节点
        Node previousBackNode = null;
        ListIterator<CourseDto> listIterator = courses.listIterator(courses.size());
        int i = 0;
        while (listIterator.hasPrevious()) {
            CourseDto courseDto = listIterator.previous();
            // 以开始节点为界限，得到最后一个开始节点之后的所有审批人(因为可能重启过流程)
            // 回退到发起人节点，倒序的第一个节点就是ROOT，需要跳过
            if (NodeTypeEnum.ROOT.equals(courseDto.getNodeType()) && i >= 1) {
                break;
            }
            boolean havePreviousBackNode = courseDto.getApproveResultDtos()
                    .stream()
                    .anyMatch(approve -> NodeOperationTypeEnum.BACK.equals(approve.getNodeOperationTypeEnum()) && currentNodeId.equals(approve.getBackNodeId()));
            if (havePreviousBackNode) {
                previousBackNode = FlowUtil.findNode(courseDto.getNodeId());
                break;
            }
            i++;
        }

        BackResubmitDto resubmitDto = new BackResubmitDto();
        Node nextNode = null;
        if (ObjectNull.isNull(previousBackNode)) {
            return resubmitDto.setWhetherResubmit(false).setNextNode(nextNode);
        }

        // 根据回退配置，确定下一步流转的节点
        Node backNode = FlowUtil.findNode(previousBackNode.getId());
        BackProperties backProperties = FlowUtil.getNodeBackProps(backNode);
        BackTaskResubmitEnum resubmit = backProperties.getResubmit();
        switch (resubmit) {
            case DIRECT_CURRENT_NODE:
                // 直达当前节点（发起回退的节点）
                nextNode = backNode;
                break;
            case SEQUENCE:
                // 按流程顺序审批：不做处理
            default:
                break;
        }

        return resubmitDto.setWhetherResubmit(true)
                .setNextNode(nextNode)
                .setBackTaskResubmit(backProperties.getResubmit());
    }

    /**
     * 节点配置是否禁用动态选择审批人
     *
     * @param props 节点配置
     * @return true-禁用动态选择审批人，false-不禁用动态选择审批人
     */
    public static Boolean getDisableDynamicApprover(NodeProperties props) {
        boolean disableDynamicApprover = true;
        switch (props.getType()) {
            case SELF_SELECT:
                // 发起人自选，只能是false
                disableDynamicApprover = false;
                break;
            case JOB:
            case ROLE:
            case ASSIGN_USER:
                // 兼容旧数据，未配置是否禁止动态选择审批人时，默认为false
                disableDynamicApprover = Optional.ofNullable(props.getDisableDynamicApprover()).orElse(false);
                break;
            default:
                // 其它类型，默认禁止动态选择审批人
                break;
        }
        return disableDynamicApprover;
    }

    /**
     * 获取节点是否可动态选择审批人配置
     *
     * @param extend 流程高级设置
     * @param node 流程节点
     * @return true-可动态选择审批人，false-不能动态选择审批人
     */
    public static boolean getNodeCanDynamicApprover(FlowExtendDto extend, Node node) {
        return extend.getEnableDynamicApprover() && !FlowUtil.getDisableDynamicApprover(node.getProps());
    }


    /**
     * 从审批过程中提取最终审批过程
     * <p>
     *     每个审批节点只保留最后一次审批记录（排除回退操作，也就是从流程起点至终点，只保留最终一份记录）
     *
     * @param flowTask 流程实例
     * @param nodePaths 流程路径集合
     * @return 最终审批记录
     */
    public static List<CourseDto> extractFinalTaskCourse(FlowTask flowTask, List<List<Node>> nodePaths) {
        LinkedList<CourseDto> finalTaskCourse = new LinkedList<>();
        if (ObjectNull.isNull(flowTask) || ObjectNull.isNull(flowTask.getCourses())) {
            return finalTaskCourse;
        }
        // 已添加 和 不需要添加的节点id
        Set<String> excludedIds  = new HashSet<>();
        LinkedList<CourseDto> courses = flowTask.getCourses();
        ListIterator<CourseDto> listIterator = courses.listIterator(courses.size());
        while (listIterator.hasPrevious()) {
            CourseDto courseDto = listIterator.previous();
            // 以开始节点为界限，得到最后一个开始节点之后的所有审批人(因为可能重启过流程)
            // 回退到发起人节点，倒序的第一个节点就是ROOT，需要跳过
            if (NodeTypeEnum.ROOT.equals(courseDto.getNodeType())) {
                break;
            }
            String nodeId = courseDto.getNodeId();

            // 若是回退操作，找到回退目标节点与当前节点之间的所有节点id，这部分节点的审批记录不转换
            List<String> backBetweenNodeIds = getBackSubListBetween(courseDto, nodePaths);
            if (ObjectNull.isNotNull(backBetweenNodeIds)) {
                excludedIds.addAll(backBetweenNodeIds);
            }
            // 跳过已转换或不需要转换的节点
            if (excludedIds.contains(nodeId)) {
                continue;
            }
            excludedIds.add(nodeId);
            finalTaskCourse.addFirst(courseDto);
        }
        return finalTaskCourse;
    }

    /**
     * 获取回退目标节点与回退节点之间（包括回退节点id和回退目标节点id）的节点id
     *
     * @param courseDto 审批记录
     * @param nodePaths 节点路径
     * @return 在回退目标节点与回退节点之间的节点id集合
     */
    public static List<String> getBackSubListBetween(CourseDto courseDto, List<List<Node>> nodePaths) {
        Optional<ApproveResultDto> backOptional = courseDto.getApproveResultDtos().stream()
                .filter(re -> NodeOperationTypeEnum.BACK.equals(re.getNodeOperationTypeEnum()))
                .findFirst();
        if (!backOptional.isPresent()) {
            return Collections.emptyList();
        }
        String backNodeId = backOptional.get().getBackNodeId();
        String nodeId = courseDto.getNodeId();
        List<String> nodeIds = Arrays.asList(backNodeId, nodeId);
        return nodePaths.stream()
                .map(paths -> {
                    List<String> pathNodeIds = paths.stream().map(Node::getId).collect(Collectors.toList());
                    if (pathNodeIds.containsAll(nodeIds)) {
                        int backNodeIdIndex = pathNodeIds.indexOf(backNodeId);
                        int nodeIdIndex = pathNodeIds.indexOf(nodeId);
                        List<String> betweenNodeIds = pathNodeIds.subList(backNodeIdIndex, nodeIdIndex);
                        betweenNodeIds.add(nodeId);
                        return betweenNodeIds;
                    }
                    return null;
                })
                .filter(ObjectNull::isNotNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
