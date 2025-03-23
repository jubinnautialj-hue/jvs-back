package cn.bctools.data.factory.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import cn.bctools.data.factory.html.FHtmlGraph;
import cn.bctools.data.factory.html.LineHtml;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.SourceDto;
import cn.bctools.data.factory.html.node.InputNode;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.receiver.DataFactoryTaskConsumer;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Api(tags = "[jvs-data-factory]组件运行,此为测试运行，不为正式运行，正式运行需要使用定时任务进行执行")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/data/execute")
public class DataExecuteController {

    JvsDataFactoryService jvsDataFactoryService;
    DataFactoryTaskConsumer dataFactoryTaskConsumer;
    JvsDataFactoryQueueService jvsDataFactoryQueueService;
    DataSourceStructureService dataSourceStructureService;
    DorisJdbcTemplate dorisJdbcTemplate;

    @Log(back = false)
    @ApiOperation("执行逻辑")
    @PostMapping("/source/preview/{dataId}/{nodeId}")
    public R sourceTree(@PathVariable String dataId, @PathVariable String nodeId, @RequestBody FHtmlGraph html) {
        SourceDto source = null;
        String error = "执行逻辑错误";
        if (jvsDataFactoryService.getDataFactoryIsLock(dataId)) {
            try {
                //加锁
                //找到nodeId节点
                //两种执行模式进行执行
                html.setEndId(nodeId);
                //开始执 行
                html.run();
                source = html.getSourceDto();
                if (StrUtil.isNotBlank(html.getError())) {
                    error += ":" + html.getError();
                }
            } catch (Exception exception) {
                log.error("执行逻辑错误", exception);
                error += ":" + exception.getMessage();
            } finally {
                jvsDataFactoryService.unLockDataFactory(dataId);
            }
            if (source == null) {
                return R.failed(error);
            }
            return R.ok(source);
        }
        return R.failed("当前设计存在正在运行的任务,目前无法运行");
    }

    @Log(back = false)
    @ApiOperation("获取当前节点的上级节点的字段集")
    @PostMapping("/source/preview/{dataId}/{nodeId}/fields_map")
    public R getStageFieldsMap(@PathVariable String dataId, @PathVariable String nodeId, @RequestBody FHtmlGraph html) {
        List<NodeHtml> collect = null;
        String error = "执行逻辑错误";
        if (jvsDataFactoryService.getDataFactoryIsLock(dataId)) {
            try {
                //找到nodeId节点
                //两种执行模式进行执行
                //获取结束的节点 用于 判断是否退出 因为 当前节点是不需要执行的
                html.setEndId(nodeId);
                html.setIsIncludeEnd(Boolean.FALSE);
                //开始执行
                html.run();
                if (StrUtil.isNotBlank(html.getError())) {
                    error += ":" + html.getError();
                } else {
                    List<String> preNodeIds = html.getEdges().stream().filter(line -> ObjectUtil.equals(line.getTargetNode(), nodeId)).map(LineHtml::getSourceNode).collect(Collectors.toList());
                    collect = html.getNodeHeaderMapList().stream().filter(e -> ObjectUtil.contains(preNodeIds, e.getId())).collect(Collectors.toList());
                }
                //只获取头部信息
            } catch (Exception exception) {
                log.error("执行逻辑错误", exception);
                error += ":" + exception.getMessage();
            } finally {
                jvsDataFactoryService.unLockDataFactory(dataId);
            }
            if (collect == null) {
                return R.failed(error);
            }
            return R.ok(collect);
        }
        return R.failed("当前设计存在正在运行的任务,目前无法运行");
    }

    @Log
    @ApiOperation("立即同步")
    @GetMapping("/sync/data/{dataId}")
    public R getAll(@PathVariable String dataId) {
        JvsDataFactory dataFactory = jvsDataFactoryService.getById(dataId);
        if (!dataFactory.getIntegrityIs()) {
            return R.failed("配置不完整,请重新检查");
        }
        if (ObjectNull.isNull(dataFactory.getViewJson())) {
            return R.failed("数据智仓配置还未配置,请前去配置.");
        }
        //前置任务后置任务如果存在一条在队列中就直接返回
        String taskExec = jvsDataFactoryQueueService.isTaskExec(dataFactory);
        if (StrUtil.isNotBlank(taskExec)) {
            return R.failed(taskExec);
        }
        jvsDataFactoryService.sendQueue(dataFactory, QueueTaskTypeEnum.PREFIX_TASK, UserCurrentUtils.getCurrentUser(), IdGenerator.getIdStr(), OperateMethodEnum.MANUALLY);
        return R.ok("任务入队列成功");
    }

    @Log
    @ApiOperation("输入节点的数据重新同步")
    @PostMapping("/sync/data/ods")
    public R syncDataOds(@RequestBody NodeHtml nodeHtml) {
        InputNode inputNode = SpringContextUtil.getBean(InputNode.class);
        InputParams inputParams = JSONObject.parseObject(JSONObject.toJSONString(nodeHtml), InputParams.class);
        long count = dataSourceStructureService.count(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getId, inputParams.getSourceData().getFromSource().getId()));
        if (count == 0) {
            throw new BusinessException("数据源不存在!");
        }
        //生成表名称
        String tableName = "ods_" + nodeHtml.getDataId() + "_" + nodeHtml.getId() + "_" + Boolean.FALSE;
        inputParams.setTableName(tableName);
        //如果是重新同步 需要删除原有的表1
        dorisJdbcTemplate.dropForce(tableName);
        inputNode.syncData(inputParams, Boolean.TRUE, 10000L);
        return R.ok("数据同步完成");
    }

}
