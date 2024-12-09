package cn.bctools.design.rule.impl.rule.ergodic;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleDesignUtils;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.ResultDto;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 需要根据画布信息，保存对应的画布日志，用于回显的时候，查看
 *
 * @author jvs
 */
@Rule(value = "循环容器", group = RuleGroup.常用插件, test = false, customStructure = true, returnType = ClassType.数组, order = 8,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "对数组对象进行遍历，也可传入数字做为循环次数，类似 for 循环。")

public class ErgodicServiceImpl implements BaseCustomFunctionInterface<ErgodicDto> {

    @Autowired
    RunLogService runLogService;

    @Override
    public Object execute(ErgodicDto ergodicDto, Map<String, Object> params) {
        LOG.info("开始循环");
        ErgodicAsyncEnum async = ergodicDto.getAsync();
        if (ObjectNull.isNull(async)) {
            async = ErgodicAsyncEnum.Asynchronous;
        }
        //获取容器前的上下文信息
        RuleExecDto currentCanvasDto = RuleSystemThreadLocal.getRule();
        RuleExecuteDto executeDto = currentCanvasDto.getExecuteDto();
        if (ObjectNull.isNull(executeDto.getExecCanvasId())) {
            //默认为主画布
            executeDto.setExecCanvasId("main");
        }
        //获取全局变量,并设置到对象中
        //获取当前执行到的节点 Id
        String execNodeId = executeDto.getExecNodeId();
        //根据节点ID,寻找下一个开始节点,获取到新的画布, 拿着请求参数 ,执行新的画布结果
        HtmlGraph graph = currentCanvasDto.getGraph();
        HtmlGraph htmlGraph = graph.getErgodicCanvas().get(execNodeId);
        if (ObjectNull.isNull(htmlGraph)) {
            return null;
        }
        //设置上下文
        htmlGraph.setTid(graph.getTid());
        List<Map<String, Object>> body = new ArrayList<>();
        if (ergodicDto.getBody() instanceof List) {
            List listBody = (List) ergodicDto.getBody();
            if (!listBody.isEmpty()) {
                if (listBody.get(0) instanceof Map) {
                    body.addAll(listBody);
                } else {
                    //数组循环
                    listBody.forEach(a -> body.add(Dict.create().set("key", a)));
                }
            }
            extracted(async, body, currentCanvasDto, execNodeId, graph, htmlGraph, currentCanvasDto.getType());
            return body;
        } else if (ergodicDto.getBody() instanceof Number) {
            //如果是数字，则从1到具体的数字进行循环
            IntStream.iterate(1, i -> i + 1).limit((Integer) ergodicDto.getBody()).forEach(e -> body.add(Dict.create().set("key", e)));
            extracted(async, body, currentCanvasDto, execNodeId, graph, htmlGraph, currentCanvasDto.getType());
            return body;
        } else if (ergodicDto.getBody() instanceof Map) {
            body.add((Map<String, Object>) ergodicDto.getBody());
            extracted(async, body, currentCanvasDto, execNodeId, graph, htmlGraph, currentCanvasDto.getType());
            return body.get(0);
        } else {
            throw new BusinessException("不支持此类型");
        }
    }

    /**
     * 执行结果数据
     *
     * @param body
     * @param currentCanvasDto
     * @param execNodeId
     * @param graph
     * @param htmlGraph
     */
    private void extracted(ErgodicAsyncEnum async, List<Map<String, Object>> body, RuleExecDto currentCanvasDto, String execNodeId, HtmlGraph graph, HtmlGraph htmlGraph, RunType type) {
        LOG.info("执行循环");
        if (ErgodicAsyncEnum.Asynchronous_Blocking.equals(async)) {
            //异步执行
            body = asynchronousBlocking(body, currentCanvasDto, execNodeId, graph, htmlGraph, type);
        } else {
            //同步执行
            asynchronous(body, currentCanvasDto, execNodeId, graph, htmlGraph, type);
        }
    }

    /**
     * 异步执行
     *
     * @param body
     * @param currentCanvasDto
     * @param execNodeId
     * @param graph
     * @param htmlGraph
     * @return
     */
    private List<Map<String, Object>> asynchronousBlocking(List<Map<String, Object>> body, RuleExecDto currentCanvasDto, String execNodeId, HtmlGraph graph, HtmlGraph htmlGraph, RunType type) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String tenantId = TenantContextHolder.getTenantId();

        List<CompletableFuture<Result>> tasks = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) {
            int index = i;
            CompletableFuture<Result> resultCompletableFuture = CompletableFuture.supplyAsync(() -> {
                RuleSystemThreadLocal.threadLocal.remove();
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return getResult(currentCanvasDto, index, body, execNodeId, graph, htmlGraph, tenantId, type);
            }, RuleStartUtils.EXECUTOR);
            tasks.add(resultCompletableFuture);
        }
        List<Result> collect = tasks.stream().map(CompletableFuture::join).collect(Collectors.toList());
        List<RunLogPo> ergodicLogs = collect.parallelStream().map(e -> e.log).collect(Collectors.toList());
        if (currentCanvasDto.getOpenLogRecording()) {
            runLogService.saveBatch(ergodicLogs);
        }
        return collect.stream().map(e -> e.map).collect(Collectors.toList());
    }

    /**
     * 同步执行
     *
     * @param body
     * @param currentCanvasDto
     * @param execNodeId
     * @param graph
     * @param htmlGraph
     * @return
     */
    private void asynchronous(List<Map<String, Object>> body, RuleExecDto currentCanvasDto, String execNodeId, HtmlGraph graph, HtmlGraph htmlGraph, RunType type) {
        String tenantId = TenantContextHolder.getTenantId();
        List<RunLogPo> logPos = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) {
            Result result = getResult(currentCanvasDto, i, body, execNodeId, graph, htmlGraph, tenantId, type);
            logPos.add(result.log);
            if (ObjectNull.isNotNull(result.endResult)) {
                if (result.ex.getStats()) {
                    //如果为成功，则替换最新的数据值
                    body.set(i, result.map);
                }
                String functionName = result.endResult.getFunctionName();
                //如果表示是退出循环，则直接不再向下执行了
                if ("循环控制".equals(functionName) && Boolean.parseBoolean(result.endResult.getValue().toString())) {
                    //表示循环控制退出
                    break;
                }
                //如果最后一个节点是消息节点代表程序中止， 则直接中止程序
                //中止后，不再继续执行
                if (RuleSystemThreadLocal.runStop()) {
                    //当前循环容器退出程序后， 后续的需要再进行退出
                    break;
                }
            }
        }
        //判断是否需要去记录日志
        if (currentCanvasDto.getOpenLogRecording()) {
            runLogService.saveBatch(logPos);
        }
    }


    @NotNull
    private Result getResult(RuleExecDto currentCanvasDto, int i, List<Map<String, Object>> body, String execNodeId, HtmlGraph graph, HtmlGraph htmlGraph, String tenantId, RunType type) {
        LOG.info("循环初始化数据");
        RuleSystemThreadLocal.setGlobalVariable(currentCanvasDto.getExecuteDto().getNodeResult(), currentCanvasDto.getExecuteDto().getExecCanvasId());
        //设置行号 支持多层
        RuleSystemThreadLocal.setErgodic(i);
        //设置当前画布信息
        Map<String, Object> map = body.get(i);
        //执行循环  并设置正在执行的画布信息
        RuleExecuteDto ex = new RuleExecuteDto(map).setExecCanvasId(execNodeId);
        ex.setReqVariableMap(map);
        ResultDto resultDto = new ResultDto().setValue(map).setClassType(ClassType.未识别).setTime(0L);
        ex.getNodeResult().put(execNodeId, resultDto);
        //需要将循环节点数据提前放到节点上, 然后再执行
        RuleExecDto ruleExecDto = new RuleExecDto().setExecuteDto(ex)
                .setSecret(currentCanvasDto.getSecret())
                .setType(type)
                .setOpenLogRecording(currentCanvasDto.getOpenLogRecording())
                .setTid(currentCanvasDto.getTid())
                .setPath((ObjectNull.isNotNull(currentCanvasDto.getPath()) ? (currentCanvasDto.getPath() + ",") : "") + "[" + execNodeId + "," + i + "]").setGraph(graph);
        RuleSystemThreadLocal.set(ruleExecDto);

        RunLogPo logPo = new RunLogPo();
        logPo.setStartTime(LocalDateTime.now());
        LOG.info("执行循环其中" + i + "次");
        RuleDesignUtils.sequentialExecution(htmlGraph, ex);
        LOG.info("执行循环其中" + i + "完成");
        //获取遍历的返回结果
        RuleSystemThreadLocal.getRule().getExecuteDto().getNodeResult().putAll(ex.getNodeResult());
        //将上下文的对象再放到执行中去
        ConcurrentHashMap<String, ResultDto> globalVariable = new ConcurrentHashMap<>(RuleSystemThreadLocal.getGlobalVariable(currentCanvasDto.getExecuteDto().getExecCanvasId()));
        currentCanvasDto.getExecuteDto().setNodeResult(globalVariable);
        RuleSystemThreadLocal.set(currentCanvasDto);

        logPo.setEndTime(LocalDateTime.now());
        logPo.setReqRunKey(RuleSystemThreadLocal.getRule().getSecret());
        logPo.setStatus(true);
        logPo.setSort(i);
        logPo.setRunType(type);
        logPo.setPath(ruleExecDto.getPath());
        logPo.setParentId(execNodeId);
        logPo.setVariableMap(ex.getVariableMap());
        logPo.setTenantId(tenantId);
        logPo.setResult(JSON.parseObject(JSON.toJSONString(ex)));
        logPo.setTid(currentCanvasDto.getTid());
        logPo.setErrorMsg(ex.getErrorMessage());

        ResultDto endResult = ex.getEndResult();
        LOG.info("返回循环执行结果");
        return new Result(map, ex, endResult, logPo);
    }

    private static class Result {
        public final Map<String, Object> map;
        public final RuleExecuteDto ex;
        public final ResultDto endResult;
        public final RunLogPo log;

        public Result(Map<String, Object> map, RuleExecuteDto ex, ResultDto endResult, RunLogPo log) {
            this.map = map;
            this.ex = ex;
            this.endResult = endResult;
            this.log = log;
        }
    }

}
