package cn.bctools.design.rule.impl.rule.app.rule;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class ApiRuleSelected implements ParameterSelected<String> {

    RuleDesignService ruleDesignService;

    @Override
    public String key() {
        return "jvsAppId";
    }

    @Override
    public List<ParameterOption<String>> getOptions() {
        //应用ID 必须要从前端 获取 如果获取 不到，即返回错误不可使用
        String appId = SystemThreadLocal.get(key());
        if (ObjectNull.isNull(appId)) {
            return new ArrayList<>();
        }
        return ruleDesignService.list(new LambdaQueryWrapper<RuleDesignPo>()
                        .eq(RuleDesignPo::getJvsAppId, appId))
                .stream()
                .map(e -> new ParameterOption<String>().setLabel(e.getName()).setValue(e.getSecret())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
