package cn.bctools.design.rule.component;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.rule.entity.TaskCronDto;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.xxl.job.api.XxlAdminApi;
import cn.bctools.xxl.job.api.XxlJobInfoDto;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;

/**
 * @author guojing
 */
@Slf4j
@Component
public class XxlJobComponent {

    @Resource
    XxlAdminApi xxlAdminApi;

    @Value("${xxl.job.accessToken:qNAMzjEUPoqjaOBgaGMUWQUud2GNoqW7}")
    String token = "qNAMzjEUPoqjaOBgaGMUWQUud2GNoqW7";
    @Value("${xxl.job.cron.time:1800}")
    Integer time = 1800;

    /**
     * 根据设计创建定时任务
     *
     * @param task     任务对象
     * @param onTask   是否开启
     * @param appName  应用名称
     * @param ruleName 逻辑名称
     * @param secret
     */
    public void saveOrUpdateJob(TaskCronDto task, boolean onTask, String appName, String ruleName, String secret) {
        if (ObjectNull.isNull(task)) {
            return;
        }
        if (ObjectNull.isNull(task.getCron())) {
            return;
        }
        XxlJobInfoDto xxlJobInfoDto = BeanCopyUtil.copy(task, XxlJobInfoDto.class);
        xxlJobInfoDto.setAuthor(ObjectNull.isNotNull(task.getAuthor()) ? task.getAuthor() : "admin");
        xxlJobInfoDto.setAlarmEmail(ObjectNull.isNotNull(task.getAlarmEmail()) ? task.getAlarmEmail() : "test@bctools.com");
        xxlJobInfoDto.setExecutorHandler(RuleConstant.DEMO_JOB_HANDLER);
        for (CronEnum value : CronEnum.values()) {
            if (task.getCron().equals(value.toString())) {
                task.setCron(value.getCorn());
            }
        }
        //验证corn 表达式是否对不对
        if (!CronExpression.isValidExpression(task.getCron())) {
            throw new BusinessException("corn表达式不正确", task.getCron());
        }
        checkCorn(task.getCron());
        xxlJobInfoDto.setScheduleConf(task.getCron());
        String jobName = "%s 应用 %s 逻辑，逻辑key %s";
        jobName = String.format(jobName, appName, ruleName, secret);
        xxlJobInfoDto.setJobDesc(jobName);
        //远程调用的KEY,与定时任务调用处理的地方相同
        Dict dict = Dict.create().set("key", secret).set("startTime", task.getStartTime());
        xxlJobInfoDto.setExecutorParam(JSONObject.toJSONString(dict));
        xxlJobInfoDto.setTriggerStatus(onTask ? 1 : 0);
        log.info("执行定时任务的参数为,{}", JSONObject.toJSONString(xxlJobInfoDto));
        //启动定时任务
        Integer integerR = null;
        String errorMsg = "启动定时任务失败";
        try {
            R r = xxlAdminApi.saveOrUpdate(xxlJobInfoDto, token, SpringContextUtil.getApplicationContextName());
            if (r.is()) {
                integerR = Integer.valueOf(r.getData().toString());
            } else {
                throw new BusinessException(errorMsg, r.getMsg());
            }
        } catch (Exception exception) {
            log.error(errorMsg, exception);
            throw new BusinessException(errorMsg);
        }
        log.info("定时任务返回定时ID为:{}", integerR);
        ///回填写定时任务的ID值,用做更新和修改操作
        task.setId(integerR);
    }

    /**
     * 批量删除定时任务
     *
     * @param ids 任务id集合
     */
    public void deleteBatch(Collection<Integer> ids) {
        if (ObjectNull.isNull(ids)) {
            return;
        }
        ids.forEach(id -> {
            try {
                xxlAdminApi.delete(id, token);
            } catch (Exception e) {
                log.error("删除定时任务失败", e);
                throw new BusinessException("删除定时任务失败");
            }
        });
    }

    /**
     * 批量停止定时任务
     *
     * @param ids 任务id集合
     */
    public void stopBatch(Collection<Integer> ids) {
        if (ObjectNull.isNull(ids)) {
            return;
        }
        ids.forEach(id -> {
            try {
                xxlAdminApi.stop(id, token);
            } catch (Exception e) {
                log.error("停止定时任务失败 id: {}, token: {}", id, token, e);
                throw new BusinessException("停止定时任务失败");
            }
        });
    }

    @SneakyThrows
    public void checkCorn(String cron) {
        // 定义一个 Cron 表达式
        CronExpression cronExpression = new CronExpression(cron);
        //导包
        Date date = cronExpression.getTimeAfter(new Date());
        Date date1 = cronExpression.getTimeAfter(date);
        long between = DateUtil.between(date, date1, DateUnit.SECOND);
        //判断是否超过多少秒
        if (between <= time) {
            throw new BusinessException("定时任务周期太短,请联系管理员开放" + ",最低" + time + "秒");
        }
    }

}
