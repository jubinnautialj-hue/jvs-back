package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageSms;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.document.service.MessageSmsService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "消息模板-短信")
@RestController
@RequestMapping(value = "/message/sms")
@AllArgsConstructor
public class MessageSmsController {
    private final MessageSmsService messagesmsService;
    private final MessageVariableBinDingService messageVariableBinDingService;
    private final MessageConfigService messageconfigService;

    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update")
    public R<MessageSms> update(@RequestBody MessageSms messageSms) {
        messagesmsService.saveOrUpdate(messageSms);
        messageVariableBinDingService.batchSaveOrUpdate(messageSms.getId(),messageSms.getBinDingList());
        return R.ok(messageSms);
    }

    @Log
    @ApiOperation("通过id获取数据")
    @GetMapping("/{id}")
    public R<MessageSms> get(@ApiParam(value = "id", required = true) @PathVariable String id) {
        return R.ok(messagesmsService.getById(id));
    }

    @Log
    @ApiOperation("删除")
    @PostMapping("/del/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R delete(@ApiParam(value = "id", required = true) @PathVariable String id) {
        messageVariableBinDingService.remove(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, id));
        messageconfigService.remove(new LambdaQueryWrapper<MessageConfig>().eq(MessageConfig::getMessageId, id));
        messagesmsService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("短信下拉框")
    @GetMapping("/select")
    public R<List<MessageSms>> select() {
        List<MessageSms> list = messagesmsService.list();
        return R.ok(list);
    }

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<MessageSms>> getDictPage(Page<MessageSms> page, MessageSms dto) {
        LambdaQueryWrapper<MessageSms> queryWrapper = new LambdaQueryWrapper<MessageSms>()
                .like(StrUtil.isNotBlank(dto.getContent()), MessageSms::getContent, dto.getContent())
                .eq(StrUtil.isNotBlank(dto.getFacilitator()), MessageSms::getFacilitator, dto.getFacilitator())
                .orderByDesc(MessageSms::getCreateTime);
        messagesmsService.page(page, queryWrapper);
        return R.ok(page);
    }
}
