package cn.bctools.data.factory.job;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.data.factory.entity.SysSetting;
import cn.bctools.data.factory.entity.TaskCronDto;
import cn.bctools.data.factory.entity.enums.CronEnum;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.SysSettingDataTypeEnums;
import cn.bctools.data.factory.po.XxJobDtaFactoryPo;
import cn.bctools.data.factory.service.SysSettingService;
import cn.bctools.xxl.job.api.XxlAdminApi;
import cn.bctools.xxl.job.api.XxlJobInfoDto;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author guojing
 */
@Slf4j
@Component
public class XxlJobComponent {


    @Resource
    XxlAdminApi xxlAdminApi;

    @Autowired
    SysSettingService sysSettingService;

    @Value("${xxl.job.accessToken:qNAMzjEUPoqjaOBgaGMUWQUud2GNoqW7}")
    String token = "qNAMzjEUPoqjaOBgaGMUWQUud2GNoqW7";

    @SneakyThrows
    void addConsanguinityJob() {
        //判断血缘视图同步的定时任务是否存在 如果不存在就新增一个定时任务
        SysSetting one = sysSettingService.getOne(new LambdaQueryWrapper<SysSetting>().eq(SysSetting::getDataType, SysSettingDataTypeEnums.consanguinityAnalyseJob));
        if (one == null) {
            XxlJobInfoDto xxlJobInfoDto = new XxlJobInfoDto();
            xxlJobInfoDto.setExecutorHandler("jvs-data-factory-mgr-consanguinity");
            xxlJobInfoDto.setScheduleConf("0 0 0 * * ?");
            xxlJobInfoDto.setJobDesc("血缘视图");
            xxlJobInfoDto.setExecutorParam("");
            xxlJobInfoDto.setAuthor("admin");
            Integer jobId = null;
            try {
                jobId = xxlAdminApi.save(xxlJobInfoDto, token, SpringContextUtil.getApplicationContextName()).getData();
            } catch (Exception exception) {
                log.info("添加血缘视图定时任务失败，停止程序运行，请查看xxjob服务是否启动成功，错误----", exception);
                System.exit(1);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jobId", jobId);
            one = new SysSetting()
                    .setDataJson(jsonObject)
                    .setExplainTxt(SysSettingDataTypeEnums.consanguinityAnalyseJob.getDesc())
                    .setDataType(SysSettingDataTypeEnums.consanguinityAnalyseJob);
            sysSettingService.save(one);
        }
    }

    @SneakyThrows
    void addCheckSeaTunnelTaskJob() {
        //判断血缘视图同步的定时任务是否存在 如果不存在就新增一个定时任务
        SysSetting one = sysSettingService.getOne(new LambdaQueryWrapper<SysSetting>().eq(SysSetting::getDataType, SysSettingDataTypeEnums.checkSeaTunnelTaskJob));
        if (one == null) {
            XxlJobInfoDto xxlJobInfoDto = new XxlJobInfoDto();
            xxlJobInfoDto.setExecutorHandler("real_time_task_check");
            xxlJobInfoDto.setScheduleConf("0 0/10 * * * ? ");
            xxlJobInfoDto.setJobDesc("seaTunnel实时抽取任务校验");
            xxlJobInfoDto.setExecutorParam("");
            xxlJobInfoDto.setAuthor("admin");
            Integer jobId = null;
            try {
                jobId = xxlAdminApi.save(xxlJobInfoDto, token, SpringContextUtil.getApplicationContextName()).getData();
            } catch (Exception exception) {
                log.info("添加血缘视图定时任务失败，停止程序运行，请查看xxjob服务是否启动成功，错误----", exception);
                System.exit(1);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jobId", jobId);
            one = new SysSetting()
                    .setDataJson(jsonObject)
                    .setExplainTxt(SysSettingDataTypeEnums.checkSeaTunnelTaskJob.getDesc())
                    .setDataType(SysSettingDataTypeEnums.checkSeaTunnelTaskJob);
            sysSettingService.save(one);
        }
    }

    @SneakyThrows
    public void stop(TaskCronDto task) {
        //如果是停止，则需要将定时任务给删除掉
        try {
            if (ObjectUtil.isNotNull(task) && ObjectUtil.isNotNull(task.getId())) {
                xxlAdminApi.stop(task.getId(), token).getData();
            }
        } catch (Exception e) {
            log.error("停止定时任务失败", e);
            throw new BusinessException("停止定时任务失败");
        }
    }

    @SneakyThrows
    public void remove(TaskCronDto task) {
        //如果是停止，则需要将定时任务给删除掉
        try {
            if (ObjectUtil.isNotNull(task) && ObjectUtil.isNotNull(task.getId())) {
                xxlAdminApi.delete(task.getId(), token).getData();
            }
        } catch (Exception e) {
            log.error("删除定时任务失败", e);
            throw new BusinessException("删除定时任务失败");
        }
    }
    /**
     * 根据设计创建定时任务
     *
     * @param task   任务对象
     * @param onTask 是否开启
     * @param name   数据集名称
     * @param id     id
     */
    public void saveOrUpdateJob(TaskCronDto task, Boolean onTask, String name, String id, TaskTypeEnum taskType) {
        int triggerStatus = 0;
        if (ObjectNull.isNull(task)) {
            return;
        }
        if (ObjectNull.isNull(task.getCron())) {
            return;
        }
        String executorHandler;
        switch (taskType) {
            case ordinary:
                executorHandler="jvs-data-factory-mgr";
                break;
            case mqtt:
                executorHandler="jvs-data-factory-mgr-mqtt";
                break;
            case api:
                executorHandler="jvs-data-factory-mgr-api";
                break;
            default:
                throw new BusinessException("此类型不支持生成定时任务");
        }
        XxlJobInfoDto xxlJobInfoDto = BeanCopyUtil.copy(task, XxlJobInfoDto.class);
        xxlJobInfoDto.setAuthor(ObjectNull.isNotNull(task.getAuthor()) ? task.getAuthor() : "admin");
        xxlJobInfoDto.setAlarmEmail(ObjectNull.isNotNull(task.getAlarmEmail()) ? task.getAlarmEmail() : "test@test.com");
        xxlJobInfoDto.setExecutorHandler(executorHandler);
        for (CronEnum value : CronEnum.values()) {
            if (task.getCron().equals(value.toString())) {
                task.setCron(value.getCorn());
            }
        }
        try {
            task.setCron(CronEnum.valueOf(task.getCron()).getCorn());
        } catch (Exception exception) {
            log.warn("用户手动输入的cron表达式");
        }
        //验证corn 表达式是否对不对
        if (!CronExpression.isValidExpression(task.getCron())) {
            throw new BusinessException("corn表达式不正确", task.getCron());
        }
        xxlJobInfoDto.setScheduleConf(task.getCron());
        String jobName = "数据集: %s";
        jobName = String.format(jobName, name);
        xxlJobInfoDto.setJobDesc(jobName);
        //组装数据
        XxJobDtaFactoryPo xxJobDtaFactoryPo = new XxJobDtaFactoryPo()
                .setId(id)
                .setTenantId(TenantContextHolder.getTenantId());
        xxlJobInfoDto.setExecutorParam(JSONObject.toJSONString(xxJobDtaFactoryPo));
        if (onTask && task.getOnTask()) {
            triggerStatus = 1;
        }
        xxlJobInfoDto.setTriggerStatus(triggerStatus);
        log.info("执行定时任务的参数为,{}", JSONObject.toJSONString(xxlJobInfoDto));
        //启动定时任务
        Integer integerR;
        try {
            R r = xxlAdminApi.saveOrUpdate(xxlJobInfoDto, token, SpringContextUtil.getApplicationContextName());
            if (r.is()) {
                integerR = Integer.valueOf(r.getData().toString());
            } else {
                throw new BusinessException("启动定时任务失败", r.getMsg());
            }
        } catch (Exception exception) {
            log.error("启动定时任务失败", exception);
            throw new BusinessException("启动定时任务失败");
        }
        log.info("定时任务返回定时ID为:{}", integerR);
        ///回填写定时任务的ID值,用做更新和修改操作
        task.setId(integerR);
    }


}
