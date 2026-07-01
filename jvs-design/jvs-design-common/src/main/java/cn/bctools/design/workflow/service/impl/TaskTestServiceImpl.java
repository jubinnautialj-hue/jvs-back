package cn.bctools.design.workflow.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.design.workflow.dto.DataDto;
import cn.bctools.design.workflow.dto.SaveFlowReqDesign;
import cn.bctools.design.workflow.dto.testflow.FlowTestPrepareDto;
import cn.bctools.design.workflow.dto.testflow.FlowTestReqDto;
import cn.bctools.design.workflow.dto.testflow.StartFlowTestDto;
import cn.bctools.design.workflow.dto.testflow.StartFlowTestResDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.TaskTestService;
import cn.bctools.design.workflow.support.RuntimeService;
import cn.bctools.design.workflow.support.StartTask;
import cn.bctools.design.workflow.support.valid.ValidatedFlowDesign;
import cn.bctools.design.workflow.utils.FlowUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 工作流测试
 */
@Service
@AllArgsConstructor
public class TaskTestServiceImpl implements TaskTestService {

    private final FlowDesignService flowDesignService;
    private final FlowDynamicDataService flowDynamicDataService;
    private final FlowTaskService flowTaskService;
    private final RuntimeService runtimeService;
    private final TaskServiceImpl taskService;
    private final AuthUserServiceApi authUserServiceApi;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTestPrepareDto prepare(SaveFlowReqDesign dto) {
        // 保存工作流设计
        flowDesignService.saveDesign(dto);
        FlowDesign flowDesign = flowDesignService.getById(dto.getId());
        // 校验是否可发起测试
        String design = flowDesignService.getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.DESIGNING).getDesignBody();
        ValidatedFlowDesign.valid(design, flowDesign.getExtend());
        // 构造响应
        String designBody = JSON.toJSONString(dto.getDesignBody());
        return BeanCopyUtil.copy(flowDesign, FlowTestPrepareDto.class)
                .setManualNodes(flowDesignService.getManualNodes(designBody))
                .setDesignBody(designBody);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StartFlowTestResDto startTask(StartFlowTestDto dto) {
        FlowDesign flowDesign = Optional.ofNullable(flowDesignService.getOne(Wrappers.<FlowDesign>lambdaQuery()
                .eq(FlowDesign::getId, dto.getId()))
        ).orElseThrow(() -> new BusinessException("工作流不存在"));
        // 发起人自选审核人修改配置
        String designBody = flowDesignService.getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.DESIGNING).getDesignBody();
        String design = FlowUtil.setSelfSelectApprover(true, designBody, flowDesign.getExtend(), dto.getApprovers());

        // 保存数据
        String dataModelId = flowDesign.getDataModelId();
        flowDesign.setDataModelId(dataModelId);
        UserDto userDto = getUserDto(dto.getUserId());

        // 设置为测试数据
        JSONObject data = Optional.ofNullable(dto.getData()).orElse(new JSONObject());
        data.put(FlowDataFieldEnum.JVS_MODEL_TEST.getFieldKey(), Boolean.TRUE);
        DataDto dataDto = flowDynamicDataService.saveModelData(flowDesign.getJvsAppId(), flowDesign.getDataModelId(), data);
        String dataId = dataDto.getDataId();

        // 保存测试工作流任务
        FlowTask flowTask = flowTaskService.buildSaveFlowTask(null, flowDesign, design, dataId, null);
        flowTask.setCreateById(userDto.getId());
        flowTask.setCreateBy(userDto.getRealName());
        flowTask.setUpdateBy(userDto.getId());
        flowTask.setTest(Boolean.TRUE);
        flowTask.setDesignBody(design);
        // 执行流程
        StartTask startTask = new StartTask();
        startTask.setUser(userDto);
        startTask.setFlowTask(flowTask);
        startTask.setData(dataDto.getData());
        startTask.setDataVersion(dataDto.getVersion());
        runtimeService.start(startTask);
        return new StartFlowTestResDto().setTaskId(flowTask.getId()).setDataId(flowTask.getDataId()).setFlowManualNodes(flowTask.getFlowManualNodes());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute(FlowTestReqDto dto) {
        taskService.execute(dto, getUserDto(dto.getUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StartFlowTestResDto restartTask(StartFlowTestDto dto) {
        return startTask(dto);
    }

    /**
     * 查询用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    private UserDto getUserDto(String userId) {
        SearchUserDto search = new SearchUserDto();
        search.setUserIds(Collections.singletonList(userId));
        return Optional.ofNullable(authUserServiceApi.userSearch(search).getData().get(0)).orElseThrow(() -> new BusinessException("user_does_not_exist"));
    }
}
