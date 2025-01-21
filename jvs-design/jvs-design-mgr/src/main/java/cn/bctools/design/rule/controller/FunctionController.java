package cn.bctools.design.rule.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PinyinUtils;
import cn.bctools.common.utils.R;
import cn.bctools.design.constant.OssConstantType;
import cn.bctools.design.rule.dto.FunctionGroupDto;
import cn.bctools.design.rule.dto.FunctionTestDto;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.utils.OssUtils;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.OptionsDto;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.dto.OptionsType;
import cn.bctools.rule.dto.RuleFunctionDto;
import cn.bctools.rule.dto.RuleFunctionDtoParameter;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.utils.RuleDesignUtils;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.bctools.rule.config.SystemInit.selectedMap;

/**
 * 自定义方法控制类
 *
 * @author guojing
 */
@Slf4j
@Api(tags = "方法管理")
@RestController
@RequestMapping("/app/design/{appId}/rule/custom/function")
public class FunctionController {

    @Autowired
    Map<String, LinkFieldSelected> fieldSelectedMap;
    @Autowired
    FunctionBusinessService functionBusinessService;
    @Autowired
    RuleDesignService designService;
    @Autowired
    RuleExternalService ruleExternalService;

    @Log
    @ApiOperation(value = "获取所有分组")
    @GetMapping("/group")
    public R group() {
        List<String> group = Arrays.stream(RuleGroup.values()).map(Enum::name).collect(Collectors.toList());
        ruleExternalService.list(new LambdaQueryWrapper<RuleExternalPo>().select(RuleExternalPo::getRuleGroup))
                .forEach(e -> group.add(e.getRuleGroup()));
        List<Dict> collect = group.stream()
                .map(e -> Dict.create().set("key", e).set("value", e)).collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 获取方法关联参数
     *
     * @return 返回选择的值
     */
    @Log(back = false)
    @ApiOperation("获取方法关联参数")
    @GetMapping("/link/{type}/{id}/{field}")
    public R getLinkTypeValue(@PathVariable String type, @PathVariable String id, @PathVariable String field, @PathVariable String appId) {
        Object o = fieldSelectedMap.get(type).link(id, field);
        return R.ok(o);
    }

    @Log(back = false)
    @ApiOperation("搜索获取所有方法")
    @PostMapping("/list/search")
    public R<List<RuleFunctionDto>> search(@RequestBody Map<String, Object> parameters, @PathVariable String appId) {
        List<RuleFunctionDto> collect = getList(parameters, appId).getData().stream().flatMap(e -> e.getList().stream()).collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 获取所有的自定义方法, 并进行分组
     *
     * @return 所有自定义方法信息
     */
    @Log(back = false)
    @ApiOperation("获取所有方法")
    @PostMapping("/list")
    public R<List<FunctionGroupDto>> getList(@RequestBody Map<String, Object> parameters, @PathVariable String appId) {
        Collection o = (Collection) parameters.getOrDefault("group", new HashSet<>());
        String name = (String) parameters.getOrDefault("name", "");
        if (ObjectNull.isNull(o)) {
            o.add(RuleGroup.常用插件.name());
        }
        //根据参数获取的值
        RuleSystemThreadLocal.setParameterSelectedOption(parameters);
        Map<String, RuleFunctionDto> functionsMap = SystemInit.getFunctionsMap(o, name);
        //添加不同的分组数据
        Set<String> groups = new LinkedHashSet<>();
        for (RuleGroup value : RuleGroup.values()) {
            groups.add(value.name());
        }
        Map<String, List<RuleFunctionDto>> collect = functionsMap.values()
                .stream()
                .filter(e -> {
                    if (ObjectNull.isNotNull(name)) {
                        return e.getFunctionName().toLowerCase().contains(name.toLowerCase()) || PinyinUtils.getCameCasePinYin(e.getFunctionName()).toLowerCase().contains(name.toLowerCase());
                    } else {
                        if (ObjectNull.isNotNull(o)) {
                            return o.contains(e.getGroup());
                        }
                        return true;
                    }
                })
                .sorted(Comparator.comparingInt(RuleFunctionDto::getOrder).reversed())
                .collect(Collectors.groupingBy(RuleFunctionDto::getGroup));
        List<FunctionGroupDto> list = new ArrayList<>();
        List<RuleExternalPo> list1 = null;
        if (ObjectNull.isNotNull(name) || ObjectNull.isNotNull(o)) {
            list1 = ruleExternalService.list(new LambdaQueryWrapper<RuleExternalPo>()
                    //如果指定了加载数据，则只查询这些类型的
                    .like(ObjectNull.isNotNull(name), RuleExternalPo::getName, name)
                    .in(ObjectNull.isNotNull(o), RuleExternalPo::getRuleGroup, o));
            list1.forEach(e -> {
                groups.add(e.getRuleGroup());
            });
        }
        Map<String, List<RuleFunctionDto>> ruleExternal = new HashMap<>(1);
        if (ObjectNull.isNotNull(list1)) {
            ruleExternal = list1
                    .stream()
                    .map(e -> new RuleFunctionDto().setEdit(true).setCustomStructure(true).setStatus(e.getStatus()).setIcon(e.getIcon()).setTest(true).setGroup(e.getRuleGroup()).setFunctionName(e.getName()).setExplain(e.getExplainInfo())
                            .setParameters(e.getFieldLists()).setFunctionId(e.getId()))
                    .map(e -> {
                        try {
                            RuleGroup ruleGroup = RuleGroup.valueOf(e.getGroup());
                            if (ruleGroup.isExternal()) {
                                List<OptionsDto> orDefault = SystemInit.getGroupNameOption(ruleGroup.name());

                                e.getParameters().add(0, new RuleFunctionDtoParameter()
                                        .setInfo("配置")
                                        .setKey("option")
                                        .setSupportFunction(false)
                                        .setNecessity(true)
                                        .setSubtype(InputType.text)
                                        .setInputType(InputType.selected)
                                        .setCustomOptionValue(e.getGroup())
                                        .setCustomOption(orDefault)
                                        .setOptionsType(OptionsType.dboptions));
                                //判断是否存在扩展属性，如果有，添加到第一个属性中用于选择项
                                return SystemInit.getSelected(e, v -> selectedMap.get(v));
                            }
                            return e;
                        } catch (IllegalArgumentException ex) {
                            return e;
                        }
                    })
                    .collect(Collectors.groupingBy(RuleFunctionDto::getGroup));
        }
        boolean isGroup = parameters.containsKey("group");
        for (String group : groups) {
            List<RuleFunctionDto> ruleFunctionDtos = collect.getOrDefault(group, new ArrayList<>(1));
            if (ObjectNull.isNotNull(ruleExternal.get(group))) {
                ruleFunctionDtos.addAll(ruleExternal.get(group));
            }
            if (ObjectNull.isNotNull(ruleFunctionDtos)) {
                list.add(new FunctionGroupDto().setGroupName(group).setList(ruleFunctionDtos));
            } else if (!isGroup) {
                list.add(new FunctionGroupDto().setGroupName(group).setList(ruleFunctionDtos));
            }
        }
        return R.ok(list);
    }

    @Log(back = false)
    @SneakyThrows
    @ApiOperation("方法测试")
    @PostMapping("/test/{secret}")
    public R testFunction(@RequestBody FunctionTestDto testDto, @PathVariable String secret, @PathVariable String appId) {
        //link关联字段处理的时候，所有被关联的字段都会被加密,除特殊datafilterfield类型不被加密
        RuleDesignPo po = designService.getOne(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setSecret(secret)));
        if (ObjectNull.isNull(po)) {
            throw new BusinessException("应用错误或设计不存在");
        }

        //组装默认值参数
        Map<String, Object> stringObjectMap = po.getParameterPos();
        stringObjectMap.put("jvsAppId", testDto.getJvsAppId());
        OssUtils.setOssTemplateBusinessId(OssConstantType.OSS_RULE_TEST, testDto.getJvsAppId());
        //根据参数获取的值
        RuleSystemThreadLocal.setParameterSelectedOption(stringObjectMap);
        //执行方法，获取结果，校验并执行
        try {
            Object req = null;
            Object execute = RuleDesignUtils.execNode(testDto.getNode(), stringObjectMap, req);
            return R.ok(execute);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                return R.failed(e.getMessage());
            }
            log.error("节点执行测试错误", e);
            return R.failed("逻辑节点执行错误");
        }
    }

    @ApiOperation("节点复制同时复制公式")
    @PostMapping("/{id}/copy/node")
    public R copy(@RequestBody Map<String, Object> testDto, @PathVariable String id) {
        //节点复制
        //将当前设计的公式 id 全部拿出来。
        //将所有 id与这个设计 json做匹配关系
        //如果 id相同 则表示存在，并将原有 id的内容进行复制。形成新的公式对象。将新的 id 复制为老的 id值。
        Map<String, FunctionBusinessPo> map = functionBusinessService.list(Wrappers.query(new FunctionBusinessPo().setDesignId(id)))
                .stream().collect(Collectors.toMap(FunctionBusinessPo::getId, Function.identity()));
        if (ObjectNull.isNotNull(map)) {
            List<FunctionBusinessPo> list = new ArrayList<>();
            String jsonString = JSONObject.toJSONString(testDto);
            for (String oldId : map.keySet()) {
                if (jsonString.contains(oldId)) {
                    FunctionBusinessPo functionBusinessPo = map.get(oldId).setId(IdWorker.getIdStr());
                    functionBusinessPo.setCreateTime(LocalDateTime.now());
                    functionBusinessPo.setUpdateTime(LocalDateTime.now());
                    list.add(functionBusinessPo);
                    //产生新的对象数据
                    jsonString = jsonString.replaceAll(oldId, functionBusinessPo.getId());
                }
            }
            functionBusinessService.saveBatch(list);
            return R.ok(JSONObject.parseObject(jsonString));
        } else {
            return R.ok(testDto);
        }
    }


}
