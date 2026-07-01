package cn.bctools.design.rule.service.impl;

import cn.bctools.design.rule.entity.RuleType;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.mapper.RunLogDao;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.rule.entity.enums.RunType;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Service
public class RunLogServiceImpl extends ServiceImpl<RunLogDao, RunLogPo> implements RunLogService {

    @Value("${app.log:false}")
    Boolean applog;

    /**
     * 保存执行日志
     *
     * @param runLogBo 保存的数据
     * @author guojing
     * @returnType 是否保存成功
     */
    @Override
    public void saveLog(RunLogPo runLogBo) {
        if (!applog) {
            return;
        }
        runLogBo.setTotalExecutionTime(Duration.between(runLogBo.getStartTime(), runLogBo.getEndTime()).toMillis());
        log.info("执行日志数据保存,数据前,{}", runLogBo);
        //日志保存为指定为逻辑执行的日志
        this.save(runLogBo);
    }


    /**
     * 创建一个日志，使用异步处理
     *
     * @param b
     * @param jvsApp
     * @param runKey     请求的逻辑Key
     * @param runType    运行的类型
     * @param parameters
     * @param reqType
     * @param type
     * @return 日志
     */
    @Override
    public RunLogPo create(String jvsApp, String runKey, RunType runType, Map<String, Object> parameters, RuleType reqType, RuleType type, boolean sync) {
        RunLogPo po = new RunLogPo();
        po.setJvsAppId(jvsApp);
        po.setReqRunKey(runKey);
        po.setStartTime(LocalDateTime.now());
        po.setStatus(true);
        po.setVariableMap(parameters);
        po.setRunType(runType);
        po.setSync(sync);
        po.setId(IdWorker.get32UUID());
        return po;
    }

}
