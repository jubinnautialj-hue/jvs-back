package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.service.FlowTaskApprovalRecordService;
import cn.bctools.design.workflow.service.FlowTaskCarbonCopyService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[workflow]工作流表单数据")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow")
public class FlowTaskBaseController {

    private final DynamicDataService dynamicDataService;
    private final FlowTaskService flowTaskService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final FormService formService;

    @Log
    @ApiOperation("忽略数据权限查询查询单条数据")
    @GetMapping("/data/query/single/{taskId}/{modelId}/{dataId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> querySingle(@PathVariable("taskId") String taskId,
                                              @PathVariable("modelId") String dataModelId,
                                              @PathVariable("dataId") String dataId) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        // “抄送我的”、“审核记录”，查询数据需要跳过数据模型的权限校验，否则查不到数据
        boolean existsApprove = flowTaskApprovalRecordService.existsTaskApprove(userDto, taskId);
        if (Boolean.FALSE.equals(existsApprove)) {
            if (Boolean.FALSE.equals(flowTaskCarbonCopyService.exists(userDto, taskId))) {
                // 当前用户既不是目标任务的抄送人，也未审批过目标任务，则查询失败
                log.error("当前用户既不是目标任务的抄送人，也未审批过目标任务");
                throw new BusinessException("当前用户既不是目标任务的抄送人也未审批过目标任务");
            }
        }
        // 若目标任务的数据模型id，与入参数据模型id不同，则查询失败
        FlowTask flowTask = flowTaskService.getById(taskId);
        if (Boolean.FALSE.equals(dataModelId.equals(flowTask.getDataModelId()))) {
            log.error("任务的数据模型，与查询条件数据模型不同");
            throw new BusinessException("任务的数据模型与查询条件数据模型不同");
        }
        return R.ok(dynamicDataService.querySingle(flowTask.getJvsAppId(), dataModelId, dataId));
    }

    @Log
    @GetMapping("/{appId}/form/{id}")
    @ApiOperation("忽略权限查询表单设计")
    @Transactional(rollbackFor = Exception.class)
    public R<FormPo> form(@PathVariable("id") String id, @PathVariable String appId) {
        FormPo po = formService.getById(id);
        if (Objects.isNull(po)) {
            return R.ok();
        }
        if (!po.getJvsAppId().equals(appId)) {
            return R.ok();
        }
        return R.ok(formService.getFormDetail(po, appId, id));
    }

}
