package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "消息配置")
@RestController
@RequestMapping(value = "/message")
@AllArgsConstructor
public class MessageConfigController {
    private final MessageConfigService messageConfigService;

    @Log
    @ApiOperation("新增或者修改")
    @PostMapping("/update/batch/{id}")
    public R<List<MessageConfig>> updateBatch(@RequestBody List<MessageConfig> list, @ApiParam("文库或者文档id") @PathVariable String id) {
        messageConfigService.saveMessage(list, id);
        return R.ok(list);
    }


    @Log
    @ApiOperation("删除")
    @PostMapping("/del/{id}")
    public R<Boolean> del(@PathVariable String id) {
        return R.ok(messageConfigService.removeById(id));
    }

    @Log
    @ApiOperation("查询已经绑定的数据")
    @GetMapping("/get")
    public R<List<MessageConfig>> get(MessageConfig message) {
        List<MessageConfig> list = messageConfigService.list(new LambdaQueryWrapper<MessageConfig>()
                .eq(MessageConfig::getBusinessId, message.getBusinessId())
                .eq(ObjectUtil.isNotNull(message.getMessageType()), MessageConfig::getMessageType, message.getMessageType()));
        return R.ok(list);
    }


}
