package cn.bctools.design.rule.impl.rule.rule;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Rule(value = "逻辑应用扩展接口",
        group = RuleGroup.服务插件,
        returnType = ClassType.对象,
        order = 4,
        test = true,
//        iconUrl = "rule-jituanOAliuchengxinzengxiudingshangxianshenpiliucheng",
        explain = "提供逻辑引擎key(url  id值)直接内部相互调用, 做跨应用数据交互和接口交互操作"
)
@AllArgsConstructor
public class RuleServiceImpl implements BaseCustomFunctionInterface<RuleDto> {
    static final String msg = "提示消息";

    @Override
    public Object execute(RuleDto ruleDto, Map<String, Object> params) {
        RuleRunService bean = SpringContextUtil.getBean(RuleRunService.class);
        RuleDesignService ruleService = SpringContextUtil.getBean(RuleDesignService.class);
        JvsApp app = CurrentAppUtils.getApp();
        CurrentAppUtils.clear();
        RuleDesignPo po = ruleService.getEnableDesign(ruleDto.getKey());
        if (ObjectNull.isNull(po)) {
            throw new BusinessException("没有此逻辑");
        }
        //执行之前将上下文对象保存一下。
        //现在循环调用，只支持一次，不能支持第二次，第二次会导致变量冲突
        Map<String, Object> threadLocal = new HashMap<>(SystemThreadLocal.get());
        RuleExecDto rule = RuleSystemThreadLocal.getRule();
        List<String> list = threadLocal.keySet().stream().filter(e -> {
            //前缀匹配删除local
            return (e.toLowerCase().startsWith("rule"));
        }).collect(Collectors.toList());
        list.forEach(e -> SystemThreadLocal.get().remove(e));

        try {
            RuleExecuteDto  run = bean.run(ruleDto.getKey(), ruleDto.getMap());
            if (msg.equals(run.getEndResult().getFunctionName())) {
                //表示程序中止
                RuleSystemThreadLocal.setRunStop();
            }
            if (ObjectNull.isNotNull(run)) {
                //判断是否是中止程序的执行
                return run.getEndResult().getValue();
            } else {
                return null;
            }
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("执行错误");
        } finally {
            CurrentAppUtils.setApp(app);
            //不管是什么情况返回执行时都需要将上下文设置回去
            SystemThreadLocal.setAll(threadLocal);
            RuleSystemThreadLocal.set(rule);
        }
    }
}
