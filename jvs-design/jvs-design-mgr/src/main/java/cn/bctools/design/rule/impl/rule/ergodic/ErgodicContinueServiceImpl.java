package cn.bctools.design.rule.impl.rule.ergodic;

import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 需要根据画布信息，保存对应的画布日志，用于回显的时候，查看
 *
 * @author wl
 */
@Rule(value = "循环控制",
        label = "Loop control",
        group = RuleGroup.常用插件,
        order = 9,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "循环容器中执行到当前节点，执行成功后循环退出。类似 for 循环中的 break")
@AllArgsConstructor
public class ErgodicContinueServiceImpl implements BaseCustomFunctionInterface<ErgodicContinueDto> {

    RunLogService runLogService;

    @Override
    public Object execute(ErgodicContinueDto ergodicDto, Map<String, Object> params) {
        if (ergodicDto.getBody()) {
            //直接退出当前循环,返回标识，如果是循环时，并返回了此节点，将直接不再执行当前循环容器，直接执行下一次循环容器
            return true;
        }
        return false;
    }
}
