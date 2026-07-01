package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.SendSigningReminderDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureapply sign.
 *
 * @author jvs
 */
@Rule(value = "发送签约提醒",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "文件签约提醒，通过发送手机短信方式提醒签约方签署合同。支持单个web签约合同或批量签署合同，短信中的短链接地址有效期是72小时，超过72小时后需要重新发起。"
)
@AllArgsConstructor
public class SendSigningReminder implements BaseCustomFunctionInterface<SendSigningReminderDto> {

    @Override
    public Object execute(SendSigningReminderDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/notify", dto);
    }

}
