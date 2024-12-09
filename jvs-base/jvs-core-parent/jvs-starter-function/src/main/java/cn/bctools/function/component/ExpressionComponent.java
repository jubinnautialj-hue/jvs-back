package cn.bctools.function.component;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.graph.ExpressionGraph;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.function.utils.ExpressionGraphUtils;
import cn.bctools.function.utils.ExpressionUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Component
@Slf4j
@AllArgsConstructor
public class ExpressionComponent {

    ExpressionHandler handler;
    FunctionBusinessService functionBusinessService;
    static final String TABLE_FORM = "tableForm";
    static final String TAB_FORM = "tab";

    public void getExpression(String designId, String useCase, ExecDto body) {

        List<FunctionBusinessPo> functionList = getFunctionList(designId, useCase);
        TableType type = SystemThreadLocal.get("tableType");

        //表示表格内触发点, 判断是否有父级
        if (ObjectNull.isNotNull(type) && ObjectNull.isNotNull(body.getParentKey())) {
            //表示选项卡,或表格新增,此时没有行级操作
            //需要过滤数据
            //需要根据父级进行过滤
            String parentKeyPath = body.getParentKey().stream().collect(Collectors.joining("."));
            // 获取该设计下所有的表达式
            if (ObjectNull.isNotNull(type)) {
                //判断表格行级操作。如果是添加，删除，则只过滤对应的公式进行执行，否则不执行
                switch (type) {
                    case add:
                    case line:
                        functionList = functionList.stream()
                                //如果操作触发点,不是表格, 就不做父级过滤，父级的表格操作也需要在计算
                                .filter(e -> e.getBusinessId().contains(parentKeyPath) || JSON.toJSONString(e.getRelatedIds()).contains(parentKeyPath))
                                .collect(Collectors.toList());
                        break;
                    case del:
                        functionList = functionList.stream()
                                //如果操作触发点,不是表格, 就不做父级过滤
                                .filter(e -> !e.getBusinessId().contains(parentKeyPath + "."))
                                .collect(Collectors.toList());
                        break;

                }

            }
        }

        if (ObjectNull.isNotNull(functionList)) {
            Object designSkips = SystemThreadLocal.get("designSkip");
            if (ObjectNull.isNotNull(designSkips)) {
                //如果是初始化直接返回
                if (designSkips.equals(true)) {
                    //过滤公式 没有关联字段的优先处理
                    List<FunctionBusinessPo> collect = functionList.stream().filter(e -> ObjectNull.isNull(e.getRelatedIds())).collect(Collectors.toList());
                    functionList = functionList.stream()
                            .filter(e -> ObjectNull.isNotNull(e.getRelatedIds()))
                            //必须全是sys初始化的时候，则才执行如果不是则不执行
                            .filter(e -> e.getRelatedIds().stream().filter(s -> s.startsWith("SYS")).count() == e.getRelatedIds().size())
                            .collect(Collectors.toList());
                    if (ObjectNull.isNotNull(collect)) {
                        functionList.addAll(collect);
                    }
                }
            }
            Map<String, FunctionBusinessPo> functionMap = getFunctionMap(functionList, body);
            // 构建变量引用的关系图
            ExpressionGraph<String> graph = ExpressionGraphUtils.buildFunctionGraph(functionList);
            // 计算各个参数, 以远距离优先搜索为顺序
            List<String> paramBusinessIds = ExpressionGraphUtils.getCalculateSort(graph, body.getModifiedField());
            if (ObjectNull.isNotNull(paramBusinessIds)) {
                Set<String> nextKeySet = getModifiedParams(body, useCase, paramBusinessIds, functionMap);
                if (designSkips.equals(true)) {
                    //只执行一次，直接跳过
                    return;
                }
                if (ObjectNull.isNotNull(nextKeySet)) {
                    SystemThreadLocal.remove("index");
                    for (String nextKey : nextKeySet) {
                        //查询出上级要触发的公式  这里必须要复制一个新的，下级
                        Map<String, FunctionBusinessPo> finalFunctionMap = new HashMap<>(functionMap);
                        {
                            Map<String, FunctionBusinessPo> collect = functionList.stream().filter(e -> e.getRelatedIds().contains(nextKey))
                                    .filter(e -> finalFunctionMap.containsKey(e.getBusinessId()))
                                    .collect(Collectors.toMap(FunctionBusinessPo::getBusinessId, Function.identity()));
                            //如果有完全匹配的，就使用完全匹配
                            if (ObjectNull.isNotNull(collect.keySet())) {
                                functionMap = collect;
                            } else {
                                //重新使用开始匹配方式
                                functionMap = functionList.stream().filter(e -> {
                                            List<String> relatedIds = e.getRelatedIds();
                                            for (String relatedId : relatedIds) {
                                                if (relatedId.equals(nextKey)) {
                                                    return true;
                                                }
                                            }
                                            return relatedIds.stream().filter(v -> v.startsWith(nextKey)).findFirst().isPresent();
                                        })
                                        .filter(e -> finalFunctionMap.containsKey(e.getBusinessId()))
                                        .collect(Collectors.toMap(FunctionBusinessPo::getBusinessId, Function.identity()));
                                //这里做两次循环，是因为可能存在 tab 多次操作
                                for (FunctionBusinessPo e : functionList) {
                                    if (ObjectNull.isNull(e.getParentType())) {
                                        List<String> relatedIds = e.getRelatedIds();
                                        for (String relatedId : relatedIds) {
                                            if (functionMap.containsKey(relatedId)) {
                                                functionMap.put(e.getBusinessId(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        getModifiedParams(body.setModifiedField(nextKey).setIndex(null).setParentKey(new ArrayList<>()), useCase, paramBusinessIds, functionMap);
                        //根据距离下向计算
                        if (paramBusinessIds.indexOf(nextKey) >= 0) {
                            for (int i = paramBusinessIds.indexOf(nextKey); i < paramBusinessIds.size(); i++) {
                                String e = paramBusinessIds.get(i);
                                body.setParentKey(null);
                                getModifiedParams(body.setModifiedField(e).setIndex(null), useCase, paramBusinessIds, functionMap);
                            }
                        }
                    }
                }
            }
        }
        //触发同级公式
//        String mid = body.getParentKey().get(body.getParentKey().size());
//        body.getParentKey().remove(body.getParentKey().size());
//        getModifiedParams(body.setModifiedField(mid).setIndex(null), useCase, paramBusinessIds, functionMap);
//
//        getModifiedParams(designId, useCase, new ExecDto().setIndex(null).setParams(body.getParams()).setParentKey(body.getParentKey()).setModifiedField(mid));

    }

    /**
     * 根据设计和场景获取所有的公式集合
     */
    private List<FunctionBusinessPo> getFunctionList(String designId, String useCase) {
        return functionBusinessService.list(Wrappers.<FunctionBusinessPo>lambdaQuery()
                        .select(FunctionBusinessPo::getId, FunctionBusinessPo::getType, FunctionBusinessPo::getParentType, FunctionBusinessPo::getBusinessId, FunctionBusinessPo::getRelatedIds, FunctionBusinessPo::getBody)
                        .eq(FunctionBusinessPo::getUseCase, useCase)
                        .eq(FunctionBusinessPo::getDesignId, designId)
                        .orderByAsc(FunctionBusinessPo::getCreateTime))
                //排除为空的公式
                .stream().filter(e -> ObjectNull.isNotNull(e.getBody())).filter(e -> ObjectNull.isNotNull(e.getBody().trim())).collect(Collectors.toList());
    }

    /**
     * 根据字段解析获取所有的公式触发key
     */
    private Map<String, FunctionBusinessPo> getFunctionMap(List<FunctionBusinessPo> functionList, ExecDto body) {
        Map<String, FunctionBusinessPo> functionMap = new HashMap<>(functionList.size());
        for (FunctionBusinessPo po : functionList) {
            // 使用覆盖操作获取最新的表达式
            functionMap.put(po.getBusinessId(), po);
        }
        return functionMap;
    }

    /**
     * 执行具体公式组装为对象进行返回
     *
     * @return
     */
    private Set<String> getModifiedParams(ExecDto dto, String useCase, List<String> paramBusinessIds, Map<String, FunctionBusinessPo> functionMap) {
        //如果是自己，则不处理，用户操作优先
        Object designSkips = SystemThreadLocal.get("designSkip");
        Set<String> nextKeySet = new HashSet<>();

        if (ObjectNull.isNotNull(dto.getIndex())) {
            //如果行级数不为空，则表示是表格里面的， 则需要获取关联字段进行匹配
            Map<String, FunctionBusinessPo> poHashMap = new HashMap<>(functionMap);
            for (String map : poHashMap.keySet()) {
                FunctionBusinessPo functionBusinessPo = functionMap.get(map);
                for (String relatedId : functionBusinessPo.getRelatedIds()) {
                    if (!functionMap.containsKey(relatedId)) {
                        functionMap.put(relatedId, functionBusinessPo);
                    }
                }

            }
        }
        for (int i = 0; i < paramBusinessIds.size(); i++) {
            // 不计算起点
            String businessIds = paramBusinessIds.get(i);
            String regex = ",";
            for (String businessId : businessIds.split(regex)) {
                FunctionBusinessPo function;
                if (functionMap.containsKey(businessId)) {
                    function = functionMap.get(businessId);
                } else {
                    ArrayList<String> strings = null;
                    if (ObjectNull.isNotNull(dto.getParentKey())) {
                        strings = new ArrayList<>(dto.getParentKey());
                    } else {
                        strings = new ArrayList<>();
                    }
                    strings.add(businessId);
                    businessId = strings.stream().collect(Collectors.joining("."));
                    function = functionMap.remove(businessId);
                }

                if (Objects.isNull(function)) {
                    // 没有表达式, 不做计算
                    continue;
                }
                if (ObjectNull.isNotNull(dto.getParentKey())) {
                    String collect = dto.getParentKey().stream().collect(Collectors.joining("."));
                    nextKeySet.add(collect);

                    if (function.getBusinessId().equals(collect + "." + dto.getModifiedField())) {
                        {
                            if (ObjectNull.isNotNull(dto.getIndex())) {
                                String key = dto.getParentKey().stream().collect(Collectors.joining(".")) + "[" + dto.getIndex() + "]" + ".";
                                //获取businessId的值，进行拼凑获取字段集
                                String[] split = function.getBusinessId().split("\\.");
                                key = key + split[split.length - 1];
                                Object eval = JvsJsonPath.read(JSONObject.toJSONString(dto.getParams()), key);
                                if (ObjectNull.isNotNull(eval)) {
                                    if (!eval.equals(0)) {
                                        //不为空，直接返回
                                        continue;
                                    }
                                }
                            } else {
                                Object eval = JvsJsonPath.read(JSONObject.toJSONString(dto.getParams()), function.getBusinessId());
                                if (ObjectNull.isNotNull(eval)) {
                                    if (!eval.equals(0)) {
                                        //不为空，直接返回
                                        continue;
                                    }
                                }
                            }
                        }

                    }
                } else {
                    //如果是自己，则不处理，用户操作优先
                    if (function.getBusinessId().equals(dto.getModifiedField())) {
                        continue;
                    }
                }

                //如果是表格，操作，可能需要直接退出
                if (ObjectNull.isNotNull(dto.getIndex())) {
                    if (!TABLE_FORM.equals(function.getParentType())) {
                        //如果父级不是表格，有行号的时候不触发
                        continue;
                    }
                    //匹配的 key不执行
                    String s = dto.getParentKey().stream().collect(Collectors.joining(".")) + dto.getModifiedField();
                    if (!function.getRelatedIds().contains(s)) {
//                        continue;
                    }
                }
                // 表格需要单独的配置标识 ,如果没有，直接跳过
                if (ObjectNull.isNotNull(function.getParentType())) {
                    if (TABLE_FORM.equals(function.getParentType())) {
                        if (ObjectNull.isNull(dto.getIndex())) {
                            //如果是表格，并没有行号
                            continue;
                        }
                    }
                    if (TAB_FORM.equals(function.getParentType())) {
                        if (ObjectNull.isNotNull(dto.getIndex())) {
                            continue;
                        }
                    }
                }


                String expression = function.getBody();
                try {
                    // 记录变更的数据
                    //获取到是否有索引字段
                    if (ObjectNull.isNotNull(dto.getIndex())) {
                        //判断是否是数组对象
                        Object result = handler.calculate(expression, dto.getParams(), useCase);
                        String key = dto.getParentKey().stream().collect(Collectors.joining(".")) + "[" + dto.getIndex() + "]" + ".";
                        //获取businessId的值，进行拼凑获取字段集
                        String[] split = function.getBusinessId().split("\\.");
                        key = key + split[split.length - 1];
                        JSONPath.set(dto.getParams(), key, result);
                    } else {
                        //判断原来是否有值如果有值，则直接返回 判断是否是初始化渲染，不是用户点击，如果是初始化，以原始数据为准，如果不为空再继续向下执行
                        if (ObjectNull.isNotNull(designSkips)) {
                            if (designSkips instanceof Boolean) {
                                if ((Boolean) designSkips) {
                                    Object read = JvsJsonPath.read(JSONObject.toJSONString(dto.getParams()), businessId);
                                    if (ObjectNull.isNotNull(read)) {
                                        extracted(functionMap, function);
                                        continue;
                                    }
                                }
                            }
                        }
                        //如果是表格，必须要行级数据
                        Object result = handler.calculate(expression, dto.getParams(), useCase);
                        JSONPath.set(dto.getParams(), businessId, result);
                    }
                    //删除已经执行的公式
                    extracted(functionMap, function);
                } catch (Exception e) {
                    // 计算异常时不能影响后续计算
                    log.error("公式计算异常, 表达式: {}, 参数: {}, 使用场景: {}, 异常信息: {}", expression, dto.getParams(), useCase, e);
                }
            }
        }
        return nextKeySet;
    }

    private static void extracted(Map<String, FunctionBusinessPo> functionMap, FunctionBusinessPo function) {
        if (function.getBusinessId().contains(".")) {
            //如果是表格，就删除，如果是选项卡就不删除 删除避免二次执行  此处没有做tab 选项卡里面的处理，有可能会在选项卡里面的表格有问题
            //表格的 . 只有一层，选项卡里面的点有多层 xx.xx.xx
            if (function.getBusinessId().indexOf(".") == function.getBusinessId().lastIndexOf(".")) {
                functionMap.remove(function.getBusinessId());
            }
        }
    }

    /**
     * 处理业务函数对象
     * <p>
     * 解析引用的变量id
     *
     * @param execPo 函数对象
     */
    public void handleFunctionPo(FunctionBusinessPo execPo) {
        if (Objects.isNull(execPo)) {
            throw new BusinessException("参数不能为空");
        }
        // 校验使用场景
        handler.checkUseCase(execPo.getUseCase());
        // 处理表达式引用关系
        String body = execPo.getBody();
        List<String> ids = Collections.emptyList();
        if (StringUtils.isNotBlank(body)) {
            try {
                ids = ExpressionUtils.getCustomParams(body);
            } catch (Exception e) {
                body = body.replaceAll("“", "\"");
                body = body.replaceAll("”", "\"");
                ids = ExpressionUtils.getCustomParams(body);
                throw new BusinessException("请输入英文双引号");
            }
        }
        if (ids.contains(execPo.getBusinessId())) {
            //公式在列表中的场景可以自己调用自己
            if (!"pageButtonDisplay".equals(execPo.getUseCase())) {
                throw new BusinessException("表达式不能引用自身");
            }
        }
        execPo.setRelatedIds(ids);
    }

}
