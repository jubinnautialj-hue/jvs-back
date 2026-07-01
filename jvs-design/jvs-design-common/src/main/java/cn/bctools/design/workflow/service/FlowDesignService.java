package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.dto.permission.HavePermissionDesignResDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 工作流设计 服务类
 */
public interface FlowDesignService extends IService<FlowDesign> {

    /**
     * 快捷创建工作流设计
     *
     * @param dto 快捷创建工作流入参
     * @return 工作流设计id
     */
    FlowDesign quickCreation(QuickCreationFlowDesignReqDto dto);

    /**
     * 发布工作流设计
     *
     * @param id    工作流id
     * @param appId 应用id
     */
    void published(String id, String appId);

    /**
     * 停用工作流设计
     *
     * @param id    工作流id
     * @param appId 应用id
     */
    void stop(String id, String appId);

    /**
     * 修改设计基础配置
     *
     * @param dto 修改工作流入参
     */
    void update(UpdateFlowDesignReqDto dto);

    /**
     * 删除工作流设计
     *
     * @param id    工作流id
     * @param appId 应用id
     * @return 删除的流程设计
     */
    FlowDesign delete(String id, String appId);

    /**
     * 保存设计
     *
     * @param dto 保存工作流设计入参
     * @return 设计json
     */
    FlowDesign saveDesign(SaveFlowReqDesign dto);

    /**
     * 获取可以启动的工作流集合
     *
     * @param userDto 登录用户
     * @param name    工作流名称
     * @return 工作流集合
     */
    List<HavePermissionDesignResDto> havePermissionDesign(UserDto userDto, String name);

    /**
     * 获取应用下所有工作流设计
     *
     * @param appId 应用id
     * @return 工作流设计集合
     */
    List<FlowDesign> getByAppId(String appId);

    /**
     * 得到应用下指定节点类型的工作流设计
     *
     * @param appId          应用id
     * @param nodeTypeValues 工作流节点类型
     * @return 工作流设计
     */
    List<FlowDesignDto> getByAppId(String appId, String[] nodeTypeValues);

    /**
     * 获取工作流设计节点
     *
     * @param id            工作流id
     * @param nodeTypeEnums 节点类型（筛选指定类型节点）
     * @return 工作流设计节点集合（返回节点部分配置）
     */
    List<FlowDesignNodeDto> getNodesById(String id, List<NodeTypeEnum> nodeTypeEnums);

    /**
     * 获取工作流设计节点
     *
     * @param flowDesign    工作流设计
     * @param nodeTypeEnums 节点类型（筛选指定类型节点）
     * @return 工作流设计节点集合（返回节点部分配置）
     */
    List<FlowDesignNodeDto> getNodesByDesign(String flowDesign, List<NodeTypeEnum> nodeTypeEnums);

    /**
     * 获取所有工作流下面的逻辑引擎引用关系
     *
     * @return 工作流与逻辑引擎引用关系. Map<为工作流id, 逻辑引擎ids>
     */
    Map<String, List<String>> getRuleFlows();

    /**
     * 获取人工节点集合
     *
     * @param flowDesign 设计
     * @return 人工节点集合
     */
    List<Node> getManualNodes(String flowDesign);

    /**
     * 获取工作流设计扩展设置
     *
     * @param designId 工作流设计id
     * @return 流程扩展设置
     */
    FlowExtendDto getFlowExtend(String designId);

    /**
     * 获取工作流设计版本
     *
     * @param flowDesign    工作流设计
     * @param versionStatus 工作流版本状态
     * @return 工作流设计版本
     */
    FlowDesignVersion getDesignVersion(FlowDesign flowDesign, FlowDesignVersionStatusEnum versionStatus);

    /**
     * 复制节点
     *
     * @param node 待复制的节点
     * @return 复制后的节点
     */
    JSONObject copyNode(JSONObject node);
}
