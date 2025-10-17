package cn.bctools.design.rule.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.constant.OssConstantType;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.oss.utils.OssUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.AsyncService;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.UrlUtils;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlEdge;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.NodeHtml;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static cn.bctools.rule.utils.RuleDesignUtils.doNext;


/**
 * @author guojing
 */
@Component
@Slf4j
@AllArgsConstructor
public class RuleRunServiceImpl implements RuleRunService, AsyncService {

    private final static String RULE_KEY_FORMAT = "rule:run:key:%s";

    RedisUtils redisUtils;
    RuleDesignService ruleService;
    RuleExternalService ruleExternalService;
    RuleStartUtils ruleStartUtils;
    RunLogService runLogService;

    @Override
    public RuleExecuteDto run(String ruleKey, Map<String, Object> variableMap) {

        //根据参数获取的值
        RuleSystemThreadLocal.setParameterSelectedOption(variableMap);
        //根据租户ID调用自己的服务
        RuleDesignPo po = ruleService.getEnableDesign(ruleKey);
        if(ObjectNull.isNull(po)){
            throw new BusinessException("逻辑不存在");
        }
        OssUtils.setOssTemplateBusinessId(OssConstantType.OSS_RULE_RUN, po.getJvsAppId());

        //通过定时任务执行时，可能租户信息丢失
        if (ObjectNull.isNull(TenantContextHolder.getTenantId())) {
            TenantContextHolder.setTenantId(po.getTenantId());
        }
        if (ObjectNull.isNull(po)) {
            return null;
        }
        log.info("执行逻辑, ruleKey: {}, 租户id: {}", ruleKey, po.getTenantId());
        // 获取逻辑流程运行时参数
        // 1.获取数据库参数
        Map<String, Object> ruleVariable = new HashMap<>(16);
        if (po.getParameterPos() != null && ObjectNull.isNull(variableMap)) {
            ruleVariable.putAll((po.getParameterPos()));
        }
        // 2.获取请求体参数
        if (ObjectUtils.isNotEmpty(variableMap)) {
            ruleVariable.putAll(variableMap);
        }
        // 3.获取请求路径上的参数(优先级最高)
        Map<String, Object> urlParams = UrlUtils.getUrlParams();
        if (ObjectUtils.isNotEmpty(urlParams)) {
            ruleVariable.putAll(urlParams);
        }
        RunLogPo logPo = runLogService.create(po.getJvsAppId(), po.getSecret(), RunType.REAL, ruleVariable, po.getReqType(), po.getReqType(), po.getSync());
        ruleVariable.put("ruleKey", ruleKey);
        RuleExecuteDto data = new RuleExecuteDto().setReqVariableMap(ruleVariable).setVariableMap(ruleVariable);
        String key = String.format(RULE_KEY_FORMAT, logPo.getId());
        SystemThreadLocal.set("redisKey", key);
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        RuleExecDto ruleExecDto = new RuleExecDto()
                .setExecuteDto(data)
                .setType(RunType.REAL)
                .setSecret(po.getSecret())
                .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));

        ruleStartUtils.start(po, logPo, ruleExecDto);
        //返回执行日志对象
        if (ObjectNull.isNotNull(data.getErrorMessage())) {
            throw new BusinessException(data.getErrorMessage());
        }
        if (ObjectNull.isNull(data.getEndResult())) {
            return null;
        }
        return data;
    }

    @Override
    public void importCheck(List<RuleDesignPo> ruleDesignPos) {
        //导入操作校验是否是使用了扩展，并当前这个功能中是否有此扩展模块
//        Set<String> functionMap = ruleExternalService.list(Wrappers.query(new RuleExternalPo().setStatus(true))).stream().map(RuleExternalPo::getName).collect(Collectors.toSet());
//        if (ObjectNull.isNull(functionMap)) {
//            return;
//        }
        //不检查集成扩展是否支持
//        Set<String> set = new HashSet<>();
//        String tenantId = TenantContextHolder.getTenantId();
//        for (RuleDesignPo ruleDesignPo : ruleDesignPos) {
//            ruleDesignPo.setTenantId(tenantId);
//            HtmlGraph htmlGraph = JSONObject.parseObject(ruleDesignPo.getDesignDrawingJson(), HtmlGraph.class);
//            if (ObjectNull.isNull(htmlGraph) || ObjectNull.isNull(htmlGraph.getNodeList())) {
//                continue;
//            }
//            Set<String> collect = htmlGraph.getNodeList().stream().filter(e -> functionMap.contains(e.getName())).map(e -> e.getName()).collect(Collectors.toSet());
//            set.addAll(collect);
//        }
//        if (ObjectNull.isNotNull(set)) {
//            throw new BusinessException("导入失败,本地没有找到" + JSONObject.toJSONString(set) + "逻辑扩展组件,不支持不同环境导入");
//        }
    }

    @Override
    public void async(List<HtmlEdge> edges, List<NodeHtml> nodes, NodeHtml nextNode, RuleExecuteDto executeDto, List<CompletableFuture<RuleExecuteDto>> futureList) {
        int i = Runtime.getRuntime().availableProcessors();
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                i,
                i,
                10L,
                TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        RuleExecDto rule = RuleSystemThreadLocal.getRule();
        CompletableFuture<RuleExecuteDto> future = CompletableFuture.supplyAsync(() -> {
            RuleSystemThreadLocal.set(rule);
            //清空数据
            RuleSystemThreadLocal.threadLocal.remove();
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //设置上下文需要的数据
            RuleSystemThreadLocal.setGlobalVariable(executeDto.getNodeResult(), executeDto.getExecCanvasId());
            // 递归调用当前方法
            doNext(edges, nodes, nextNode, executeDto);
            //将线程里面的执行结果返回回去放主线程中
            RuleExecuteDto dto = RuleSystemThreadLocal.getRule().getExecuteDto();
            return dto;
        }, threadPoolExecutor);
        futureList.add(future);
    }
}
