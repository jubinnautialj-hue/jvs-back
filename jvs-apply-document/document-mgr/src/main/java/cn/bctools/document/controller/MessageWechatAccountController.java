package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageVariableBinDing;
import cn.bctools.document.entity.MessageWechat;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.service.MessageWechatService;
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
@Api(tags = "消息模板-微信模板")
@RestController
@RequestMapping(value = "/message/wechat")
@AllArgsConstructor
public class MessageWechatAccountController {
    private final MessageWechatService messageWechatService;
    private final MessageVariableBinDingService messageVariableBinDingService;
    private final MessageConfigService messageconfigService;


    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update")
    public R<MessageWechat> update(@RequestBody MessageWechat messageWechat) {
        messageWechatService.saveOrUpdate(messageWechat);
        messageVariableBinDingService.batchSaveOrUpdate(messageWechat.getId(),messageWechat.getBinDingList());
        return R.ok(messageWechat);
    }

    @Log
    @ApiOperation("通过id获取数据")
    @GetMapping("/{id}")
    public R<MessageWechat> get(@ApiParam(value = "id", required = true) @PathVariable String id) {
        return R.ok(messageWechatService.getById(id));
    }

    @Log
    @ApiOperation("删除")
    @PostMapping("/del/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R delete(@ApiParam(value = "id", required = true) @PathVariable String id) {
        messageVariableBinDingService.remove(new LambdaQueryWrapper<MessageVariableBinDing>().eq(MessageVariableBinDing::getMessageId, id));
        messageconfigService.remove(new LambdaQueryWrapper<MessageConfig>().eq(MessageConfig::getMessageId, id));
        messageWechatService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("下拉框")
    @GetMapping("/select")
    public R<List<MessageWechat>> select() {
        List<MessageWechat> list = messageWechatService.list();
        return R.ok(list);
    }

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<MessageWechat>> getDictPage(Page<MessageWechat> page, MessageWechat dto) {
        LambdaQueryWrapper<MessageWechat> queryWrapper = new LambdaQueryWrapper<MessageWechat>()
                .like(StrUtil.isNotBlank(dto.getContent()), MessageWechat::getContent, dto.getContent())
                .eq(StrUtil.isNotBlank(dto.getFacilitator()), MessageWechat::getFacilitator, dto.getFacilitator())
                .orderByDesc(MessageWechat::getCreateTime);
        messageWechatService.page(page, queryWrapper);
        return R.ok(page);
    }
}
