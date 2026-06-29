package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.dto.FileSyncDto;
import cn.bctools.document.entity.DcSyncLog;
import cn.bctools.document.service.DcSyncLogService;
import cn.bctools.document.service.FileSyncService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件异步同步minio数据
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "文件异步同步minio数据")
@RestController
@RequestMapping(value = "/file/sync")
@AllArgsConstructor
public class FileSyncController {
    private final FileSyncService fileSyncService;
    private final DcSyncLogService dcSyncLogService;

    @Log
    @ApiOperation(value = "同步")
    @PostMapping
    @SneakyThrows
    public R fileSync(@RequestBody @Validated FileSyncDto fileSyncDto) {
        fileSyncService.fileSync(fileSyncDto, UserCurrentUtils.getCurrentUser());
        return R.ok();
    }

    @Log
    @ApiOperation(value = "获取所有文件夹 名称")
    @GetMapping("/get/folder")
    @SneakyThrows
    public R<List<String>> getMinioFolder() {
        List<String> folder = fileSyncService.getFolder();
        return R.ok(folder);
    }

    @Log
    @ApiOperation(value = "获取同步日志")
    @GetMapping("/page/log")
    @SneakyThrows
    public R<Page<DcSyncLog>> pageLog(Page<DcSyncLog> page, String syncIndicator) {
        LambdaQueryWrapper<DcSyncLog> wrapper = Wrappers.<DcSyncLog>lambdaQuery().eq(BasalPo::getCreateById, UserCurrentUtils.getUserId());
        if (StrUtil.isNotBlank(syncIndicator)) {
            wrapper.eq(DcSyncLog::getSyncIndicator, syncIndicator);
        }
        wrapper.orderByDesc(BasalPo::getCreateTime);
        dcSyncLogService.page(page, wrapper);
        return R.ok(page);
    }


}
