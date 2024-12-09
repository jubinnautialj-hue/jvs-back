package cn.bctools.rule.error;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 两种状态返回给前端，主要返回示例为 R.ok("xxx")  R.failed("xxxx")
 *
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "提示消息",
        group = RuleGroup.常用插件,
        returnType = ClassType.对象,
        order = 1,
//        iconUrl = "rule-drdsfenbushiguanxixingshujukufuwuDRD",
        explain = "将业务消息返回给前端进行展示，此节点需要事件类型或api 类型的逻辑才会有界面展示提示，其它场景逻辑执行时只会结束运行。此节点将中止程序的运行,常用按钮、或前后置操作失败的返回。此节点执行即为程序结束"
)
public class MessageTipsServiceImpl implements BaseCustomFunctionInterface<MessageTipsDto> {

    @Override
    public Object execute(MessageTipsDto errorDto, Map<String, Object> params) {
        if (!errorDto.getOnOff()) {
            RuleSystemThreadLocal.getRule().getExecuteDto().setException(new RuleException(RuleExceptionEnum.业务错误, errorDto.getMessage())).setMessageResult(errorDto.getData()).setStats(errorDto.getOnOff()).setSyncMessageTips(errorDto.getMessage());
        }
        return errorDto;
    }

}
