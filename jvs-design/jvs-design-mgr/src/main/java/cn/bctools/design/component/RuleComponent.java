package cn.bctools.design.component;

import cn.bctools.common.utils.R;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.use.api.RuleApi;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author jvs
 * The type Rule component.
 */
@Slf4j
@RestController
@Api(tags = "[Feign]逻辑引擎")
@AllArgsConstructor
public class RuleComponent implements RuleApi {

    /**
     * The Rule run service.
     */
    RuleRunService ruleRunService;

    @Override
    public R run(String key, Map<String, Object> dataMap) {
        RuleExecuteDto run = ruleRunService.run(key, dataMap);
        return R.ok(run);
    }

}
