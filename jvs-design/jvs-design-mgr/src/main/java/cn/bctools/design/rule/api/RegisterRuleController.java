package cn.bctools.design.rule.api;

import cn.bctools.common.utils.R;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.rule.dto.RuleFunctionDto;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 逻辑引擎服务注册节点
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "注册逻辑引擎")
@RestController
@RequestMapping("/register")
public class RegisterRuleController {

    RuleExternalService ruleExternalService;

    @PostMapping("/{applicationName}")
    public R register(@PathVariable String applicationName, @RequestBody List<RuleFunctionDto> obj) {
        ruleExternalService.register(obj,applicationName);
        return R.ok();
    }

}
