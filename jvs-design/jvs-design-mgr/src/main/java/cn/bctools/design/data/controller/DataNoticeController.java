package cn.bctools.design.data.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.notice.dto.DataNoticeDto;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[data]消息通知")
@RestController
@AllArgsConstructor
@RequestMapping("/base/data/notice")
@Deprecated
public class DataNoticeController {

    private final DataNoticeService service;

    @Log
    @ApiOperation("保存")
    @PostMapping
    public R<String> create(@Validated @RequestBody DataNoticeDto dto, @PathVariable String appId) {
        service.saveDataNotice(appId, dto);
        return R.ok();
    }
}
