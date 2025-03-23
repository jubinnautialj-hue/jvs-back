package cn.bctools.design.rule.impl.dingding.dingding;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.dingding.DingRes;
import cn.bctools.dingding.DingSendUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.bctools.rule.tools.ftp.FtpSelectedOption;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "钉钉机器人",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        order = 2,
        help = "https://open.dingtalk.com/document/robots/custom-robot-access",
//        iconUrl = "rule-dingding",
        explain = "钉钉机器人节点可应用在设备消息推送、监控报警、信息公示等多种场景中，支持将设备告警信息、设备属性信息、业务逻辑处理结果等，以定时触发或设备触发等方式推送到钉钉群中。目前，仅支持信息推送，不支持返回消息处理。"
)
@AllArgsConstructor
public class DingDingServiceImpl implements BaseCustomFunctionInterface<DingDingDto> {

    DingSendUtils dingSendUtils;
    ModelInterface modelInterface;


    @Override
    public Object execute(DingDingDto dto, Map<String, Object> params) {

        Object byKey = modelInterface.getByKey(dto.getDingDingUrl());
        //获取的数据源
        DingDingSelectedOption option = BeanCopyUtil.copy(DingDingSelectedOption.class, byKey);
        //获取的数据源
        try {
            DingRes dingRes = dingSendUtils.sendMessage(option.getWebhook(), option.getSecret(), dto.getContent(), dto.getMobiles());
            log.info("钉钉发送成功,{}", JSONObject.toJSONString(dingRes));
        } catch (Exception e) {
            log.error("发送钉钉失败", e);
        }
        return true;
    }

}
