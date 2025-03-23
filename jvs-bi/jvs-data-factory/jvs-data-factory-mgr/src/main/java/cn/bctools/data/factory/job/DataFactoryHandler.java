package cn.bctools.data.factory.job;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import cn.bctools.data.factory.po.XxJobDtaFactoryPo;
import cn.bctools.data.factory.service.JvsDataFactoryLogService;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author guojing
 */
@Component
@AllArgsConstructor
@Slf4j
public class DataFactoryHandler {

    private final JvsDataFactoryService jvsDataFactoryService;
    private final JvsDataFactoryQueueService jvsDataFactoryQueueService;
    private final ConsanguinityViewService consanguinityViewService;

    @XxlJob("jvs-data-factory-mgr")
    public void execute() {
        //清空线程数据
        SystemThreadLocal.clear();
        //获取id
        XxlJobHelper.log("开始执行");
        String param = XxlJobHelper.getJobParam();
        XxJobDtaFactoryPo xxJobDtaFactoryPo = JSONObject.parseObject(param, XxJobDtaFactoryPo.class);
        //设置租户id
        xxJobDtaFactoryPo.setTenantId(xxJobDtaFactoryPo.getTenantId());
        TenantContextHolder.setTenantId(xxJobDtaFactoryPo.getTenantId());
        long id = Thread.currentThread().getId();
        XxlJobHelper.log("入参为{}", param);
        XxlJobHelper.log("当前执行的线程id为:{}", id);
        //检查队列中是否存在此任务 一个任务 在队列中只能存在一次
        JvsDataFactory byId = jvsDataFactoryService.getById(xxJobDtaFactoryPo.getId());
        if (byId == null) {
            XxlJobHelper.log("未找到此数据");
            return;
        }
        String taskExec = jvsDataFactoryQueueService.isTaskExec(byId);
        UserDto userDto = new UserDto().setId("--").setRealName(byId.getTask().getAuthor()).setTenantId(xxJobDtaFactoryPo.getTenantId());
        if (StrUtil.isNotBlank(taskExec)) {
            JvsDataFactoryLog entity = new JvsDataFactoryLog().setDataId(byId.getId());
            entity.setDelFlag(false);
            entity.setOperateMethod(OperateMethodEnum.AUTOMATIC);
            entity.setEndTime(LocalDateTime.now());
            entity.setFailureReason("定时任务触发时," + taskExec);
            entity.setOperatorId(userDto.getId());
            entity.setOperatorName(userDto.getRealName());
            entity.setExecutionResults(false);
            JvsDataFactoryLogService factoryLogService = SpringContextUtil.getBean(JvsDataFactoryLogService.class);
            factoryLogService.save(entity);
            XxlJobHelper.log(taskExec);
            return;
        }
        try {
            jvsDataFactoryService.sendQueue(byId, QueueTaskTypeEnum.PREFIX_TASK, userDto, IdGenerator.getIdStr(), OperateMethodEnum.AUTOMATIC);
        } catch (Exception exception) {
            XxlJobHelper.log("入队列失败{}", exception.getMessage());
        }
        XxlJobHelper.log("执行完成");
        TenantContextHolder.clear();
    }

    @XxlJob("jvs-data-factory-mgr-consanguinity")
    public void executeConsanguinity() {
        SystemThreadLocal.clear();
        //血缘视图同步 主要是校验数据是否存在
        String param = XxlJobHelper.getJobParam();
        JSONObject jsonObject = Optional.ofNullable(JSONObject.parseObject(param)).orElse(new JSONObject());
        String string = consanguinityViewService.syncConsanguinity(jsonObject.getString("id"));
        XxlJobHelper.log(string);
    }
}
