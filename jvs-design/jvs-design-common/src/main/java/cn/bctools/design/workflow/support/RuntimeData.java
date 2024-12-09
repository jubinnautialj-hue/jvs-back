package cn.bctools.design.workflow.support;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPath;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 运行时数据
 */

@Data
@Accessors(chain = true)
public class RuntimeData {

    /**
     * true-启动工作流，false-执行工作流
     */
    private Boolean start = Boolean.FALSE;

    /**
     * 正在处理的节点id
     */
    private String nodeId;

    /**
     * 当前处理的节点
     */
    private Node currentNode;

    /**
     * 当前节点的数据版本
     */
    private String dataVersion;

    /**
     * 用户信息
     */
    private UserDto user;

    /**
     * 工作流内容
     */
    private JSONObject data;

    /**
     * 工作流处理操作信息入参
     */
    private FlowReqDto flowDto;

    /**
     * 工作流任务信息
     */
    private FlowTask flowTask;

    /***
     * 工作流任务所有可执行路径
     */
    private List<FlowTaskPath> taskPaths;

    /**
     * 流转过程中的任务节点
     */
    private List<FlowTaskNode> flowTaskNodes;

    /**
     * 当前处理的工作流任务节点信息
     */
    private FlowTaskNode flowTaskNode;

    /**
     * 工作流高级配置
     */
    private FlowExtendDto flowExtend;

    /**
     * 动态增加的节点
     */
    private Node addNode;

    /**
     * 处理时间
     */
    private String time = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN);

    /**
     * true-可修改当前审批人处理状态，false-不修改当前审批人处理状态
     */
    private Boolean changePersonProcessStatus = Boolean.TRUE;

    /**
     * 审批结果
     * 暂存一次审批操作的执行结果
     */
    private List<ApproveResultDto> approveResults = new ArrayList<>();

    /**
     * 当前节点的审批模式(审批模式模式是与流程配置相同，但若执行了增加、移除审批人等操作，可能会导致当前审批节点的审批模式变更)
     */
    private NodePropertiesModeEnum currentNodeMode;
}
