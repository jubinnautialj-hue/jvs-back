package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum.ACCOMPLISH;

/**
 * @author xiaohui
 */
@Api(tags = "数据集-监控")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/monitoring")
public class MonitoringController {
    private final JvsDataFactoryQueueService jvsDataFactoryQueueService;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final static String TASK_FAIL_MESSAGE="%s在%s手动清理状态";

    @SneakyThrows
    @ApiOperation("检查任务是否存在执行中")
    @GetMapping("/check/queue/{id}")
    public R<Long> checkQueue(@ApiParam("数据集id") @PathVariable("id") String id) {
        JvsDataFactory byId = jvsDataFactoryService.getById(id);
        String taskExec = jvsDataFactoryQueueService.isTaskExec(byId);
        if (StrUtil.isNotBlank(taskExec)) {
            return R.failed(taskExec);
        }
        return R.ok(0L);
    }

    @Log(back = false)
    @ApiOperation("获取队列任务")
    @GetMapping("/get/queue/task/{isAccomplish}")
    public R<Page<JvsDataFactoryQueue>> getQueuePage(Page<JvsDataFactoryQueue> page, JvsDataFactoryQueue jvsDataFactoryQueue, @ApiParam("是否为已执行") @PathVariable Boolean isAccomplish) {
        LambdaQueryWrapper<JvsDataFactoryQueue> queryWrapper = new LambdaQueryWrapper<JvsDataFactoryQueue>()
                .select(JvsDataFactoryQueue::getCreateTime,JvsDataFactoryQueue::getDataFactoryId,JvsDataFactoryQueue::getTaskItselfId, JvsDataFactoryQueue::getId,JvsDataFactoryQueue::getTaskStatus,JvsDataFactoryQueue::getEndTime,
                        JvsDataFactoryQueue::getErrorMsg,JvsDataFactoryQueue::getQueueTaskType, JvsDataFactoryQueue::getPrincipalName, JvsDataFactoryQueue::getOperateMethod)
                .orderByDesc(JvsDataFactoryQueue::getCreateTime);
        if (isAccomplish) {
            queryWrapper.notIn(JvsDataFactoryQueue::getTaskStatus, ACCOMPLISH, QueueTaskStatusEnum.FAIL);
        } else {
            queryWrapper.in(JvsDataFactoryQueue::getTaskStatus, ACCOMPLISH, QueueTaskStatusEnum.FAIL);
        }
        page = jvsDataFactoryQueueService.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return R.ok(page);
        }
        List<String> ids = page.getRecords().stream().flatMap(e -> {
            if (StrUtil.isNotBlank(e.getTaskItselfId())) {
                return Arrays.asList(e.getDataFactoryId(), e.getTaskItselfId()).stream();
            } else {
                return Arrays.asList(e.getDataFactoryId()).stream();
            }
        }).distinct().collect(Collectors.toList());
        List<JvsDataFactory> jvsDataFactories = jvsDataFactoryService.listByIds(ids);
        page.getRecords().stream().peek(e -> {
            String name = jvsDataFactories.stream().filter(v -> v.getId().equals(e.getDataFactoryId())).findFirst().orElse(new JvsDataFactory().setName("--")).getName();
            e.setTaskName(name);
            String taskName = jvsDataFactories.stream().filter(v -> v.getId().equals(e.getTaskItselfId())).findFirst().orElse(new JvsDataFactory().setName("--")).getName();
            e.setTaskItselfName(taskName);
        }).collect(Collectors.toList());
        return R.ok(page);
    }

    @Log
    @ApiOperation("修改状态")
    @GetMapping("/update/queue/task/{id}")
    public R<Boolean> updateQueueTask(@ApiParam("任务调度id") @PathVariable("id") String taskId) {
        //检查一下状态是否被变更
        long count = jvsDataFactoryQueueService.count(new LambdaQueryWrapper<JvsDataFactoryQueue>().eq(JvsDataFactoryQueue::getId, taskId).eq(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.QUEUE));
        if (count > 0) {
            jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda().eq(JvsDataFactoryQueue::getId, taskId).set(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.STOP));
        } else {
            return R.failed("此任务状态已经变更!");
        }
        return R.ok();
    }
    @Log
    @ApiOperation("修改状态")
    @PostMapping("/update/queue/task/batch")
    public R<Boolean> updateQueueTaskBatch() {
        jvsDataFactoryQueueService.update(new UpdateWrapper<JvsDataFactoryQueue>().lambda()
                .ne(JvsDataFactoryQueue::getTaskStatus, QueueTaskStatusEnum.ACCOMPLISH)
                .ne(JvsDataFactoryQueue::getTaskStatus,QueueTaskStatusEnum.FAIL)
                .set(JvsDataFactoryQueue::getTaskStatus,QueueTaskStatusEnum.FAIL)
                .set(JvsDataFactoryQueue::getErrorMsg,String.format(TASK_FAIL_MESSAGE, UserCurrentUtils.getRealName(),DateUtil.now())));
        return R.ok(Boolean.TRUE);
    }
}
