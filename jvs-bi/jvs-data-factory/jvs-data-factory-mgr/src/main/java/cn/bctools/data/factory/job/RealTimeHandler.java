package cn.bctools.data.factory.job;

import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelJobInfoReturnDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.enums.JobInfoStatusEnums;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelApiService;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.RealTimeLog;
import cn.bctools.data.factory.source.enums.StateEnums;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.RealTimeLogService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author guojing
 */
@Component
@AllArgsConstructor
@Slf4j
public class RealTimeHandler {
    private final RealTimeLogService realTimeLogService;
    private final SeaTunnelApiService seaTunnelApiService;
    private final DataSourceStructureService dataSourceStructureService;
    private final DorisJdbcTemplate dorisJdbcTemplate;

    @XxlJob("real_time_task_check")
    public void execute() {
        //清空线程数据
        SystemThreadLocal.clear();
        TenantContextHolder.clear();
        //获取需要验证的数据
        List<RealTimeLog> list = realTimeLogService.list(new LambdaQueryWrapper<RealTimeLog>().eq(RealTimeLog::getTaskStatus, StateEnums.succeed));
        XxlJobHelper.log("本次验证的数据为:{}", JSONObject.toJSONString(list));
        //数据验证
        if (!list.isEmpty()) {
            list.forEach(e -> {
                //修改验证时间
                e.setCheckTime(LocalDateTime.now());
                realTimeLogService.updateById(e);
                SeaTunnelJobInfoReturnDto jobInfo;
                try {
                    jobInfo = seaTunnelApiService.getJobInfo(e.getSeaTunnelId());
                }catch (Exception exception){
                    jobInfo=new SeaTunnelJobInfoReturnDto()
                            .setErrorMsg(exception.getMessage())
                            .setJobStatus(JobInfoStatusEnums.CANCEL);
                    log.info("获取seatunnel任务错误",exception);
                    XxlJobHelper.log("获取seaTunnel任务错误",exception);
                }
                JobInfoStatusEnums jobInfoStatusEnums = Optional.ofNullable(jobInfo.getJobStatus()).orElse(JobInfoStatusEnums.CANCEL);
                if (!jobInfoStatusEnums.equals(JobInfoStatusEnums.RUNNING)) {
                    e.setTaskStatus(StateEnums.fail);
                    e.setErrorMsg(jobInfo.getErrorMsg());
                    realTimeLogService.updateById(e);
                    //修改表结构数据
                    dataSourceStructureService.update(new UpdateWrapper<DataSourceStructure>().lambda()
                            .eq(DataSourceStructure::getId, e.getDataStructureId())
                            .set(DataSourceStructure::getRealTimeIsOpen, Boolean.FALSE));
                    //删除表数据
                    dorisJdbcTemplate.dropForce(e.getOdsTableName());
                    XxlJobHelper.log("不通过的数据为:{}", JSONObject.toJSONString(e));
                }

            });
        }

    }
}
