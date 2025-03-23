package cn.bctools.data.factory.html;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.CommonConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.enums.ImPushTypeEnums;
import cn.bctools.data.factory.html.node.dto.SupplierNodeData;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.im.MessageImPush;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.source.dto.InParameterDto;
import cn.bctools.data.factory.util.DorisUtil;
import cn.bctools.data.factory.util.SystemTool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Data
@Slf4j
@Accessors(chain = true)
public class FHtmlGraph {
    List<NodeHtml> nodes;
    List<LineHtml> edges;
    /**
     * 结束节点
     * 根据结束节点，找开始，然后进行一次一次执行
     * 预览执行，直接是用户点击的节点ID
     * 如果是正式执行，默认为输出节点
     */
    String endId = null;


    /**
     * 是否是正式运行，如果是正式运行则不需要获取返回结果
     */
    Boolean formal = false;

    /**
     * 当前节点的输出条数
     */
    Long nodeDataNumber = 0L;
    /**
     * 是否需要执行指定的结束节点  用于在设计时  会执行当前节点 浪费资源
     */
    Boolean isIncludeEnd = true;
    /**
     * 是否包含结束节点
     */
    Boolean isEnd = true;
    /**
     * 预览的节点的执行结果数据
     */
    SourceDto sourceDto;
    /**
     * 队列日志id
     */
    String queueLogId;
    /**
     * 执行到第几个节点了
     */
    double executeNodeNumber = 0;
    /**
     * 所有节点的map
     */
    Map<String, NodeHtml> nodeMap;
    /**
     * 所有的开始节点
     */
    List<NodeHtml> startNodes = new ArrayList<>();

    /**
     * 每个节点的标题
     */
    List<NodeHtml> nodeHeaderMapList = new LinkedList<>();

    /**
     * 入参
     */
    List<InParameterDto> inParameterPo;

    /**
     * 日志 用于记录每个节点的数据量
     */
    List<JvsDataFactoryLog.NodeLog> nodeLog = new ArrayList<>();

    /**
     * 错误信息
     */
    String error;

    public void run() {
        //线程工具用于设置api数据源的入参
        SystemTool<List<InParameterDto>> tool = new SystemTool<>();
        try {
            if (ObjectUtil.isNotEmpty(inParameterPo)) {
                tool.set("inParameter", inParameterPo);
            }
            init();
            //根据结束节点找到开始节点
            Map<String, SupplierNodeData> hashMap = new HashMap<>(startNodes.size());
            next(startNodes, hashMap);
        } catch (Exception e) {
            error = e.getMessage();
            log.error("执行节点错误", e);
        } finally {
            DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
            boolean notNull = ObjectUtil.isNotNull(sourceDto);
            String documentName = null;
            if (notNull) {
                documentName = this.sourceDto.getDocumentName();
            }
            if (formal) {
                //删除线程数据
                tool.remove("inParameter");
                //获取输出条数 用于记录日志
                if (StrUtil.isNotBlank(documentName)) {
                    this.nodeDataNumber = dorisJdbcTemplate.getCount(documentName);
                }
                //记录血缘视图
                ConsanguinityViewService bean = SpringContextUtil.getBean(ConsanguinityViewService.class);
                //先删除血缘视图 所有来源
                String dataId = startNodes.get(0).getDataId();
                bean.deleteSource(dataId, 1);
                ConsanguinityAnalyseConsumer consanguinityAnalyseConsumer = SpringContextUtil.getBean(ConsanguinityAnalyseConsumer.class);
                startNodes.stream().map(e -> {
                    JSONObject sourceData = (JSONObject) e.getSourceData();
                    JSONObject fromSource = sourceData.getJSONObject("fromSource");
                    return new ConsanguinityAnalyse()
                            .setDesignName(fromSource.getJSONObject("dataSource").getString("sourceName"))
                            .setDataFactoryId(e.getDataId())
                            .setDesignDetailId(fromSource.getString("id"))
                            .setTenantId(TenantContextHolder.getTenantId())
                            .setDesignDetailName(fromSource.getString("tableNameDesc"))
                            .setViewType(ConsanguinityViewTypeEnum.valueOf(fromSource.getString("sourceType")))
                            .setType(1)
                            .setDesignId(fromSource.getString("dataSourceId"));
                }).forEach(consanguinityAnalyseConsumer::send);
            }
            //删除中间表数据 如果不是正式运行表示设计时调用 就需要设置一下返回值  需要判断是否为设计阶段 设计阶段不用删除开始节点表
            List<String> startNodeId = new ArrayList<>();
            if (!formal) {
                startNodeId = nodes.stream().filter(e -> e.getType().equals(FNodeType.Input)).map(NodeHtml::getId).collect(Collectors.toList());
            }
            List<String> finalStartNodeId = startNodeId;
            List<String> collectionNames = nodeHeaderMapList.stream()
                    .filter(e -> !finalStartNodeId.contains(e.getId()))
                    .map(e -> DorisUtil.createTableName(e.getDataId(), e.getId(), this.formal))
                    .collect(Collectors.toList());
            //如果不是正式运行就需要删除最后一个节点的数据 正式运行最后一个节点就是结果输出需要执行以下逻辑
            if (!formal && notNull) {
                //排序
                List<Map<String, Object>> list = dorisJdbcTemplate.findAllToFormat(documentName, this.getSourceDto().getHeaders(), null);
                this.sourceDto.setData(list);
                //因为 nodeHeaderMapList  是存储的当前节点的前面节点 所以需要添加当前节点
                //如果不是正式运行并且是开始节点就需要排除
                if (!finalStartNodeId.contains(this.getSourceDto().getNodeId())) {
                    collectionNames.add(documentName);
                }
            }
            //如果是正式运行需要判断开始节点是否存在开启了增量同步 如果是 就不需要删除 节点数据
            if (formal) {
                nodes.stream().filter(e -> e.getType().equals(FNodeType.Input))
                        .map(e -> JSONObject.parseObject(JSONObject.toJSONString(e), InputParams.class))
                        .filter(e -> Optional.ofNullable(e.getSourceData().getFromSource().getIncrementalSetting()).orElse(new InputParams.IncrementalSetting().setIncrementalMode(Boolean.FALSE)).getIncrementalMode())
                        .map(e -> DorisUtil.createTableName(e.getDataId(), e.getId(), this.formal))
                        .forEach(collectionNames::remove);
            }
            //判断是否进入debug模式
            if (!SpringContextUtil.getBean(CommonConfig.class).getDebugStatus()) {
                //去重防止有些节点的入参没有,返回的是上一个节点的表名称
                collectionNames.stream().distinct().forEach(dorisJdbcTemplate::dropForce);
            }

        }
    }

    public static Class getParameterClass(Class<?> e) {
        Type obj = e.getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) obj;
        Class cls = (Class) parameterizedType.getActualTypeArguments()[0];
        return cls;
    }

    private void init() {
        //todo 限制执行节点
        if (StrUtil.isNotBlank(endId)) {
            List<String> executeNodeIds = this.findExecuteNodeIds();
            nodes = nodes.stream().filter(e -> executeNodeIds.contains(e.getId())).collect(Collectors.toList());
        }
        nodeMap = nodes.stream().collect(Collectors.toMap(NodeHtml::getId, Function.identity()));
        //如果不是用户选择的结束节点，则直接使用节点里面的结束节点
        if (ObjectNull.isNull(endId)) {
            endId = getNodes().stream().filter(e -> e.getType().equals(FNodeType.export)).findFirst().map(e -> e.getId()).get();
        }
        //根据结束节点，找到开始节点
        findStart(endId);
    }

    private List<String> findExecuteNodeIds() {
        List<LineHtml> resultLineHtml = new ArrayList<>();
        List<LineHtml> fromList = this.edges.stream().filter(e -> StrUtil.equals(e.getTargetNode(), this.endId)).collect(Collectors.toList());
        if (!fromList.isEmpty()) {
            resultLineHtml.addAll(fromList);
            this.findExecuteNodeIds(fromList, resultLineHtml);
        }
        List<String> collect = resultLineHtml.stream().map(LineHtml::getSourceNode).collect(Collectors.toList());
        collect.add(this.endId);
        return collect;
    }

    private void findExecuteNodeIds(List<LineHtml> fromList, List<LineHtml> result) {
        List<String> nodeIds = fromList.stream().map(LineHtml::getSourceNode).collect(Collectors.toList());
        List<LineHtml> collect = this.edges.stream().filter(e -> nodeIds.contains(e.getTargetNode())).collect(Collectors.toList());
        result.addAll(collect);
        if (!collect.isEmpty()) {
            findExecuteNodeIds(collect, result);
        }
    }

    /**
     * 根据节点找到上级节点
     *
     * @return
     */
    private void findStart(String endId) {
        //如果节点的是开始，则直接返回
        NodeHtml nodeHtml = nodeMap.get(endId);
        if (ObjectNull.isNotNull(nodeHtml) && Arrays.asList(FNodeType.Input).contains(nodeHtml.getType())) {
            startNodes.add(nodeHtml);
            //直接结束
            return;
        }

        //找上级节点id
        this.getEdges().stream()
                //过滤
                .filter(e -> e.getTargetNode().equals(endId))
                //获取节点
                .map(e -> e.getSourceNode())
                //获取对应的节点
                .map(e -> nodeMap.get(e))
                //根据节点判断是否是开始节点
                .forEach(e -> {
                    //如果是开始节点，则添加上去
                    if (e.getType().equals(FNodeType.Input)) {
                        //判断开始节点是否已添加
                        boolean b = startNodes.stream().anyMatch(node -> node.getId().equals(e.getId()));
                        if (!b) {
                            startNodes.add(e);
                        }
                    } else {
                        //递归找下级l
                        findStart(e.getId());
                    }
                });
        //根据线找点
    }

    private List<NodeHtml> findNext(List<NodeHtml> nodes) {
        //符合条件的节点
        List<NodeHtml> collect = edges.stream()
                .filter(e -> nodes.stream().anyMatch(o -> o.getId().equals(e.getSourceNode())))
                .map(e -> nodeMap.get(e.getTargetNode()))
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(NodeHtml::getId))), ArrayList::new));
        List<String> executeNodeIds = collect.stream().map(NodeHtml::getId).collect(Collectors.toList());
        Map<String, List<String>> compareMap = edges.stream().filter(line -> executeNodeIds.contains(line.getTargetNode()))
                .collect(Collectors.groupingBy(LineHtml::getTargetNode, Collectors.mapping(LineHtml::getSourceNode, Collectors.toList())));
        //判断节点执行等级一致 即在当前可执行节点数据集中找不到上级节点
        collect = collect.stream().filter(node -> {
            //上级节点id集合
            List<String> prevNodeIds = compareMap.get(node.getId());
            return prevNodeIds.stream().noneMatch(executeNodeIds::contains);
        }).collect(Collectors.toList());

        //获取到了多个节点
        return collect;
    }

    /**
     * 执行下一组节点
     *
     * @param executeNodeList 需要执行的节点
     * @param run             执行需要的数据 nodeId:nodeData
     */
    private void next(List<NodeHtml> executeNodeList, Map<String, SupplierNodeData> run) {
        DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        boolean end = false;
        for (int i = 0; i < executeNodeList.size(); i++) {
            NodeHtml nodeHtml = executeNodeList.get(i);
            //执行当前节点时 需要判断上一个节点是否执行完成如果没有直接放行
            List<String> collect = this.edges.parallelStream().filter(e -> e.getTargetNode().equals(nodeHtml.getId())).map(LineHtml::getSourceNode).collect(Collectors.toList());
            long count = nodeHeaderMapList.parallelStream().filter(e -> collect.contains(e.getId())).count();
            if (count != collect.size()) {
                continue;
            }
            //判断是不否有结束节点如果有，则不再执行了
            //只执行这个节点了，不再向下执行了
            //判断是否需要执行当前节点
            if (!this.isIncludeEnd) {
                List<String> list = this.edges.stream().filter(e -> e.getTargetNode().equals(this.endId)).map(LineHtml::getSourceNode).collect(Collectors.toList());
                //判断是否执行完成
                int size = (int) nodeHeaderMapList.stream().filter(e -> list.contains(e.getId())).count();
                if (list.size() == size) {
                    break;
                }
            }
            long st = System.currentTimeMillis();
            //生成中间表名称
            String tableName = DorisUtil.createTableName(nodeHtml.getDataId(), nodeHtml.getId(), this.formal);
            //如果是设计阶段并且为输入节点 就不需要删除
            if (!this.formal && !nodeHtml.getType().equals(FNodeType.Input)) {
                //检查一下是否存在这个表如果存在先删除
                dorisJdbcTemplate.dropForce(tableName);
            }
            nodeHtml.setTableName(tableName);
            FData data = runNode(nodeHtml, run);
            if (this.formal) {
                executeNodeNumber++;
                //修改队列日志中的进度
                double v = executeNodeNumber / this.nodes.size();
                JvsDataFactoryQueueService jvsDataFactoryQueueService = SpringContextUtil.getBean(JvsDataFactoryQueueService.class);
                String percent = NumberUtil.formatPercent(v, 2);
                jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>()
                        .lambda()
                        .set(JvsDataFactoryQueue::getExecuteSchedule, percent)
                        .eq(JvsDataFactoryQueue::getId, queueLogId));
                MessageImPush messageImPush = SpringContextUtil.getBean(MessageImPush.class);
                messageImPush.notify(ImPushTypeEnums.updateValue, percent, queueLogId);
                //记录详细日志
                JvsDataFactoryLog.NodeLog log = new JvsDataFactoryLog.NodeLog()
                        .setNodeId(nodeHtml.getId())
                        .setNodeName(nodeHtml.getName())
                        .setType(nodeHtml.getType())
                        .setCount(0L)
                        .setDuration(System.currentTimeMillis() - st);
                //获取中数据量
                log.setCount(dorisJdbcTemplate.getCount(data.getDocumentName()));
                this.nodeLog.add(log);
            }
            //判断当前节点是否为输出如果为输出节点表示执行完成
            if (nodeHtml.getType().equals(FNodeType.export)) {
                end = true;
            } else {
                end = ObjectNull.isNotNull(endId) && endId.equals(nodeHtml.getId());
            }
            //判断是否是结束节点，如果是结束节点，直接获取数据，不获取流
            if (end) {
                sourceDto = new SourceDto().setNodeId(nodeHtml.getId()).setDocumentName(data.getDocumentName()).setHeaders(data.getTitle());
                continue;
            }
            //储存当前节点前所有节点的字段集以及节点名称和节点id
            //这里需要copy一下 不然会修改同一个地址的数据
            List<DataSourceField> title = data.getTitle();
            List<DataSourceField> copys = BeanCopyUtil.copys(title, DataSourceField.class);
            nodeHtml.setFieldList(copys);
            nodeHeaderMapList.add(nodeHtml);
        }
        if (!end) {
            List<NodeHtml> next = findNext(executeNodeList);
            if (ObjectNull.isNull(next) || next.isEmpty()) {
                return;
            }
            next(next, run);
        }
    }

    /**
     * 执行节点
     *
     * @param node    节点
     * @param hashMap 所有已执行节点数据
     * @return 当前节点数据
     */
    public FData runNode(NodeHtml node, Map<String, SupplierNodeData> hashMap) {
        Class<? extends Frun> cls = node.getType().getCls();
        Frun bean = SpringContextUtil.getBean(cls);
        Class parameterClass = getParameterClass(cls);
        Object o = JSONObject.parseObject(JSONObject.toJSONString(node), parameterClass);
        //查询与此节点的最近的前面的节点
        //获取线的总数
        List<LineHtml> lines = getEdges().stream().filter(e -> e.getTargetNode().equals(node.getId())).collect(Collectors.toList());
        List<String> fromNodeList = lines.stream().map(LineHtml::getSourceNode).collect(Collectors.toList());
        log.info("执行节点:" + node.getName());
        //转换
        Map<String, FData> nodeDataMap = new HashMap<>();
        for (String nodeId : fromNodeList) {
            List<DataSourceField> title = JSONArray.parseArray(JSONObject.toJSONString(hashMap.get(nodeId).getTitle()), DataSourceField.class);
            nodeDataMap.put(nodeId, new FData()
                    .setDocumentName(hashMap.get(nodeId).getDocumentName())
                    .setType(hashMap.get(nodeId).getType())
                    .setTitle(title));
        }
        NodeHtml html = (NodeHtml) o;
        //过滤没有展示的字段
        List<DataSourceField> title = JSONArray.parseArray(JSONObject.toJSONString(html.getFieldList()), DataSourceField.class)
                .stream()
                .peek(e -> e.setIsShow(Optional.ofNullable(e.getIsShow()).orElse(Boolean.FALSE)))
                .filter(DataSourceField::getIsShow)
                .collect(Collectors.toList());
        html.setFieldList(title);
        FData run = bean.run(formal, nodeDataMap, html);
        SupplierNodeData nodeData = new SupplierNodeData().setType(node.getType()).setTitle(run.getTitle()).setDocumentName(run.getDocumentName());
        //将数据存储到存储器中
        hashMap.put(node.getId(), nodeData);
        return run;
    }


}
