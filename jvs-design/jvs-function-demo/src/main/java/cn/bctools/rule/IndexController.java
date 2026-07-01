package cn.bctools.rule;


import cn.bctools.common.utils.R;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.RuleGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * The type Index controller.
 */
@Slf4j
@AllArgsConstructor
@RestController
@Api("demo")
@RequestMapping("/index")
public class IndexController {
    /**
     * Index r.
     *
     * @return the r
     */
    @GetMapping
    public R index() {
        return R.ok();
    }

    /**
     * 注册此controoler 为一个逻辑引擎节点
     * 需要保证Rule 的value 与ApiOperation 的value一致
     *
     * @param header the header
     * @param path   the path
     * @param test   the test
     * @return r
     */
    @Rule(value = "服务注册示例", group = RuleGroup.服务插件, explain = "服务节点注册逻辑的示例,包含请求头，url 参数接收值")
    @ApiOperation(value = "服务注册示例")
    @PostMapping("/rule/test/{path}")
    public R<Object> ruleTest(@RequestHeader(value = "header", required = false) String header, @PathVariable("path") String path, @RequestBody Test test) {
        return R.ok(test);
    }

}
