package cn.bctools.design.rule.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.constant.OssConstantType;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.utils.OssUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.mapper.RuleOptionDao;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 跑逻辑与设计请求接口
 *
 * @author guojing
 */
@Api(tags = "逻辑设计")
@Slf4j
@RestController
@RequestMapping("/app/design/{appId}/rule")
@AllArgsConstructor
public class DesignController {

    RedisUtils redisUtils;
    RuleStartUtils ruleStartUtils;
    RunLogService runLogService;
    RuleDesignService designService;
    RuleOptionDao ruleOptionService;

    private final static String RULE_KEY_FORMAT = "rule:run:key:%s";


    @SneakyThrows
    @Log
    @ApiOperation("获取测试结果")
    @GetMapping("/test/{id}")
    public R<RuleExecuteDto> returnTest(@PathVariable String id, @PathVariable String appId) {
        //判断数据库中是否存在
        long count = runLogService.count(Wrappers.query(new RunLogPo().setId(id).setJvsAppId(appId)));
        if (count != 1) {
            return R.failed("测试日志为空");
        }
        final String key = String.format(RULE_KEY_FORMAT, id);
        //判断缓存中是否存在
        RuleExecuteDto result = (RuleExecuteDto) redisUtils.get(key);
        if (Objects.isNull(result)) {
            return R.failed("测试结果为空");
        }
        //如果结束了,直接删除掉缓存数据
        if (Boolean.TRUE.equals(result.getIsEnd())) {
            redisUtils.del(key);
        }
        return R.ok(result);
    }

    @SneakyThrows
    @Log
    @ApiOperation("测试运行逻辑")
    @PostMapping("/test/{id}")
    public R test(@RequestBody Map<String, Object> parameters, @PathVariable String id, @PathVariable String appId) {
        RuleDesignPo po = designService.getOne(Wrappers.query(new RuleDesignPo().setId(id).setJvsAppId(appId)));
        OssUtils.setOssTemplateBusinessId(OssConstantType.OSS_RULE_TEST, appId);

        if (ObjectNull.isNull(po)) {
            return R.failed("没有找到逻辑");
        }
        //根据参数获取的值
        RuleSystemThreadLocal.setParameterSelectedOption(parameters);
        //组装默认值参数
        Map<String, Object> stringObjectMap = new HashMap<>(2);
        RuleExecuteDto data = new RuleExecuteDto();
        stringObjectMap.putAll(po.getParameterPos());
        RunLogPo logPo = runLogService.create(appId, po.getSecret(), RunType.TEST, stringObjectMap, po.getReqType(), po.getReqType(), po.getSync());
        stringObjectMap.put("ruleKey", po.getSecret());
        data.setReqVariableMap(po.getParameterPos()).setVariableMap(po.getParameterPos());
        String key = String.format(RULE_KEY_FORMAT, logPo.getId());
        SystemThreadLocal.set("redisKey", key);
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        RuleExecDto ruleExecDto = new RuleExecDto()
                .setExecuteDto(data)
                .setType(RunType.TEST)
                .setSecret(po.getSecret())
                .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
        if (po.getSync()) {
            RuleStartUtils.EXECUTOR.execute(() -> {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                ruleStartUtils.start(po, logPo, ruleExecDto);
            });
            //返回执行日志对象
            return R.ok(logPo.getId());
        } else {
            ruleStartUtils.start(po, logPo, ruleExecDto);
            //返回新的结构
            data.setTid(logPo.getTid());
            data.setGraph(ruleExecDto.getGraph());
            //返回执行日志对象
            return R.ok(data);
        }
    }

}
