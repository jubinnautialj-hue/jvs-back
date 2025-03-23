package cn.bctools.rule.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.rule.AsyncService;
import cn.bctools.rule.ExternalService;
import cn.bctools.rule.RunParameterException;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.dto.*;
import cn.bctools.rule.entity.enums.*;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.*;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.bctools.rule.entity.enums.RuleExceptionEnum.设计错误;

/**
 * @author wenxin
 */
public class RuleDesignUtils {

    static TaskLogUtil log = new TaskLogUtil(RuleDesignUtils.class);


    public static final List<NodeType> NO_EXECUTE_TYPES = Arrays.asList(NodeType.start, NodeType.end);

    public static Map<InputType, InputTypeTransformInterface> inputTypeTransformInterfaceMap = new HashMap<>(3);

    /**
     * 公式执行的节点
     *
     * @param nodeId
     * @return
     */
    public static Object execNodeId(String nodeId) {
        RuleExecDto rule = RuleSystemThreadLocal.getRule();
        if (ObjectNull.isNull(rule)) {
            return null;
        }
        //执行节点
        Optional<NodeHtml> first = rule.getGraph().getNodeList().stream().filter(e -> e.getId().equals(nodeId)).findFirst();
        if (first.isPresent()) {
            NodeHtml currentNode = first.orElseThrow(() -> new BusinessException("没有找到节点"));
            log.info("执行{}节点", currentNode.getData().getName());
            try {
                Object req = null;
                ResultDto execute = execNode(currentNode.getData(), rule.getExecuteDto().getVariableMap(), req);
                //判断返回类型
                rule.getExecuteDto().getNodeResult().put(nodeId, execute);
                return execute.getValue();
            } catch (Exception e) {
                log.error("节点执行错误", e);
                throw new BusinessException("节点执行错误", currentNode.getData().getName());
            }
        } else {
            //如果执行节点也没有找到，直接返回空
            return null;
        }
    }

    /**
     * 根据线顺序执行节点，当线上有条件时，待上节点结果->条件判断后再执行下一个节点逻辑；各逻辑执行单元分别使用一个线程去处理；直到最后一个节点执行完成
     *
     * @return
     */
    public static HtmlGraph sequentialExecution(String str, RuleExecDto ruleExecDto) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        log.info("集成&自动化开始" + ruleExecDto.getSecret());

        log.info("此执行为:{}", ruleExecDto.getType().desc);

        log.info("运行参数:{}", ruleExecDto.getExecuteDto().getVariableMap());

        HtmlGraph graph = JSONObject.parseObject(str, HtmlGraph.class);
        if (ObjectNull.isNull(graph.getNodeList())) {
            return graph;
        }
        //将画布信息放到对象中，用于存储执行
        ruleExecDto.getGraph().setCanvasId("main");
        if (ObjectNull.isNotNull(ruleExecDto.getGraph().getErgodicCanvas())) {
            //兼容处理
            ruleExecDto.getGraph().getErgodicCanvas().keySet().forEach(e -> ruleExecDto.getGraph().getErgodicCanvas().get(e).setCanvasId(e));
        }
        log.info("解析数据格式");
        RuleSystemThreadLocal.set(ruleExecDto);
        sequentialExecution(graph, ruleExecDto.getExecuteDto());
        if (ObjectNull.isNull(ruleExecDto.getExecuteDto().getErrorNodeId())) {
            log.info("当前执行进度为:100%");
        } else {
            log.info("自动化集成执行有错误");
        }
        ruleExecDto.setGraph(graph);
        return graph;
    }

    public static void sequentialExecution(HtmlGraph graph, RuleExecuteDto executeDto) {
        //如果图为空,返回为空
        if (ObjectNull.isNull(graph)) {
            return;
        }
        if (ObjectNull.isNull(graph.getNodeList())) {
            return;
        }
        try {
            //是否有节点自定义情况，此情况，根据请求头的节点名称，直接寻找，找到直接执行，然后返回
            String nodeNameHeader = WebUtils.getRequest().getHeader("nodeName");
            if (ObjectNull.isNotNull(nodeNameHeader)) {
                Optional<NodeHtml> nodeNameOp = graph.getNodeList().stream().filter(e -> nodeNameHeader.equals(e.getName())).findFirst();
                if (nodeNameOp.isPresent()) {
                    doNext(graph.getLineList(), graph.getNodeList(), nodeNameOp.get(), executeDto);
                }
                //不管有没有，都直接返回
                return;
            }
        } catch (Exception e) {
            //如果是通过非http请求执行的，没有此参数
        }
        //1、开始节点出发，可以不管条件，可能多条线
        Optional<NodeHtml> first = graph.getNodeList().stream().filter(e -> e.getType().equals(NodeType.start)).findFirst();
        if (!first.isPresent()) {
            //子画布中没有开始,直接结束
            return;
        }
        NodeHtml startNode = first.get();
        ResultDto resultDto = new ResultDto().setValue(executeDto.getReqVariableMap());
        executeDto.getNodeResult().put(startNode.getId(), resultDto);
        //初始化
        if (ObjectNull.isNull(graph.getNodeList())) {
            return;
        }
        //将所有的节点都拿出来放到上下文中
        //获取逻辑执行参数
        doNext(graph.getLineList(), graph.getNodeList(), startNode, executeDto);
    }

    /**
     * 顺序执行逻辑节点
     *
     * @param edges       所有连线
     * @param nodes       所有节点
     * @param currentNode 当前节点
     * @param executeDto
     */
    public static void doNext(List<HtmlEdge> edges, List<NodeHtml> nodes, NodeHtml currentNode, RuleExecuteDto executeDto) {
        log.info("执行{}节点", currentNode.getName());
        // 不执行开始、结束节点
        if (!NO_EXECUTE_TYPES.contains(currentNode.getType())) {
            // 将正在执行的节点ID放进去
            executeDto.setExecNodeId(currentNode.getId());
            // 执行当前节点, 报错后忽略剩余节点
            ResultDto resultDto = new ResultDto();
            Map<String, String> parameterInDesc = currentNode.getData().getParameters().stream().collect(Collectors.toMap(HtmlParameters::getKey, HtmlParameters::getInfo));
            Object req = null;
            try {
                resultDto = execNode(currentNode.getData(), executeDto.getVariableMap(), req);
                //设置节点的名称
                resultDto.setFunctionName(currentNode.getData().getFunctionName());
                resultDto.setParameterInDesc(parameterInDesc);
                //执行变量绑定关系
                try {
                    //判断是否自定义结构
                    if (ObjectNull.isNotNull(currentNode.getData().getCustomStructure()) && currentNode.getData().getCustomStructure() && ObjectNull.isNotNull(currentNode.getData().getCustomStructureBody())) {
                        List<RuleElementVo> customStructureBody = currentNode.getData().getCustomStructureBody();
                        if (ObjectNull.isNotNull(customStructureBody)) {
                            //不处理
                        } else {
                            resultDto.setValue(resultDto.getValue());
                        }
                    } else {
                        //自动解析结构
                        Object value = resultDto.getValue();
                        String s = JSONObject.toJSONString(value);
                        List<RuleElementVo> elementVos = RuleElementUtils.get(s);
                        if (ObjectNull.isNotNull(elementVos)) {
                            //设置结构体
                            currentNode.getData().setCustomStructureBody(elementVos);
                        }
                    }
                } catch (Exception e) {
                    log.error("自定义结构解析错误{}", e.getMessage());
                    //如果自定义结构出错，不处理
                }
                if (ObjectNull.isNull(executeDto.getNodeResult())) {
                    executeDto.setNodeResult(new HashMap<>(8));
                }
                //将结果返回给参数
                executeDto.getNodeResult().put(currentNode.getId(), resultDto);
                if (currentNode.getType().equals(NodeType.task) && currentNode.getData().getFunctionName().equals(RuleConstant.FORSTR)) {
                    //特殊处理没有直接中断执行操作
                    return;
                }
                ResultDto o = executeDto.getNodeResult().get(currentNode.getId());
                log.info("正在寻找下级节点中", currentNode.getData().getName());
                executeDto.setEndResult(o);
                //将结果放入上下文中
                RuleSystemThreadLocal.getRule().getExecuteDto().getNodeResult().put(currentNode.getId(), o);
                //中止后，不再继续执行
                if (!RuleSystemThreadLocal.runStop()) {
                    if ("循环控制".equals(currentNode.getData().getFunctionName()) && Boolean.parseBoolean(resultDto.getValue().toString())) {
                        log.info("存在循环控制,退出循环");
                        //表示循环控制退出
                        return;
                    }
                    //寻找下一个可执行的节点操作
                    nextEdges(currentNode, executeDto, nodes, edges);
                }
            } catch (Exception e) {
                resultDto.setParameterInDesc(parameterInDesc);
                resultDto.setErrorMessage(e.getMessage());
                if (e instanceof RunParameterException) {
                    //如果参数类型不匹配，需要将错误信息返回给前端展示
                    resultDto.setParameterIn((((RunParameterException) e).getData()));
                }
                if (ObjectNull.isNull(executeDto.getNodeResult())) {
                    executeDto.setNodeResult(new HashMap<>(8));
                }
                executeDto.getNodeResult().put(currentNode.getId(), resultDto);
                if (ObjectNull.isNotNull(executeDto.getNodeResult()) && executeDto.getNodeResult().containsKey(currentNode.getId())) {
                    ResultDto o = executeDto.getNodeResult().get(currentNode.getId());
                    log.info("当前执行进度为:{}% ,正在寻找下级节点中", currentNode.getData().getName());
                    executeDto.setEndResult(o);
                    //将结果放入上下文中
                    RuleSystemThreadLocal.getRule().getExecuteDto().getNodeResult().put(currentNode.getId(), o);
                }
                log.error("节点{}执行错误,错误信息 :<br/> {}", currentNode.getData().getName(), StackTraceElementUtils.logThrowableToString(e, "<br/>"));
                if (e instanceof RuleException) {
                    executeDto.setException((RuleException) e);
                    executeDto.setErrorMessage(e.getMessage()).addErrorNodeId(currentNode.getId());
                } else if (e instanceof BusinessException) {
                    executeDto.setErrorMessage(e.getMessage()).addErrorNodeId(currentNode.getId());
                } else {
                    executeDto.setErrorMessage(StackTraceElementUtils.logThrowableToString(e, "<br/>")).addErrorNodeId(currentNode.getId());
                }
                //获取是否有异常线
                Optional<HtmlEdge> errorEdges = findErrorEdges(currentNode, edges);
                if (errorEdges.isPresent()) {
                    // 获取后续执行节点
                    NodeHtml nextNode = findNextNode(errorEdges.get().getTo(), nodes);
                    // 递归调用当前方法
                    doNext(edges, nodes, nextNode, executeDto);
                } else {
                    return;
                }
            }

        } else {
            if (currentNode.getType().equals(NodeType.task) && currentNode.getData().getFunctionName().equals(RuleConstant.FORSTR)) {
                //特殊处理没有直接中断执行操作
                return;
            }
            nextEdges(currentNode, executeDto, nodes, edges);
        }
    }

    /**
     * 根据相关信息，查询是否存在异常节点
     *
     * @param currentNode
     * @param edges
     * @return
     */
    private static Optional<HtmlEdge> findErrorEdges(NodeHtml currentNode, List<HtmlEdge> edges) {
        return edges.stream().filter(e -> e.getFrom().equals(currentNode.getId())).filter(e -> "abnormal".equals(e.getState())).findFirst();
    }

    /**
     * 执行节点的连线
     */
    public static void nextEdges(NodeHtml currentNode, RuleExecuteDto executeDto, List<NodeHtml> nodes, List<HtmlEdge> edges) {
        // 获取后续所有连线集合
        List<HtmlEdge> nextEdgeList = findEdges(currentNode, edges);
        //过滤所有异步线的
        List<HtmlEdge> collect = nextEdgeList.stream().filter(e -> "async".equals(e.getState())).collect(Collectors.toList());
        //过滤所有同步线的
        nextEdgeList.removeAll(collect);
        //如果存在异步线，则直接执行
        if (ObjectNull.isNotNull(collect)) {
            log.info("执行异步线操作");
            AsyncService bean = SpringContextUtil.getBean(AsyncService.class);
            List<CompletableFuture<RuleExecuteDto>> futureList = new ArrayList<>();
            collect.forEach(e -> {
                try {
                    boolean isEdgeAllowed = execEdges(e, executeDto);
                    if (isEdgeAllowed) {
                        executeDto.getLines().add(e.getId());
                    }
                    if (isEdgeAllowed) {
                        // 获取后续执行节点
                        NodeHtml nextNode = findNextNode(e.getTo(), nodes);
                        if (ObjectUtil.isEmpty(nextNode)) {
                            return;
                        }
                        //逻辑的开始节点直接通过消息节点结束，并带有排序顺序执行时直接返回
                        bean.async(edges, nodes, nextNode, executeDto, futureList);
                    }
                } catch (Exception v) {
                    log.error("连线{}执行错误,错误信息 :<br/> ", StackTraceElementUtils.logThrowableToString(v, "<br/>"));
                    executeDto.setErrorMessage(v.getMessage()).addErrorNodeId(e.getId());
                }
            });
            // 声明结束点，结束点向上找，找到的线，肯定数量和点的数量保持一致，要么都没有结束点，要么结束点向上找，只能找到一个。
            futureList.parallelStream().map(CompletableFuture::join).forEach(e -> executeDto.getNodeResult().putAll(e.getNodeResult()));
            //查询找最近一个聚合节点，如果存在，继续向下执行，如果没有直接结束
            List<HtmlEdge> list = lookingDownLine("同步线聚合", currentNode, nodes, edges);
            if (ObjectNull.isNotNull(list)) {
                nextEdgeList.addAll(list);
            }
        }

        if (ObjectNull.isNull(nextEdgeList)) {
            log.info("没有找到下级节点数据");
        }

        for (HtmlEdge e : nextEdgeList) {
            //如果为异常线，不处理
            if ("abnormal".equals(e.getState())) {
                log.info("当前线为异常线直接退出", e.getId());
                continue;
            }
            // 判断是否执行节点
            boolean isEdgeAllowed;
            try {
                isEdgeAllowed = execEdges(e, executeDto);
                if (isEdgeAllowed) {
                    executeDto.getLines().add(e.getId());
                }
            } catch (Exception v) {
                log.error("连线{}执行错误,错误信息 :<br/> ", StackTraceElementUtils.logThrowableToString(v, "<br/>"));
                executeDto.setErrorMessage(v.getMessage()).addErrorNodeId(e.getId());
                return;
            }
            if (isEdgeAllowed) {
                // 获取后续执行节点
                NodeHtml nextNode = findNextNode(e.getTo(), nodes);
                if (ObjectUtil.isEmpty(nextNode)) {
                    continue;
                }
                //todo 如果是遇到了同步节点，直接停止。停止后此节点将不再向下执行。
                if (nextNode.getName().equals("同步线聚合")) {
                    return;
                }
                // 递归调用当前方法
                doNext(edges, nodes, nextNode, executeDto);
                //逻辑的开始节点直接通过消息节点结束，并带有排序顺序执行时直接返回
                try {
                    //如果最后一个节点是消息节点代表程序中止， 则直接中止程序
                    //中止后，不再继续执行
                    if ("循环控制".equals(executeDto.getEndResult().getFunctionName()) && Boolean.parseBoolean(executeDto.getEndResult().getValue().toString())) {
                        //表示循环控制退出
                        break;
                    }
                    if ("提示消息".equals(RuleSystemThreadLocal.getRule().getExecuteDto().getEndResult().getFunctionName())) {
                        //表示程序中止
                        return;
                    }
                } catch (Exception ex) {
                    //异步线程会出现问题
                }
            }
        }
    }

    /**
     * 根据当前节点向下寻找同步线聚合 节点以下的所有线
     *
     * @param name        节点名称
     * @param currentNode 当前节点
     * @param nodes
     * @param edges       所有线
     */
    private static List<HtmlEdge> lookingDownLine(String name, NodeHtml currentNode, List<NodeHtml> nodes, List<HtmlEdge> edges) {
        //先判断里面是否存在如果存在，再去向下寻找。但也可能 是连线错误.
        if (nodes.stream().noneMatch(e -> e.getName().equals(name))) {
            return null;
        }
        //根据当前节点向下寻找
        List<HtmlEdge> edges1 = findEdges(currentNode, edges);
        for (HtmlEdge htmlEdge : edges1) {
            Set<NodeHtml> node = nodes.stream().filter(a -> a.getId().equals(htmlEdge.getTo())).collect(Collectors.toSet());
            for (NodeHtml nodeHtml : node) {
                //如果名称匹配，则直接返回
                if (nodeHtml.getName().equals(name)) {
                    //寻找节点信息，并返回这个节点下级数据
                    return edges.stream().filter(e -> e.getFrom().equals(nodeHtml.getId())).collect(Collectors.toList());
                } else {
                    return lookingDownLine(name, nodeHtml, nodes, edges);
                }
            }
        }
        return edges;
    }

    /**
     * 计算连线结果
     *
     * @param edge       当前连线
     * @param executeDto
     * @return 计算结果: 满足条件与否
     */
    private static boolean execEdges(HtmlEdge edge, RuleExecuteDto executeDto) {
        log.info("连线中没有找到判断公式,不执行");
        //如果不是正确返回， 则直接错误
        //获取公式
        String formulaContent = edge.getFormulaContent();
        List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(formulaContent);
        if (expressionParams.isEmpty()) {
            return true;
        }
        Object calculate = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expressionParams, executeDto.getVariableMap(), "RULE");
        log.info("连线返回值,{}", calculate);
        if (calculate instanceof Boolean) {
            return (boolean) calculate;
        }
        throw new RuleException(设计错误, "连线返回类型不是布尔类型,请重新设计");
    }

    /**
     * 获取当前节点后续连线
     *
     * @param node  当前节点
     * @param edges 所有连线集合
     * @return 后续连线集合
     */
    private static List<HtmlEdge> findEdges(NodeHtml node, List<HtmlEdge> edges) {
        return edges.stream().filter(e -> e.getFrom().equals(node.getId())).sorted(Comparator.comparingInt(HtmlEdge::getSort)).collect(Collectors.toList());
    }

    private static NodeHtml findNextNode(String name, List<NodeHtml> nodes) {
        return nodes.stream().filter(e -> e.getId().equals(name)).findAny().orElse(null);
    }


    /**
     * 生成当前点的代码
     *
     * @param node        当前节点
     * @param variableMap
     * @return 运行结果, 正常为false, 报错为true
     */
    @SneakyThrows
    public static ResultDto execNode(HtmlData node, Map<String, Object> variableMap, Object req) {
        long st = System.currentTimeMillis();
        Object execute;
        Map<String, Object> varMap = null;
        //动态条件表达式
        //子线程，上下文
        RuleFunctionDto functions = SystemInit.getFunctionsBase(node.getFunctionName());
        ExternalService externalService = SpringContextUtil.getBean(ExternalService.class);
        if (ObjectNull.isNull(functions)) {
            //判断数据库里面是否存在
            externalService.checkName(node.getFunctionName(), node.getGroup());
            execute = businessRule(node.getFunctionName(), node, variableMap);
        } else {
            //获取方法的具体接口
            BaseCustomFunctionInterface bean = SpringContextUtil.getApplicationContext().getBeansOfType(BaseCustomFunctionInterface.class).get(functions.getFunctionName());
            //变量的优先级，1默认值，2：公式，3：运行过程中的值
            //生成节点的执行逻辑
            //使用变量的值进行替换
            Map<String, Object> body;
            if (ObjectNull.isNull(node.getBody())) {
                body = new HashMap<>(4);
                for (RuleFunctionDtoParameter parameter : functions.getParameters()) {
                    String key = parameter.getKey();
                    if (inputTypeTransformInterfaceMap.containsKey(parameter.getInputType())) {
                        //特殊类型转换
                        inputTypeTransformInterfaceMap.get(parameter.getInputType()).transform(parameter.getKey(), body, body.get(parameter.getKey()), variableMap, "RULE");
                    } else {
                        body.put(key, parameter.getDefaultValue());
                    }
                }
            } else {
                //不能直接重新覆盖
                body = new HashMap<>(node.getBody());
            }
            body = expressionExec(node.getParameters(), body, variableMap);
            //清空 value为空的数据
            bean.removeKey(body);

            if (ObjectNull.isNotNull(functions) && ObjectNull.isNotNull(functions.getParameterClass())) {
                //转成对象
                Class<?> cls;
                try {
                    cls = Class.forName(functions.getParameterClass());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Map<String, ParameterValue> parameterMap = SystemInit.getParameterMap(cls);
                //判断是否存在文件类型，并对单文件类型的数据进行特殊处理转换
                for (String key : parameterMap.keySet()) {
                    ParameterValue parameterValue = parameterMap.get(key);
                    if (InputType.file.equals(parameterValue.type()) && body.containsKey(key)) {
                        Object o = body.get(key);
                        if (o instanceof List) {
                            //表示多个数据类型,只取第一个
                            if (((List<?>) o).size() > 0) {
                                body.put(key, ((List<?>) o).get(0));
                            } else {
                                //清楚空文件
                                body.remove(key);
                            }
                        } else {
                            throw new RuleException(设计错误, parameterValue.info() + "正确属性类型为文件错误值为:" + o + "，类型为:" + o.getClass().getSimpleName());
                        }
                    } else if ((InputType.list.equals(parameterValue.type())
                            || InputType.listMap.equals(parameterValue.type())
                            || InputType.userList.equals(parameterValue.type())
                            || InputType.multipleSelected.equals(parameterValue.type()))
                            && body.containsKey(key) && !(body.get(key) instanceof List)) {
                        //如果为多选，用户通过公式处理时，返回的不是数组，将数据改变成数组存放进去
                        if (!"遍历数据".equals(parameterValue.info())) {
                            body.put(key, Collections.singletonList(body.get(key)));
                        }
                    } else if (ObjectNull.isNotNull(parameterValue.defaultValue()) && !body.containsKey(key)) {
                        body.put(key, parameterValue.defaultValue());
                    }
                }
                try {
                    req = BeanCopyUtil.copy(cls, body);
                } catch (Exception e) {
                    //将入参和参数处理成一样
                    Set<String> bodyKey = body.keySet();
                    for (String key : bodyKey) {
                        Object o = body.get(key);
                        Class<?> aClass = o.getClass();
                        ParameterValue parameterValue = parameterMap.get(key);
                        if (ObjectNull.isNotNull(parameterValue)) {
                            if (!(ClassUtil.isAssignable(parameterValue.type().getDataClass(), aClass))) {
                                Class<?> dataClass = parameterValue.type().getDataClass();
                                throw new RuleException(设计错误, parameterValue.info() + "正确属性类型为" + ClassType.value(dataClass).name() + "。错误值为:" + o + "，类型为:" + aClass.getSimpleName());
                            }
                        }
                    }
                }
                //判断是否需要自行校验
                if (functions.getInspect()) {
                    //自行校验
                    try {
                        bean.inspect(req);
                    } catch (Exception e) {
                        log.error(functions.getFunctionName() + "参数校验失败");
                        throw new RuleException(设计错误, e.getMessage());
                    }
                } else {
                    //框架校验
                    try {
                        BeanValidator.validatorException(req);
                    } catch (Exception e) {
                        log.error(functions.getFunctionName() + "参数校验失败");
                        throw new RuleException(设计错误, e.getMessage());
                    }
                }

                log.info("执行{}逻辑模块,参数 {}", functions.getFunctionName(), JSONObject.toJSONString(req));
                //判断是否是绑定类型如果是执行变量绑定关系
                //执行变量绑定关系值
                List<BindBaseDto> bind = new ArrayList<>();
                if (req instanceof BindDto) {
                    if (ObjectNull.isNotNull(((BindDto) req).getAssignmentBind())) {
                        bind.addAll(((BindDto) req).getAssignmentBind());
                    }
                    if (ObjectNull.isNotNull(((BindDto) req).getBind())) {
                        bind.addAll(((BindDto) req).getBind());
                    }

                    //将绑定入参设置为空,避免请求参数也设置进去
                    ((BindDto) req).setBind(null);
                }
                try {
                    execute = bean.execute(req, variableMap);
                } catch (Exception e) {
                    log.error("节点执行异常", e);
                    throw new RunParameterException(e.getMessage(), body);
                }
                if (ObjectNull.isNotNull(bind)) {
                    if (!bind.isEmpty()) {
                        varMap = varBind(bind);
                    }
                }
            } else {
                execute = bean.execute("", variableMap);
            }
        }
        long end = System.currentTimeMillis();
        //解析返回数据
        ResultDto dto = instanceofRule(execute, node.getReturnType()).setTime(end - st);
        dto.setClassType(node.getReturnType());
        if (ObjectNull.isNotNull(req)) {
            dto.setParameterIn(JSONObject.parseObject(JSONObject.toJSONString(req)));
        }
        if (ObjectNull.isNotNull(varMap)) {
            //设置绑定变量
            dto.setParameter(varMap);
        }
        return dto;
    }

    /**
     * 节点执行完成后,执行变量绑定,修改原始变量的值
     *
     * @param bind 需要绑定的字段
     * @return
     */
    private static Map<String, Object> varBind(List<BindBaseDto> bind) {
        //如果是公式 .则直接判断公式逻辑执行
        Map<String, Object> data = new HashMap<>(1);
        for (BindBaseDto dto : bind) {
            if (ObjectNull.isNull(dto.getBindType())) {
                execBind(dto.getParams(), data);
                continue;
            }
            switch (dto.getBindType()) {
                case formula:
                    if (ObjectNull.isNotNull(dto.getFormulaContent())) {
                        String trim = dto.getFormulaContent().trim();
                        if (ObjectNull.isNotNull(trim)) {
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(trim, data, "RULE");
                            if (Boolean.valueOf(result.toString())) {
                                //触发更新值
                                execBind(dto.getParams(), data);
                            }
                        }
                    } else {
                        //如果公式为空也继续执行
                        execBind(dto.getParams(), data);
                    }
                    break;
                case condition:
                    boolean present = true;
                    if (ObjectNull.isNotNull(dto.getConditions())) {
                        present = dto.getConditions().stream().filter(e -> {
                            //如果有一个不通过则不通过
                            return filterBind(e, data);
                        }).findFirst().isPresent();
                    }
                    if (present) {
                        execBind(dto.getParams(), data);
                    }
                default:
                    break;
            }
        }
        return data;
    }

    /**
     * 过滤绑定逻辑
     *
     * @param e
     * @param data
     * @return
     */
    private static boolean filterBind(BindConditionsDto e, Map<String, Object> data) {

        String fieldKey = e.getFieldKey();
        int i = fieldKey.indexOf("©");
        String s = fieldKey.substring(0, i);
        Map<String, ResultDto> nodeResult = RuleSystemThreadLocal.getRule().getExecuteDto().getNodeResult();
        Object read = JvsJsonPath.read((nodeResult.get(s).getValue()), fieldKey.substring(i + 1).replaceAll("\\*", "."));
        if (ObjectNull.isNull(e.getEnabledQueryTypes())) {
            e.setEnabledQueryTypes(DataQueryType.eq);
        }
        //判断类型
        switch (e.getProp()) {
            case value:
                return coverage(e.getEnabledQueryTypes(), read, e.getValue());
            case empty:
                return ObjectNull.isNull(read);
            case condition:
                return coverage(e.getEnabledQueryTypes(), read.toString(), e.getValue());
            case formula:
                Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(e.getFormulaContent(), data, "RULE");
                return coverage(e.getEnabledQueryTypes(), read, result);
            default:
                return false;

        }
    }

    /**
     * 对比数据的两个值是否满足条件
     *
     * @param type  条件
     * @param read  数据值
     * @param value 比对值
     * @return true|false
     */
    private static boolean coverage(DataQueryType type, Object read, Object value) {
        switch (type) {
            case isNull:
                return ObjectNull.isNull(read);
            case eq:
                return read.equals(value);
            case ne:
                return !read.equals(value);
            case lt:
                if (read instanceof Number && value instanceof Number) {
                    return ((Number) read).doubleValue() < ((Number) value).doubleValue();
                } else {
                    throw new BusinessException("赋值绑定比对类型不匹配");
                }
            case ge:
                if (read instanceof Number && value instanceof Number) {
                    return ((Number) read).doubleValue() >= ((Number) value).doubleValue();
                } else {
                    throw new BusinessException("赋值绑定比对类型不匹配");
                }
            case le:
                if (read instanceof Number && value instanceof Number) {
                    return ((Number) read).doubleValue() <= ((Number) value).doubleValue();
                } else {
                    throw new BusinessException("赋值绑定比对类型不匹配");
                }
            case gt:
                if (read instanceof Number && value instanceof Number) {
                    return ((Number) read).doubleValue() > ((Number) value).doubleValue();
                } else {
                    throw new BusinessException("赋值绑定比对类型不匹配");
                }
            case in:
                if (read instanceof List && value instanceof List) {
                    return new HashSet<>((Collection) read).containsAll((Collection<?>) value);
                } else if (read instanceof List && !(value instanceof List)) {
                    return ((List<?>) read).contains(value);
                } else if (read instanceof String) {
                    return ((String) read).contains(value.toString());
                }
            case notIn:
                if (read instanceof List && value instanceof List) {
                    return !new HashSet<>((Collection) read).containsAll((Collection<?>) value);
                } else if (read instanceof List && !(value instanceof List)) {
                    return !((List<?>) read).contains(value);
                } else if (read instanceof String) {
                    return !((String) read).contains(value.toString());
                }

            case like:
            case allin:
            case between:

        }
        return false;
    }

    /**
     * 执行绑定数据
     *
     * @param params 赋值参数
     * @param data   具体哪些数据处理逻辑有值
     */
    private static void execBind(List<BindBaseBodyDto> params, Map<String, Object> data) {
        for (BindBaseBodyDto param : params) {
            String fieldKey = param.getFieldKey();
            if (fieldKey.contains("globalvariable")) {
                switch (param.getProp()) {
                    case empty:
                        RuleSystemThreadLocal.setGlobalVariableValue(fieldKey, null);
                        break;
                    case field:
                        //将字段包装为公式进行执行
                        Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getValue() + "}", data, "RULE");
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey() + "}", data, "RULE");
                            Object o = RuleSystemThreadLocal.getGlobalVariableValue(fieldKey);
                            if (o instanceof Map) {
                                RuleSystemThreadLocal.setGlobalVariableValue(fieldKey + "." + key.toString(), obj);
                            } else {
                                throw new BusinessException("被赋值字段需要是一个对象，请检查被赋值字段类型");
                            }
                        } else {
                            RuleSystemThreadLocal.setGlobalVariableValue(fieldKey, obj);
                        }
                        break;
                    case value:
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey() + "}", data, "RULE");
                            Object o = RuleSystemThreadLocal.getGlobalVariableValue(fieldKey);
                            if (o instanceof Map) {
                                RuleSystemThreadLocal.setGlobalVariableValue(fieldKey + "." + key.toString(), param.getValue());
                            } else {
                                throw new BusinessException("被赋值字段需要是一个对象，请检查被赋值字段类型");
                            }
                        } else {
                            //设置全局变量值
                            RuleSystemThreadLocal.setGlobalVariableValue(fieldKey, param.getValue());
                        }
                        break;
                    case formula:
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey() + "}", data, "RULE");
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(param.getFormulaContent(), data, "RULE");
                            Object o = RuleSystemThreadLocal.getGlobalVariableValue(fieldKey);
                            if (o instanceof Map) {
                                RuleSystemThreadLocal.setGlobalVariableValue(fieldKey + "." + key.toString(), result);
                            } else {
                                throw new BusinessException("被赋值字段需要是一个对象，请检查被赋值字段类型");
                            }
                        } else {
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(param.getFormulaContent(), data, "RULE");
                            RuleSystemThreadLocal.setGlobalVariableValue(fieldKey, result);
                        }
                        break;
                    default:
                }

            } else {
                int i = fieldKey.indexOf("©");
                String s = fieldKey;
                Map<String, ResultDto> nodeResult = RuleSystemThreadLocal.getRule().getExecuteDto().getNodeResult();
                String execCanvasId = RuleSystemThreadLocal.getRule().getExecuteDto().getExecCanvasId();

                //保证不是直接的对当前画布副职
                if (i == -1 && !nodeResult.containsKey(fieldKey)) {
                    //进行重置
                    i = 0;
                } else if (i != -1) {
                    s = fieldKey.substring(0, i);
                }
                //判断是否是当前画布
                if (i == -1) {
                    //表示第一层
                    if (param.getProp().equals(LinkTypeEnum.empty)) {
                        nodeResult.get(s).setValue(null);
                    }
                    if (param.getProp().equals(LinkTypeEnum.value)) {
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                            JSONPath.set(nodeResult.get(s).getValue(), key.toString(), param.getValue());
                        } else {
                            nodeResult.get(s).setValue(param.getValue());
                        }
                    }
                    if (param.getProp().equals(LinkTypeEnum.field)) {
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                            Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getValue().toString() + "}", data, "RULE");
                            JSONPath.set(nodeResult.get(s).getValue(), key.toString(), obj);
                        } else {
                            Object obj = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getValue().toString() + "}", data, "RULE");
                            nodeResult.get(s).setValue(obj);
                        }
                    }
                    if (param.getProp().equals(LinkTypeEnum.formula)) {
                        if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                            Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(param.getFormulaContent(), data, "RULE");
                            JSONPath.set(nodeResult.get(s).getValue(), key.toString(), result);
                        } else {
                            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(param.getFormulaContent(), data, "RULE");
                            nodeResult.get(s).setValue(result);
                        }
                    }
                } else {
                    if (ObjectNull.isNull(execCanvasId) && i == -1) {
                        s = "start";
                    } else if (ObjectNull.isNull(execCanvasId) && i != -1) {
                        s = fieldKey.substring(0, i);
                    }
                    if (param.getProp().equals(LinkTypeEnum.empty)) {
                        if (fieldKey.contains("©ergodic©")) {
                            String[] split = fieldKey.split("©ergodic©");
                            JSONPath.remove(nodeResult.get(split[0]).getValue(), split[1].replaceAll("\\*", "."));
                        } else {
                            JSONPath.remove(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."));
                        }
                    }
                    if (param.getProp().equals(LinkTypeEnum.field)) {
                        int i1 = param.getValue().toString().indexOf("©");
                        Object result;

                        if (i1 == -1) {
                            //表示开始
                            if (nodeResult.containsKey(param.getValue().toString())) {
                                //表示获取节点的值
                                ResultDto resultDto = nodeResult.get(param.getValue().toString());
                                result = resultDto.getValue();
                            } else {
                                //表示直接取值
                                ResultDto resultDto = nodeResult.get("start");
                                result = JvsJsonPath.read(resultDto.getValue(), param.getValue().toString());
                            }
                        } else {
                            result = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getValue().toString() + "}", data, "RULE");
                        }
                        if (fieldKey.contains("©ergodic©")) {
                            String[] split = fieldKey.split("©");
                            if (split.length == 3) {
                                JSONPath.set(nodeResult.get(split[0]).getValue(), split[2], result);
                            }
                        } else if (s.startsWith("start")) {
                            if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", ".") + "." + key.toString(), result);
                            } else {
                                JSONPath.set(nodeResult.get("start").getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."), result);
                            }
                        } else {
                            if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", ".") + "." + key.toString(), result);
                            } else {
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."), result);
                            }
                        }
                    }
                    if (param.getProp().equals(LinkTypeEnum.value)) {
                        if (fieldKey.contains("©ergodic©")) {
                            String[] split = fieldKey.split("©");
                            if (split.length == 3) {
                                if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                    Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                    JSONPath.set(nodeResult.get(split[0]).getValue(), split[2] + "." + key, param.getValue());
                                } else {
                                    JSONPath.set(nodeResult.get(split[0]).getValue(), split[2], param.getValue());
                                }
                            }
                        } else if (nodeResult.containsKey(s)) {
                            if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", ".") + "." + key.toString(), param.getValue());
                            } else {
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."), param.getValue());
                            }
                        }
                    }
                    if (param.getProp().equals(LinkTypeEnum.formula)) {
                        Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(param.getFormulaContent(), data, "RULE");
                        //通过公式获取数据值，判断是否第一层
                        if ("start".equals(s)) {
                            if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", ".") + "." + key.toString(), result);
                                data.put(fieldKey, result);
                            } else {
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."), result);
                                data.put(fieldKey, result);
                            }
                        } else if (fieldKey.contains("©ergodic©")) {
                            String[] split = fieldKey.split("©");
                            if (split.length == 3) {
                                if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                    Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                    JSONPath.set(nodeResult.get(split[0]).getValue(), split[2] + "." + key.toString(), result);
                                } else {
                                    if (!nodeResult.get(split[0]).getValue().equals(result)) {
                                        JSONPath.set(nodeResult.get(split[0]).getValue(), split[2], result);
                                    }
                                }
                            }
                        } else {
                            if (ObjectNull.isNotNull(param.getFieldDynamicKey())) {
                                Object key = SpringContextUtil.getBean(ExpressionHandler.class).calculate("${RULE" + param.getFieldDynamicKey().toString() + "}", data, "RULE");
                                JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", ".") + "." + key.toString(), result);
                                data.put(fieldKey, result);
                            } else {
                                if (ObjectNull.isNull(s)) {
                                    nodeResult.get(fieldKey).setValue(result);
                                    data.put(fieldKey, result);
                                } else {
                                    JSONPath.set(nodeResult.get(s).getValue(), fieldKey.substring(i + 1).replaceAll("\\*", "."), result);
                                    data.put(fieldKey, result);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取这个值的变量值
     *
     * @return
     */
    private static Object getValue() {
        return null;
    }

    /**
     * 扩展类型操作
     *
     * @param node
     * @param variableMap
     * @return
     */
    private static Object businessRule(String functionName, HtmlData node, Map<String, Object> variableMap) {
        ExternalService bean = SpringContextUtil.getBean(ExternalService.class);
        Map<String, HtmlParameters> collect =
                node.getParameters().stream().filter(e -> ObjectNull.isNotNull(e.getFormulaContent()) && !e.getFormulaContent().trim().isEmpty()).collect(Collectors.toMap(RuleFunctionDtoParameter::getKey,
                        Function.identity()));
        Function<String, Object> expressionExecFunction = key -> {
            if (!collect.containsKey(key)) {
                return null;
            }
            HtmlParameters parameters = collect.get(key);
            String expression = parameters.getFormulaContent();
            List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
            if (expressionParams.isEmpty()) {
                return null;
            }
            //处理特殊符
            Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, variableMap, "RULE");
            if (ObjectNull.isNull(result)) {
                SpringContextUtil.getBean(FunctionBusinessService.class).checkType(parameters.getFormula(), result, parameters.getExplain() + "不能为空");
            }
            return result;
        };
        if (ObjectNull.isNotNull(node.getBody())) {
            variableMap.putAll(node.getBody());
        }
        return bean.execute(functionName, node.getGroup(), variableMap, expressionExecFunction);
    }

    /**
     * 执行公式解析某一个节点的值
     *
     * @param parameters
     * @param variableMap
     * @return
     */
    public static Map<String, Object> expressionExec(List<HtmlParameters> parameters, Map<String, Object> body, Map<String, Object> variableMap) {
        for (HtmlParameters parameter : parameters) {
            //判断是否支持替换,只有支持公式的字段才能被替换
            if (parameter.getInputType().getExtend()) {
                //不管是不是同名，不给替换处理。
//                body.put(parameter.getKey(), variableMap.getOrDefault(parameter.getKey(), body.get(parameter.getKey())));
            }
            //如果有公式再执行公式
            String formulaContent = parameter.getFormulaContent();
            if (ObjectNull.isNotNull(formulaContent) && !formulaContent.trim().isEmpty()) {
                String expression = parameter.getFormulaContent();
                List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                if (expressionParams.isEmpty()) {
                    continue;
                }
                //处理特殊符
                Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, variableMap, "RULE");
                if (ObjectNull.isNull(result)) {
                    SpringContextUtil.getBean(FunctionBusinessService.class).checkType(parameter.getFormula(), result, parameter.getExplain() + "不能为空");
                }
                body.put(parameter.getKey(), result);
            } else {
                //特殊类型转换
                if (inputTypeTransformInterfaceMap.containsKey(parameter.getInputType())) {
                    //转换
                    inputTypeTransformInterfaceMap.get(parameter.getInputType()).transform(parameter.getKey(), body, body.get(parameter.getKey()), variableMap, "RULE");
                }
            }
        }
        return body;
    }

    public static ResultDto instanceofRule(Object o, ClassType returnType) {
        if (o instanceof Boolean) {
            return new ResultDto().setValue(o).setType(InputType.number);
        }
        if (ObjectNull.isNull(returnType)) {
            return new ResultDto().setValue(o).setType(InputType.text);
        }
        if (returnType.equals(ClassType.未识别)) {

            if (o instanceof Number) {
                return new ResultDto().setValue(o).setType(InputType.number);
            }
            if (o instanceof String) {
                if (JSON.isValidObject(String.valueOf(o)) || JSON.isValidArray(String.valueOf(o))) {
                    return new ResultDto().setValue(JSONObject.parse(String.valueOf(o))).setType(InputType.json);
                }
                return new ResultDto().setValue(o).setType(InputType.text);
            }
            return new ResultDto().setValue(o).setType(InputType.json);
        } else {
            ResultDto resultDto = new ResultDto().setValue(o);
            if (o instanceof Number) {
                return resultDto.setType(InputType.number);
            } else {
                switch (returnType) {
                    case 对象:
                    case 数组:
                        return resultDto.setType(InputType.json);
                    case 布尔:
                    case 数字:
                        return resultDto.setType(InputType.number);
                    case 时间:
                    case 文本:
                    case 文件:
                    case 未识别:
                    case 浮点数:
                    default:
                        return resultDto.setType(InputType.text);
                }
            }
        }
    }

}
