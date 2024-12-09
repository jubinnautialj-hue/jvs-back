package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.bctools.common.utils.graph.GraphUtils;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.permission.HavePermissionDesignResDto;
import cn.bctools.design.workflow.dto.permission.PermissionDesignDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.mapper.FlowDesignMapper;
import cn.bctools.design.workflow.model.Condition;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.NodeForm;
import cn.bctools.design.workflow.model.NodeProperties;
import cn.bctools.design.workflow.model.enums.ConditionPropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import cn.bctools.design.workflow.model.properties.AutomationProperties;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowDesignVersionService;
import cn.bctools.design.workflow.service.FlowPurviewService;
import cn.bctools.design.workflow.support.valid.ValidatedFlowDesign;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流设计(模拟) 服务实现类
 */
@Slf4j
@Service
@AllArgsConstructor
@Design(DesignType.workflow)
public class FlowDesignServiceImpl extends ServiceImpl<FlowDesignMapper, FlowDesign> implements FlowDesignService {

    private final RuleDesignService ruleService;
    private final DataModelService dataModelService;
    private final FormService formService;
    private final FlowPurviewService flowPurviewService;
    private final IdentificationService identificationService;
    private final FlowDesignVersionService flowDesignVersionService;
    private final FunctionBusinessMapper functionBusinessMapper;

    @Override
    public FlowDesign quickCreation(QuickCreationFlowDesignReqDto dto) {
        FlowDesign flowDesign = new FlowDesign();
        flowDesign.setJvsAppId(dto.getJvsAppId());
        flowDesign.setDataModelId(dto.getDataModelId());
        flowDesign.setName("未命名");
        flowDesign.setDesignGroup("未分类");
        save(flowDesign);
        return flowDesign;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void published(String id, String appId) {
        FlowDesign flowDesign = getById(id);
        if (Boolean.FALSE.equals(flowDesign.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        // 若有发起人表单，则判断发起人表单是否有设计，若无则不可发布
        if (StringUtils.isNotBlank(flowDesign.getFormId())) {
            FormPo formPo = formService.getOne(Wrappers.<FormPo>lambdaQuery().eq(FormPo::getId, flowDesign.getFormId()).select(FormPo::getViewJson));
            Optional.ofNullable(formPo).map(FormPo::getViewJson).orElseThrow(() -> new BusinessException("请完善发起人表单设计"));
        }
        String design = getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.DESIGNING).getDesignBody();
        // 校验工作流设计json是否可发布
        ValidatedFlowDesign.valid(design, flowDesign.getExtend());
        // 校验发起人表单
        checkSendFormDesign(flowDesign.getFormId());
        // 保存可发起工作流的权限配置
        Node node = FlowUtil.parseNodeJson(design);
        flowPurviewService.save(flowDesign, node);

        // 发布
        flowDesign.setPublished(Boolean.TRUE);
        flowDesign.setDesign(flowDesign.getDesigning());
        flowDesign.setId(id);
        flowDesign.setDesignChanged(Boolean.FALSE);
        updateById(flowDesign);
        flowDesignVersionService.publish(id);
    }

    /**
     * 若存在发起人表单id，则校验发起人表单必须有设计
     *
     * @param formId
     */
    private void checkSendFormDesign(String formId) {
        if (StringUtils.isBlank(formId)) {
            return;
        }
        FormPo formPo = formService.get(formId);
        FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
        List<DataFieldPo> dataFieldPos = DesignUtils.getFields(formDesignHtml, formPo.getDataModelId(), formPo.getId());
        if (CollectionUtils.isEmpty(dataFieldPos)) {
            throw new BusinessException("请完善发起人表单设计");
        }
    }

    @Override
    public void stop(String id, String appId) {
        FlowDesign byId = getById(id);
        if (Boolean.FALSE.equals(byId.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        FlowDesign flowDesign = new FlowDesign();
        flowDesign.setId(id);
        flowDesign.setPublished(Boolean.FALSE);
        updateById(flowDesign);
    }

    @Override
    public void update(UpdateFlowDesignReqDto dto) {
        update(null, new LambdaUpdateWrapper<FlowDesign>().eq(FlowDesign::getId, dto.getId())
                .set(FlowDesign::getJvsAppId, dto.getJvsAppId())
                .set(FlowDesign::getName, dto.getName())
                .set(FlowDesign::getDesignGroup, dto.getDesignGroup())
                .set(FlowDesign::getIcon, dto.getIcon())
                .set(FlowDesign::getExtend, JSON.toJSONString(dto.getExtend()))
                .set(FlowDesign::getDescription, dto.getDescription())
        );
        // 修改数据模型名
        String modelId = Optional.ofNullable(getOne(Wrappers.<FlowDesign>lambdaQuery().eq(FlowDesign::getId, dto.getId()).select(FlowDesign::getDataModelId))).orElseGet(FlowDesign::new).getDataModelId();
        String name = dto.getName();
        dataModelService.updateName(modelId, name);
        // 修改自定义标识冗余的设计名称
        identificationService.updateDesignName(dto.getId(), name);
    }

    @Override
    public FlowDesign delete(String id, String appId) {
        FlowDesign flowDesign = getOne(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getJvsAppId, appId)
                .eq(FlowDesign::getId, id)
                .select(FlowDesign::getDataModelId));
        if (ObjectNull.isNull(flowDesign)) {
            return null;
        }
        removeById(id);
        // 删除权限设计
        flowPurviewService.delete(id);
        return flowDesign;
    }

    @Override
    public FlowDesign saveDesign(SaveFlowReqDesign dto) {
        // 获取表单id
        String nodeFormId = getFormId(dto);
        Node node = BeanCopyUtil.copy(dto.getDesignBody(), Node.class);
        String design = JSON.toJSONString(node);
        // 节点新建自定义表单id，则封装节点表单id
        if (StringUtils.isNotBlank(dto.getNodeId()) && StringUtils.isNotBlank(nodeFormId)) {
            design = JSON.toJSONString(addNodeForm(node, dto.getNodeId(), nodeFormId));
        }
        FlowDesign flowDesign = BeanCopyUtil.copy(dto, FlowDesign.class);
        flowDesign.setDesigning(design);
        // 发起人表单配置
        NodeForm nodeForm = FlowUtil.getSendUserForm(design);
        flowDesign.setFormId(nodeForm.getFormId());
        flowDesign.setFormVersion(nodeForm.getVersion());
        // 若是流程设计发生变更，则设置为已变更
        String publishedDesign = getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.USE).getDesignBody();
        if (Boolean.FALSE.equals(FlowUtil.checkDesignChange(publishedDesign, design))) {
            flowDesign.setDesignChanged(Boolean.TRUE);
        }
        // 保存集成&自动化节点配置的逻辑引擎id集合
        flowDesign.setRuleKeys(getRuleIds(design));

        // 循环设计校验
        checkCyclicDesign(flowDesign.getId(), flowDesign.getRuleKeys(), flowDesign.getJvsAppId());

        // 保存设计版本
        flowDesignVersionService.saveVersionDesigning(flowDesign.getId(), flowDesign.getJvsAppId(), design);
        flowDesign.setDesignBody(design);
        // 工作流增加版本功能后，设计主表不再保存设计
        flowDesign.setDesign(null);
        flowDesign.setDesigning(null);

        // 保存设计
        updateById(flowDesign);
        // 修改模型名称
        String dataModelId = Optional.ofNullable(getById(flowDesign.getId())).orElseGet(FlowDesign::new).getDataModelId();
        dataModelService.updateName(dataModelId, flowDesign.getName());
        return flowDesign;
    }

    /**
     * 获取表单id
     *
     * @param dto 保存工作流设计参数
     * @return 表单id
     */
    private String getFormId(SaveFlowReqDesign dto) {
        // 节点表单id
        NodeForm nodeForm = FlowUtil.getNodeForm(JSON.toJSONString(dto.getDesignBody()), dto.getNodeId());
        String formId = nodeForm.getFormId();
        // 指定节点自定义表单，表单id不存在，则创建表单id
        if (StringUtils.isNotBlank(dto.getNodeId()) && StringUtils.isBlank(formId)) {
            FlowDesign flowDesign = Optional.ofNullable(getById(dto.getId())).orElseThrow(() -> new BusinessException("流程设计不存在"));
            FormPo formPo = formService.create(flowDesign.getDataModelId(), flowDesign.getJvsAppId(), "");
            formId = formPo.getId();
        }
        return formId;
    }

    /**
     * 获取工作流设计配置的逻辑引擎设计id集合
     *
     * @param flowDesign 工作流设计
     * @return 逻辑引擎设计id集合
     */
    private List<String> getRuleIds(String flowDesign) {
        // 获取集成&自动化节点集合
        List<Node> nodes = FlowUtil.getNodesByNodeType(flowDesign, Collections.singletonList(NodeTypeEnum.AUTOMATION));
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream()
                .filter(node -> {
                    AutomationProperties automationProperties = node.getProps().getAutomation();
                    return ObjectNull.isNotNull(automationProperties) && StringUtils.isNotBlank(automationProperties.getKey());
                }).map(node -> node.getProps().getAutomation().getKey()).collect(Collectors.toList());
    }

    /**
     * 循环设计校验
     *
     * @param id       工作流id
     * @param ruleKeys 当前工作流调用的逻辑引擎key
     * @param jvsAppId 应用id
     */
    private void checkCyclicDesign(String id, List<String> ruleKeys, String jvsAppId) {
        Map<String, List<String>> map = getRuleFlows();
        map.put(id, ruleKeys);
        map.putAll(ruleService.ruleFlows(jvsAppId));
        if (GraphUtils.hasCycle(map)) {
            throw new BusinessException("检测到工作流和逻辑引擎有循环调用");
        }
    }

    /**
     * 指定节点增加表单配置
     *
     * @param node   节点
     * @param nodeId 节点id
     * @param formId 表单id
     * @return 节点
     */
    private Node addNodeForm(Node node, String nodeId, String formId) {
        if (nodeId.equals(node.getId())) {
            NodeForm nodeForm = Optional.ofNullable(node.getNodeForm()).orElse(NodeForm.buildDefault());
            nodeForm.setFormId(formId);
            nodeForm.setSendUserForm(NodeTypeEnum.ROOT.equals(node.getType()) ? Boolean.TRUE : Boolean.FALSE);
            node.setNodeForm(nodeForm);
            return node;
        }
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n ->
                        addNodeForm(n, nodeId, formId)
                );
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n ->
                        addNodeForm(n, nodeId, formId)
                );
            }
            addNodeForm(nextNode, nodeId, formId);
        }
        return node;
    }

    @Override
    public List<HavePermissionDesignResDto> havePermissionDesign(UserDto userDto, String name) {
        // 查询可发起人id/部门id/角色id/岗位id，包含当前用户的、有发起人表单的已发布工作流
        List<String> havePermissionDesignIds = flowPurviewService.havePermissionDesign(PurviewGroupEnum.send_flow, userDto);
        if (CollectionUtils.isEmpty(havePermissionDesignIds)) {
            return Collections.emptyList();
        }
        List<FlowDesign> flowDesigns = list(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getPublished, Boolean.TRUE)
                .isNotNull(FlowDesign::getFormId)
                .in(FlowDesign::getId, havePermissionDesignIds)
                .like(StringUtils.isNotBlank(name), FlowDesign::getName, name));

        // 批量获取工作流设计
        Map<String, String> designBodyMap = flowDesignVersionService.getBatchDesignBody(flowDesigns, FlowDesignVersionStatusEnum.USE);
        List<HavePermissionDesignResDto> designs = flowDesigns.stream()
                .collect(Collectors.groupingBy(FlowDesign::getDesignGroup))
                .entrySet()
                .stream()
                .map(e -> {
                    HavePermissionDesignResDto havePermissionDesignResDto = new HavePermissionDesignResDto();
                    havePermissionDesignResDto.setDesignGroup(e.getKey());
                    List<PermissionDesignDto> permissionDesignDtos = new ArrayList<>();
                    havePermissionDesignResDto.setFlowDesigns(new ArrayList<>());
                    for (FlowDesign design : e.getValue()) {
                        String flowDesignBody = designBodyMap.get(design.getId());
                        if (ObjectNull.isNull(flowDesignBody)) {
                            continue;
                        }
                        // 获取人工节点集合
                        List<Node> manualNodes = getManualNodes(flowDesignBody);
                        design.setDesign(null);
                        PermissionDesignDto permissionDesignDto = BeanCopyUtil.copy(design, PermissionDesignDto.class);
                        permissionDesignDto.setManualNodes(manualNodes);
                        // 启用了动态增加流程节点 && 不存在人工节点集合，则可以在发起流程时动态增加审批节点
                        permissionDesignDto.setCanDynamicAddNode(design.getExtend().getEnableDynamicNode() && CollectionUtils.isEmpty(manualNodes));
                        permissionDesignDtos.add(permissionDesignDto);
                    }
                    havePermissionDesignResDto.setFlowDesigns(permissionDesignDtos);
                    return havePermissionDesignResDto;
                })
                .collect(Collectors.toList());
        return designs;
    }

    /**
     * 获取人工节点集合
     *
     * @param flowDesign 流程设计
     * @return 人工节点集合
     */
    @Override
    public List<Node> getManualNodes(String flowDesign) {
        // 解析工作流设计json，获取人工节点集合
        List<Node> manualNodes = FlowUtil.getManualNodes(flowDesign);
        if (CollectionUtils.isEmpty(manualNodes)) {
            return Collections.emptyList();
        }
        List<Node> manualNodeList = new ArrayList<>();
        // 封装要返回的数据
        manualNodes.forEach(n -> {
            Node node = new Node();
            node.setId(n.getId());
            node.setName(n.getName());
            NodeProperties nodeProperties = new NodeProperties();
            nodeProperties.setTargetObj(n.getProps().getTargetObj());
            nodeProperties.setType(n.getProps().getType());
            nodeProperties.setPersonnelScope(FlowUtil.getPersonnelScope(n));
            node.setProps(nodeProperties);
            manualNodeList.add(node);
        });
        return manualNodeList;
    }

    @Override
    public List<FlowDesign> getByAppId(String appId) {
        return list(Wrappers.<FlowDesign>lambdaQuery().eq(ObjectNull.isNotNull(appId), FlowDesign::getJvsAppId, appId)
                .orderByDesc(FlowDesign::getCreateTime));
    }

    @Override
    public List<FlowDesignDto> getByAppId(String appId, String[] nodeTypeValues) {
        List<FlowDesign> flowDesigns = getByAppId(appId);
        if (CollectionUtils.isEmpty(flowDesigns)) {
            return (Collections.emptyList());
        }

        // 封装设计节点
        List<NodeTypeEnum> nodeTypeEnums = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(nodeTypeValues)) {
            nodeTypeEnums = Arrays.stream(nodeTypeValues).map(NodeTypeEnum::getByValue).collect(Collectors.toList());
        }
        Map<String, String> designBodyMap = flowDesignVersionService.getBatchDesignBody(flowDesigns, FlowDesignVersionStatusEnum.DESIGNING);
        List<FlowDesignDto> flowDesignDtoList = new ArrayList<>();
        for (FlowDesign flowDesign : flowDesigns) {
            FlowDesignDto flowDesignDto = BeanCopyUtil.copy(flowDesign, FlowDesignDto.class);
            flowDesignDto.setNodes(getNodesByDesign(designBodyMap.get(flowDesign.getId()), nodeTypeEnums));
            flowDesignDtoList.add(flowDesignDto);
        }
        return flowDesignDtoList;
    }

    @Override
    public List<FlowDesignNodeDto> getNodesById(String id, List<NodeTypeEnum> nodeTypeEnums) {
        FlowDesign flowDesign = getById(id);
        String designBody = getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.DESIGNING).getDesignBody();
        return getNodesByDesign(designBody, nodeTypeEnums);
    }

    @Override
    public List<FlowDesignNodeDto> getNodesByDesign(String flowDesign, List<NodeTypeEnum> nodeTypeEnums) {
        if (StringUtils.isBlank(flowDesign)) {
            return Collections.emptyList();
        }
        List<Node> nodes = FlowUtil.getOrderNodes(flowDesign, nodeTypeEnums);
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream().map(node -> {
            Node currentNode = FlowUtil.findNode(node.getId());
            FlowDesignNodeDto dto = BeanCopyUtil.copy(currentNode, FlowDesignNodeDto.class);
            if (NodeTypeEnum.SP.equals(dto.getType())) {
                dto.setApproverType(currentNode.getProps().getType());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getRuleFlows() {
        return list(new LambdaQueryWrapper<FlowDesign>().select(FlowDesign::getId, FlowDesign::getRuleKeys).isNotNull(FlowDesign::getRuleKeys))
                .stream()
                .collect(Collectors.toMap(FlowDesign::getId, FlowDesign::getRuleKeys));
    }

    @Override
    public FlowExtendDto getFlowExtend(String designId) {
        return Optional.ofNullable(getOne(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getId, designId)
                .select(FlowDesign::getExtend))).orElse(new FlowDesign().setExtend(new FlowExtendDto())).getExtend();
    }

    @Override
    public FlowDesignVersion getDesignVersion(FlowDesign flowDesign, FlowDesignVersionStatusEnum versionStatus) {
        // 优先获取流程设计版本
        FlowDesignVersion flowDesignVersion = flowDesignVersionService.getVersion(flowDesign.getId(), versionStatus);
        if (ObjectNull.isNotNull(flowDesignVersion)) {
            return flowDesignVersion;
        }
        String design = null;
        // 兼容旧数据，从流程设计表中获取设计
        if (FlowDesignVersionStatusEnum.USE.equals(versionStatus)) {
            design = flowDesign.getDesign();
        } else {
            design = flowDesign.getDesigning();
        }
        return new FlowDesignVersion().setDesignBody(design);
    }

    @Override
    public JSONObject copyNode(JSONObject nodeObj) {
        Node node = null;
        if (NodeTypeEnum.TJ.getValue().equals(nodeObj.get(Get.name(Node::getType)))) {
            node = BeanCopyUtil.copy(nodeObj, Condition.class);
        } else {
            node = BeanCopyUtil.copy(nodeObj, Node.class);
        }
        String parentId = node.getPid();
        replaceId(node, parentId, null);
        return JSON.parseObject(JSON.toJSONString(node));
    }

    /**
     * 替换id
     *
     * @param node         流程节点设计
     * @param parentId     上级节点id
     * @param parallelFlag 所属并行节点id
     */
    private <T extends Node> void replaceId(T node, String parentId, String parallelFlag) {
        if (ObjectNull.isNull(node)) {
            return;
        }
        String newId = IdWorker.getIdStr();
        if (ObjectNull.isNotNull(node.getId())) {
            node.setPid(parentId);
            node.setId(newId);
            node.setParallelFlag(parallelFlag);
        }
        if (NodeTypeEnum.TJ.equals(node.getType())) {
            // 公式id映射关系集合，用以复制公式  Map<已有公式id，新公式id>
            Map<String, String> functionBusinessIdMap = new HashMap<>();
            Condition conditionNode = (Condition) node;
            conditionNode.getGroups().forEach(group -> {
                // id有后缀为_func的就是没有公式，不用替换为公式id，否则替换为新的公式id
                group.getCondition().forEach(condition -> {
                    if (ConditionPropertiesTypeEnum.FUN.equals(condition.getType())) {
                        if (condition.getId().endsWith("_func")) {
                            String conditionId = IdWorker.getIdStr() + "_func";
                            List<String> cids = group.getCids().stream()
                                    .map(cid -> {
                                        if (cid.equals(condition.getId())) {
                                            cid = conditionId;
                                        }
                                        return cid;
                                    })
                                    .collect(Collectors.toList());
                            group.setCids(cids);
                            condition.setId(conditionId);
                        } else {
                            // 已有公式，则创建公式，并替换公式id
                            String newFunctionId = IdGenerator.getIdStr();
                            functionBusinessIdMap.put(condition.getId(), newFunctionId);
                            condition.setId(newFunctionId);
                        }
                    }
                });
            });
            // 复制公式
            if (!functionBusinessIdMap.isEmpty()) {
                List<FunctionBusinessPo> functionBusinessList = functionBusinessMapper.selectBatchIds(functionBusinessIdMap.keySet());
                if (ObjectNull.isNull(functionBusinessList)) {
                    return;
                }
                functionBusinessList.forEach(fun -> {
                    fun.setId(functionBusinessIdMap.get(fun.getId()));
                    functionBusinessMapper.insert(fun);
                });
            }
        }

        // 继续遍历后续节点
        Node nextNode = node.getNode();
        if (nextNode != null) {
            if (NodeTypeEnum.CONDITION.equals(node.getType())) {
                node.getConditions().forEach(n ->
                        replaceId(n, node.getId(), node.getParallelFlag())
                );
            }
            if (NodeTypeEnum.PARALLEL.equals(node.getType())) {
                node.getParallels().forEach(n ->
                        replaceId(n, node.getId(), node.getId())
                );
            }
            replaceId(node.getNode(), node.getId(), node.getParallelFlag());
        }
    }
}
