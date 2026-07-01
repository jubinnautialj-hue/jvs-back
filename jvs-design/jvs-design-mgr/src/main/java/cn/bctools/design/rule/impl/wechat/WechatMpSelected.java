package cn.bctools.design.rule.impl.wechat;

import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Service
@AllArgsConstructor
public class WechatMpSelected implements ParameterSelected<String> {

    WechatOfficialAccountApi wechatOfficialAccountApi;

    RedisUtils redisUtils;

    @Override
    public List<ParameterOption<String>> getOptions() {
        return wechatOfficialAccountApi.getAllPrivateTemplate(false).stream().map(e -> new ParameterOption<String>().setLabel(e.getTitle()).setValue(e.getTemplateId())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }
}
