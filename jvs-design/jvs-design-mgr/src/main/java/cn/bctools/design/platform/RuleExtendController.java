package cn.bctools.design.platform;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.design.util.WebServiceUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "逻辑引擎扩展")
@RestController
@RequestMapping("/platform/rule/extend")
public class RuleExtendController {

    RuleExternalService ruleExternalService;
    static final String webservice = "webservice";

    @Log
    @ApiOperation(value = "获取扩展")
    @GetMapping
    public R all(PageDTO<RuleExternalPo> page, RuleExternalPo po) {
        List<RuleExternalPo> collect = SystemInit.getFunctionsMaps().stream()
                .map(e -> new RuleExternalPo().setId(e.getFunctionId()).setStatus(e.getStatus()).setIcon(e.getIcon()).setExplainInfo(e.getExplain()).setName(e.getFunctionName()).setRuleGroup(e.getGroup()).setFieldList(e.getParameters()))
                .collect(Collectors.toList());
        List<RuleExternalPo> list = ruleExternalService.list();
        if(ObjectNull.isNotNull(list)) {
            collect.addAll(list);
        }
        if (ObjectNull.isNotNull(po.getRuleGroup())) {
            collect = collect.stream().filter(e -> e.getRuleGroup().equals(po.getRuleGroup())).collect(Collectors.toList());
        }
        long start = (page.getCurrent() - 1) * page.getSize();
        long end = Math.min(page.getCurrent() * page.getSize(), collect.size());
        List<RuleExternalPo> ruleExternalPos = collect.subList((int) start, (int) end);
        ruleExternalPos.forEach(e -> {
            if (ObjectNull.isNull(e.getStatus())) {
                e.setStatus(true);
            }
        });
        page.setTotal(collect.size());
        page.setRecords(ruleExternalPos);
        return R.ok(page);
    }

    @Log
    @ApiOperation(value = "获取分组设置信息")
    @GetMapping("/group")
    public R ruleGroup() {
        List<String> set = Arrays.stream(RuleGroup.values()).map(Enum::name).collect(Collectors.toList());
        ruleExternalService.list().forEach(e -> set.add(e.getRuleGroup()));
        List<Dict> collect = set.stream().distinct().map(e -> Dict.create().set("key", e).set("value", e).set("url", e)).collect(Collectors.toList());
        return R.ok(collect);
    }

    @Log
    @ApiOperation(value = "获取类型")
    @GetMapping("/types")
    public R types() {
        List<Dict> collect = Arrays.stream(InputType.values()).filter(InputType::getExtend).map(e -> new Dict().set("key", e.get()).set("value", e.get())).collect(Collectors.toList());
        return R.ok(collect);
    }

    @Log
    @ApiOperation(value = "测试逻辑")
    @PutMapping("/test")
    public R test(@RequestBody RuleExternalPo po) {
        Object execute;
        if (webservice.equals(po.getType())) {
            execute = ruleExternalService.executeWebService(po, new HashMap<>(8), e -> e, true);
        } else {
            execute = ruleExternalService.execute(po, new HashMap<>(8), e -> e, true);
        }
        return R.ok(execute);
    }

    @Log
    @ApiOperation(value = "新增扩展")
    @PostMapping
    public R save(@RequestBody RuleExternalPo ruleExternalPo) {
        RuleExternalPo one = ruleExternalService.getOne(new LambdaQueryWrapper<RuleExternalPo>().eq(RuleExternalPo::getName, ruleExternalPo.getName()));
        if (ObjectNull.isNotNull(one)) {
            ruleExternalPo.setId(one.getId());
        }
        ruleExternalService.saveOrUpdate(ruleExternalPo);
        return R.ok("新增成功");
    }

    @Log
    @ApiOperation(value = "修改扩展")
    @PutMapping
    public R put(@RequestBody RuleExternalPo ruleExternalPo) {
        save(ruleExternalPo);
        return R.ok("新增成功");
    }

    @Log
    @ApiOperation(value = "删除扩展")
    @DeleteMapping("/{id}")
    public R delete(@PathVariable String id) {
        ruleExternalService.removeById(id);
        return R.ok("新增成功");
    }

    @ApiOperation(value = "解析地址")
    @PutMapping("/parseWSDL")
    public R parseWsdl(@RequestBody Map<String, String> map) {
        //解析地址
        String url = map.get("url");
        Set<RuleExternalPo> parseList = WebServiceUtils.parseWsdl(url);
        //判断库里面是否存在此方法，如果存在，则不处理，如果不存在则处理
        parseList.forEach(e -> {
            e.setUrl(url);
            e.setFunctionName(e.getName());
            e.setRuleGroup("webservice");
            e.setStatus(false);
            if (ruleExternalService.count(Wrappers.query(new RuleExternalPo().setName(e.getName()))) == 0) {
                ruleExternalService.save(e);
            }
        });
        return R.ok("新增成功");
    }

    @Log(back = false)
    @ApiOperation(value = "获取扩展")
    @GetMapping("/list")
    public R<List<RuleExternalPo>> list(){
        List<RuleExternalPo> collect = ruleExternalService.list(Wrappers.lambdaQuery(RuleExternalPo.class)
                .select(RuleExternalPo::getName, RuleExternalPo::getType));
        return R.ok(collect);
    }


}
