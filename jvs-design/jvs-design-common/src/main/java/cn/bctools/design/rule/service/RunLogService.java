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
     * 注意：此方法已在RuleStartUtils的线程池中异步调用，不需要@Async注解
     * 移除@Async避免双重异步导致的线程爆炸和连接池耗尽问题
     *
     * @param runLogBo
     */
    void saveLog(RunLogPo runLogBo);

}
