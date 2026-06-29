package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "消息模板变量与业务系统变量绑定")
@RestController
@RequestMapping(value = "/message/variable/bin/ding")
@AllArgsConstructor
public class MessageVariableBinDingController {
    private final MessageVariableBinDingService messageVariableBinDingService;

    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update")
    public R<MessageVariableBinDing> update(@RequestBody MessageVariableBinDing messageVariableBinDing) {
        messageVariableBinDingService.saveOrUpdate(messageVariableBinDing);
        return R.ok(messageVariableBinDing);
    }


    @Log
    @ApiOperation("新增或者修改批量")
    @PostMapping("/update/batch")
    public R<List<MessageVariableBinDing>> update(@RequestBody List<MessageVariableBinDing> messageVariableBinDing) {
        messageVariableBinDingService.saveOrUpdateBatch(messageVariableBinDing);
        return R.ok(messageVariableBinDing);
    }


    @Log
    @ApiOperation("获取消息对应的数据")
    @GetMapping("/select/{id}")
    public R<List<MessageVariableBinDing>> select(@ApiParam(value = "消息id", required = true) @PathVariable String id) {
        List<MessageVariableBinDing> list = messageVariableBinDingService.list(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, id));
        return R.ok(list);
    }
}
