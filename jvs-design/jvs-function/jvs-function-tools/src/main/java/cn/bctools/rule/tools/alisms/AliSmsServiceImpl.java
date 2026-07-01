package cn.bctools.rule.tools.alisms;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.api.AliSmsApi;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.sms.config.AliSmsConfig;
import cn.bctools.sms.utils.SmsSendUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 短信发送功能
 */
@Slf4j
@Rule(value = "阿里短信",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.数组,
        testShowEnum = TestShowEnum.TEXT,
        order = 14,
        statsMsg ="请在【系统设置】完善【短信配置】后，并在【短信消息模板】中同步短信模板",
        explain = "阿里短信服务，用淘宝账号打通三大运营商通信能力。<br/>注意：短信签名和模板是必须的，而且要通过审核后方可使用，不允许随意发送短信。详细查看【后台管理】-【短信配置】"
)
@AllArgsConstructor
public class AliSmsServiceImpl implements BaseCustomFunctionInterface<SmsFunctionDto> {

    AliSmsApi aliSmsApi;
    AuthTenantConfigServiceApi configServiceApi;
    AuthUserServiceApi authUserServiceApi;

    @SneakyThrows
    @Override
    public Object execute(SmsFunctionDto dto, Map<String, Object> params) {
        //当前这个租户的配置信息
        R<String> key = configServiceApi.key(ConfigsTypeEnum.SMS_CONFIGURATION, TenantContextHolder.getTenantId());
        SysConfigSms config = configServiceApi.convertKey(ConfigsTypeEnum.SMS_CONFIGURATION, key.getData());
        if (Boolean.FALSE.equals(config.getEnable())) {
            throw new IllegalArgumentException("未找到短信配置，请核实!");
        }
        List<String> phones = new ArrayList<>();
        for (String userId : dto.getUserIds()) {
            try {
                String phone = authUserServiceApi.getById(userId).getData().getPhone();
                if (ObjectNull.isNotNull(phone)) {
                    phones.add(phone);
                }
            } catch (Exception e) {
                phones.add(userId);
            }
        }
        AliSmsConfig copy = BeanCopyUtil.copy(config, AliSmsConfig.class);
        Object o = SmsSendUtils.aliImpl(copy, dto.getTemplateCode(), phones, dto.getContent());
        log.info("短信发送信息为{}", JSONObject.toJSONString(dto.getContent()));
        return o;
    }

}
