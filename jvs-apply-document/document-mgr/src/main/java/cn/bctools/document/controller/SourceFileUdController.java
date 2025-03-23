package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.entity.SourceFileUdLog;
import cn.bctools.document.entity.enums.SourceFileUdOperateStatusEnums;
import cn.bctools.document.entity.enums.SourceFileUdOperateTypeEnums;
import cn.bctools.document.receiver.SourceFileUpDownConsumer;
import cn.bctools.document.receiver.dto.DownFileTaskDto;
import cn.bctools.document.receiver.dto.UpFileTaskDto;
import cn.bctools.document.service.SourceFileUdLogService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 */
@Slf4j
@Api(tags = "源文件操作")
@RestController
@RequestMapping(value = "/source/file")
@AllArgsConstructor
public class SourceFileUdController {
    private final SourceFileUdLogService sourceFileUdLogService;
    private final SourceFileUpDownConsumer sourceFileUpDownConsumer;

    @Log(back = false)
    @ApiOperation("获取日志记录")
    @GetMapping("/log/page")
    public R<Page<SourceFileUdLog>> logPage(Page<SourceFileUdLog> page, SourceFileUdLog sourceFileUdLog) {
        LambdaQueryWrapper<SourceFileUdLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(sourceFileUdLog.getOperateType()), SourceFileUdLog::getOperateType, sourceFileUdLog.getOperateType());
        queryWrapper.orderByDesc(SourceFileUdLog::getCreateTime);
        sourceFileUdLogService.page(page, queryWrapper);
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("下载文件")
    @PostMapping("/down")
    public R<SourceFileUdLog> down(@RequestBody SourceFileUdLog sourceFileUdLog) {
        sourceFileUdLog.setOperateType(SourceFileUdOperateTypeEnums.down)
                .setDcId(sourceFileUdLog.getDcId())
                .setOperateStatus(SourceFileUdOperateStatusEnums.await);
        sourceFileUdLogService.save(sourceFileUdLog);
        DownFileTaskDto downFileTaskDto = new DownFileTaskDto().setId(sourceFileUdLog.getDcId())
                .setTenantId(TenantContextHolder.getTenantId())
                .setLogId(sourceFileUdLog.getId());
        sourceFileUpDownConsumer.sendDownTask(downFileTaskDto);
        return R.ok(sourceFileUdLog);
    }

    @Log(back = false)
    @ApiOperation("上传文件")
    @PostMapping("/up/{parentId}")
    public R<SourceFileUdLog> up(@RequestBody SourceFileUdLog sourceFileUdLog, @PathVariable String parentId) {
        sourceFileUdLog.setOperateType(SourceFileUdOperateTypeEnums.up)
                .setOperateStatus(SourceFileUdOperateStatusEnums.await);
        sourceFileUdLogService.save(sourceFileUdLog);
        UpFileTaskDto upFileTaskDto = new UpFileTaskDto()
                .setParentId(parentId)
                .setTenantId(TenantContextHolder.getTenantId())
                .setFileName(sourceFileUdLog.getFileName())
                .setBucketName(sourceFileUdLog.getBucketName())
                .setLogId(sourceFileUdLog.getId())
                .setUserDto(UserCurrentUtils.getCurrentUser());
        sourceFileUpDownConsumer.sendUpTask(upFileTaskDto);
        return R.ok(sourceFileUdLog);
    }
}
