package cn.bctools.design.data.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.dto.DataEventSettingDto;
import cn.bctools.design.data.service.DataEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据事件管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[data]数据事件")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/data/event")
public class DataEventController {

    DataEventService eventService;

    /**
     * 查询设计的事件回调配置
     *
     * @param modelId  模型id
     * @param designId 设计id
     * @return 字段信息集合
     */
    @ApiOperation("查询事件列表")
    @GetMapping("/list/{modelId}/{designId}")
    public R<DataEventSettingDto> getList(@ApiParam(value = "模型id", required = true) @PathVariable("modelId") String modelId,
                                          @ApiParam(value = "设计id", required = true) @PathVariable("designId") String designId, @PathVariable String appId) {
        return R.ok(eventService.getEventList(appId, modelId, designId));
    }

    /**
     * 修改设计的事件回调配置
     *
     * @param modelId    模型id
     * @param designId   设计id
     * @param settingDto 事件回调配置
     * @return 字段信息集合
     */
    @ApiOperation("修改事件列表")
    @PutMapping("/list/{modelId}/{designId}")
    public R<DataEventSettingDto> updateList(@ApiParam(value = "模型id", required = true) @PathVariable("modelId") String modelId,
                                             @ApiParam(value = "设计id", required = true) @PathVariable("designId") String designId,
                                             @RequestBody DataEventSettingDto settingDto, @PathVariable String appId) {
        return R.ok(eventService.updateEventList(appId, modelId, designId, settingDto));
    }

}
