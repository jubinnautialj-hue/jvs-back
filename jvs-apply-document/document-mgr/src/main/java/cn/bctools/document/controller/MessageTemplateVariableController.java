package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.entity.MessageTemplateVariable;
import cn.bctools.document.service.MessageTemplateVariableService;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "消息变量")
@RestController
@RequestMapping(value = "/message/variable")
@AllArgsConstructor
public class MessageTemplateVariableController {
    private final MessageTemplateVariableService messageTemplateVariableService;

    @Log
    @ApiOperation("获取所有数据")
    @GetMapping("/select")
    public R<List<MessageTemplateVariable>> select() {
        List<MessageTemplateVariable> list = messageTemplateVariableService.list();
        return R.ok(list);
    }
}
