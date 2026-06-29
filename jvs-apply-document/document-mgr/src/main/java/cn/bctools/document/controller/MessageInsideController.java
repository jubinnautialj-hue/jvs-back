package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageInside;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.document.service.MessageInsideService;
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
@Api(tags = "消息模板-站内")
@RestController
@RequestMapping(value = "/message/inside")
@AllArgsConstructor
public class MessageInsideController {
    private final MessageInsideService messageInsideService;
    private final MessageVariableBinDingService messageVariableBinDingService;
    private final MessageConfigService messageconfigService;


    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update")
    public R<MessageInside> update(@RequestBody MessageInside messageInside) {
        messageInsideService.saveOrUpdate(messageInside);
        messageVariableBinDingService.batchSaveOrUpdate(messageInside.getId(),messageInside.getBinDingList());
        return R.ok(messageInside);
    }

    @Log
    @ApiOperation("删除")
    @PostMapping("/del/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R delete(@ApiParam(value = "id", required = true) @PathVariable String id) {
        messageVariableBinDingService.remove(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, id));
        messageconfigService.remove(new LambdaQueryWrapper<MessageConfig>().eq(MessageConfig::getMessageId, id));
        messageInsideService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("站内信下拉框")
    @GetMapping("/select")
    public R<List<MessageInside>> select() {
        List<MessageInside> list = messageInsideService.list();
        return R.ok(list);
    }

    @Log
    @ApiOperation("通过id获取数据")
    @GetMapping("/{id}")
    public R<MessageInside> get(@ApiParam(value = "id", required = true) @PathVariable String id) {
        return R.ok(messageInsideService.getById(id));
    }

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<MessageInside>> getDictPage(Page<MessageInside> page, MessageInside dto) {
        LambdaQueryWrapper<MessageInside> queryWrapper = new LambdaQueryWrapper<MessageInside>()
                .like(StrUtil.isNotBlank(dto.getContent()), MessageInside::getContent, dto.getContent())
                .eq(StrUtil.isNotBlank(dto.getFacilitator()), MessageInside::getFacilitator, dto.getFacilitator())
                .orderByDesc(MessageInside::getCreateTime);
        messageInsideService.page(page, queryWrapper);
        return R.ok(page);
    }
}
