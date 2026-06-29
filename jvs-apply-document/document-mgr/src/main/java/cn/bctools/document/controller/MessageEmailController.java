package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageEmail;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.document.service.MessageEmailService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
@Api(tags = "消息模板-邮件")
@RestController
@RequestMapping(value = "/message/email")
@AllArgsConstructor
public class MessageEmailController {
    private final MessageEmailService messageEmailService;
    private final MessageVariableBinDingService messageVariableBinDingService;
    private final MessageConfigService messageconfigService;


    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update")
    public R<MessageEmail> update(@RequestBody MessageEmail messageEmail) {
        messageEmailService.saveOrUpdate(messageEmail);
        messageVariableBinDingService.batchSaveOrUpdate(messageEmail.getId(),messageEmail.getBinDingList());
        return R.ok(messageEmail);
    }

    @Log
    @ApiOperation("通过id获取数据")
    @GetMapping("/{id}")
    public R<MessageEmail> get(@ApiParam(value = "id", required = true) @PathVariable String id) {
        return R.ok(messageEmailService.getById(id));
    }

    @Log
    @ApiOperation("删除")
    @PostMapping("/del/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R delete(@ApiParam(value = "id", required = true) @PathVariable String id) {
        messageVariableBinDingService.remove(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, id));
        messageconfigService.remove(new LambdaQueryWrapper<MessageConfig>().eq(MessageConfig::getMessageId, id));
        messageEmailService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("下拉框")
    @GetMapping("/select")
    public R<List<MessageEmail>> select() {
        List<MessageEmail> list = messageEmailService.list();
        return R.ok(list);
    }

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<MessageEmail>> getDictPage(Page<MessageEmail> page, MessageEmail dto) {
        LambdaQueryWrapper<MessageEmail> queryWrapper = new LambdaQueryWrapper<MessageEmail>()
                .like(StrUtil.isNotBlank(dto.getContent()), MessageEmail::getContent, dto.getContent())
                .eq(StrUtil.isNotBlank(dto.getFacilitator()), MessageEmail::getFacilitator, dto.getFacilitator())
                .orderByDesc(MessageEmail::getCreateTime);
        messageEmailService.page(page, queryWrapper);
        return R.ok(page);
    }
}
