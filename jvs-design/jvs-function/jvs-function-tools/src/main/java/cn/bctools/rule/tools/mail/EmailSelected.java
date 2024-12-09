package cn.bctools.rule.tools.mail;

import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Service
@AllArgsConstructor
public class EmailSelected implements ParameterSelected<String> {


    RedisUtils redisUtils;
    static final String AUDIT_STATE_PASS = "AUDIT_STATE_PASS";
    static final String KEY = "message:push:EMAIL";

    @Override
    public List<ParameterOption<String>> getOptions() {

        //如果是不是最新的，则直接返回缓存数据
        if (redisUtils.hasKey(KEY)) {
            List<AliSmsTemplateVo> allPrivateTemplate = (List<AliSmsTemplateVo>) redisUtils.get(KEY);
            List<ParameterOption<String>> collect = allPrivateTemplate.stream()
                    .filter(e -> {
                        return AUDIT_STATE_PASS.equals(e.getAuditStatus());
                    })
                    .map(e -> new ParameterOption<String>().setLabel(e.getTemplateName()).setValue(e.getTemplateCode()))
                    .collect(Collectors.toList());
            return collect;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }


}
