package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.workflow.entity.*;
import cn.bctools.design.workflow.service.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhuxiaokang
 * 工作流设计套件实现
 */
@Service
@AllArgsConstructor
public class FlowJvsDesigner implements IJvsDesigner {

    private final MapperMethodHandler mapperMethodHandler;
    private final FlowDesignService flowDesignService;
    private final FlowDesignVersionService flowDesignVersionService;
    private final FlowPurviewService flowPurviewService;
    private final FlowQuickReplyService flowQuickReplyService;
    private final FlowTaskService flowTaskService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final FlowTaskParallelService flowTaskParallelService;
    private final FlowTaskPathService flowTaskPathService;


    @Override
    public void delete(String appId, String designId) {
        flowDesignService.remove(Wrappers.<FlowDesign>lambdaQuery().eq(FlowDesign::getJvsAppId, appId).eq(FlowDesign::getId, designId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(flowDesignService, Wrappers.<FlowDesign>lambdaQuery().eq(FlowDesign::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskService, Wrappers.<FlowTask>lambdaQuery().eq(FlowTask::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowDesignVersionService, Wrappers.<FlowDesignVersion>lambdaQuery().eq(FlowDesignVersion::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowPurviewService, Wrappers.<FlowPurview>lambdaQuery().eq(FlowPurview::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowQuickReplyService, Wrappers.<FlowQuickReply>lambdaQuery().eq(FlowQuickReply::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskApprovalRecordService, Wrappers.<FlowTaskApprovalRecord>lambdaQuery().eq(FlowTaskApprovalRecord::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskCarbonCopyService, Wrappers.<FlowTaskCopied>lambdaQuery().eq(FlowTaskCopied::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskNodeService, Wrappers.<FlowTaskNode>lambdaQuery().eq(FlowTaskNode::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskParallelService, Wrappers.<FlowTaskParallel>lambdaQuery().eq(FlowTaskParallel::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskPathService, Wrappers.<FlowTaskPath>lambdaQuery().eq(FlowTaskPath::getJvsAppId, appId));
        mapperMethodHandler.deletePhysical(flowTaskPersonService, Wrappers.<FlowTaskPerson>lambdaQuery().eq(FlowTaskPerson::getJvsAppId, appId));
    }
}
