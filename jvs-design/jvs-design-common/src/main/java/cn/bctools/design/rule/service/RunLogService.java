package cn.bctools.design.rule.service;

import cn.bctools.design.rule.entity.RuleType;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.rule.entity.enums.RunType;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

/**
 * @author guojing
 */
public interface RunLogService extends IService<RunLogPo> {

    /**
     * 根据参数创建逻辑执行日志对象
     *
     * @param jvsApp     应用id
     * @param runKey     运行的key
     * @param runType    运行的类型
     * @param parameters 具体的参数
     * @param reqType
     * @param type
     * @param sync       是否异步
     * @return
     */
    RunLogPo create(String jvsApp, String runKey, RunType runType, Map<String, Object> parameters, RuleType reqType, RuleType type, boolean sync);

    /**
     * 保存运行日志
     *
     * @param runLogBo
     */
    @Async
    void saveLog(RunLogPo runLogBo);

}
