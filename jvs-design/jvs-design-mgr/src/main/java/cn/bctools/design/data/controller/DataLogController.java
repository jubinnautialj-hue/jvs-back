package cn.bctools.design.data.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.data.entity.DataLogPo;
import cn.bctools.design.data.service.DataLogService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史数据管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[data]历史数据")
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}/data/log")
public class DataLogController {

    DataLogService dataLogService;
    DynamicDataService dynamicDataService;

    @ApiOperation("查询单条")
    @GetMapping("/query/single/{dataModelId}/{dataId}/{version}")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> querySingle(@PathVariable("appId") String appId, @PathVariable("dataId") String dataId,
                                              @PathVariable("version") String version, @PathVariable("dataModelId") String dataModelId) {
        DataLogPo logPo = dataLogService.getLog(dataModelId, dataId, version);
        if (Objects.isNull(logPo)) {
            log.warn("历史数据查询异常, 数据不存在, 数据id: {}, 版本: {}", dataId, version);
            return R.ok(Collections.emptyMap());
        }
        //做数据转换操作
        Map<String, Object> content = dynamicDataService.paresMapWithEcho(appId, logPo.getContent(), dataModelId, null, false);
        return R.ok(content);
    }

    @ApiOperation("查询变更记录")
    @GetMapping("/change/{dataModelId}/{dataId}")
    @Transactional(rollbackFor = Exception.class)
    public R<List<Object>> dataChange(@PathVariable("dataId") String dataId,
                                      @PathVariable("dataModelId") String dataModelId) {
        List<DataLogPo> logPos = dataLogService.getLog(dataModelId, dataId);
        if (CollectionUtils.isEmpty(logPos)) {
            return R.ok(Collections.emptyList());
        }
        return R.ok(logPos.stream()
                .filter(log -> CollectionUtils.isNotEmpty(log.getDataChange()))
                .sorted(Comparator.comparing(DataLogPo::getUpdateTime))
                .flatMap(log -> log.getDataChange().stream())
                .collect(Collectors.toList())
        );
    }

    @ApiOperation("获取数据的跟进记录")
    @GetMapping("/follow/{dataModelId}/{dataId}")
    public R<List<Object>> follow(@PathVariable("dataId") String dataId, @PathVariable("dataModelId") String dataModelId) {
        List<DataLogPo> logPos = dataLogService.follow(dataModelId, dataId);
        if (CollectionUtils.isEmpty(logPos)) {
            return R.ok(Collections.emptyList());
        }
        return R.ok(logPos.stream()
                .sorted(Comparator.comparing(DataLogPo::getUpdateTime))
                .collect(Collectors.toList())
        );
    }

    @ApiOperation("新增数据的跟进记录")
    @PostMapping("/follow/{dataModelId}/{dataId}")
    public R followAll(@PathVariable("dataId") String dataId, @PathVariable("dataModelId") String dataModelId, @RequestBody Map<String, Object> body, @PathVariable String appId) {
        dataLogService.follow(dataModelId, dataId, body, UserCurrentUtils.getNullableUser());
        return R.ok();
    }
}
