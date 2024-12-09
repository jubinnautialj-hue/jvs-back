package cn.bctools.design.data.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.design.notice.dto.DataNoticeDto;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[data]消息通知")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/data/notice")
public class DataNoticeDesignController {

    private final DataNoticeService service;

    @Log
    @ApiOperation("新增")
    @PostMapping
    public R<String> create(@Validated @RequestBody DataNoticeDto dto, @PathVariable String appId) {
        service.saveDataNotice(appId, dto);
        return R.ok();
    }

    @Log
    @ApiOperation("修改")
    @PutMapping
    public R<String> update(@Validated @RequestBody DataNoticeDto dto, @PathVariable String appId) {
        Optional.ofNullable(dto.getId()).orElseThrow(() -> new BusinessException("id为空"));
        service.saveDataNotice(appId, dto);
        return R.ok();
    }

    @Log
    @ApiOperation("获取模型所有通知配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelId", value = "模型id", required = true),
    })
    @GetMapping("/{modelId}")
    public R<List<QueryDataNoticeRespDto>> getAllByModelId(@PathVariable String modelId, @PathVariable String appId) {
        return R.ok(service.getAllByModelId(appId, modelId));
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public R<FlowTaskProxy> revoke(@PathVariable String id, @PathVariable String appId) {
        service.remove(Wrappers.query(new DataNoticePo().setId(id).setJvsAppId(appId)));
        return R.ok();
    }
}
