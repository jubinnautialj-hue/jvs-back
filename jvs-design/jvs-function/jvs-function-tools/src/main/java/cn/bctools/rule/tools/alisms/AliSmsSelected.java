package cn.bctools.rule.tools.alisms;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取配置文件中的数据，确定阿里云选择项
 * 如果是自定义类型，则不需要实现，如果是已知类型，则需要固定选择项
 *
 * @author Administrator
 */
@Service
@AllArgsConstructor
public class AliSmsSelected implements ParameterSelected<String> {

    RedisUtils redisUtils;

    @Override
    public List<ParameterOption<String>> getOptions() {

        String key = "message:push:SMS" + TenantContextHolder.getTenantId();
        //如果是不是最新的，则直接返回缓存数据
        if (redisUtils.hasKey(key)) {
            List<AliSmsTemplateVo> allPrivateTemplate = (List<AliSmsTemplateVo>) redisUtils.get(key);
            List<ParameterOption<String>> collect = allPrivateTemplate.stream()
                    .filter(e -> "AUDIT_STATE_PASS".equals(e.getAuditStatus()))
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
