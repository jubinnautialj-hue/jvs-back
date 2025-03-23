package cn.bctools.bi.controller;


import cn.bctools.bi.dto.DownFileTaskDto;
import cn.bctools.bi.dto.UpFileTaskDto;
import cn.bctools.bi.entity.JvsBiFile;
import cn.bctools.bi.entity.enums.JvsBiFileOperateTypeEnums;
import cn.bctools.bi.receiver.BiConsumer;
import cn.bctools.bi.service.JvsBiFileService;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
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

import java.util.stream.Collectors;

/**
 * @author admin
 */
@Slf4j
@Api(tags = "[jvs-bi]")
@RestController
@AllArgsConstructor
@RequestMapping("/bi")
public class BiController {
    private final BiConsumer biConsumer;
    private final JvsBiFileService jvsBiFileService;

    @Log(back = false)
    @ApiOperation("下载文件")
    @PostMapping("/down")
    public R<JvsBiFile> down(@RequestBody JvsBiFile jvsBiFile) {
        jvsBiFile.setOperateType(JvsBiFileOperateTypeEnums.down);
        jvsBiFileService.save(jvsBiFile);
        DownFileTaskDto downFileTaskDto = new DownFileTaskDto()
                .setDataId(jvsBiFile.getDataId())
                .setIsMock(jvsBiFile.getIsMock())
                .setType(jvsBiFile.getServeType())
                .setTaskId(jvsBiFile.getId())
                .setTenantId(TenantContextHolder.getTenantId());
        biConsumer.sendDownTask(downFileTaskDto);
        return R.ok(jvsBiFile);
    }

    @Log(back = false)
    @ApiOperation("上传文件")
    @PostMapping("/up/{menuId}")
    public R<JvsBiFile> up(@RequestBody JvsBiFile jvsBiFile, @PathVariable String menuId) {
        jvsBiFileService.save(jvsBiFile);
        UpFileTaskDto downFileTaskDto = new UpFileTaskDto()
                .setFileName(jvsBiFile.getFileName())
                .setType(jvsBiFile.getServeType())
                .setTaskId(jvsBiFile.getId())
                .setBucketName(jvsBiFile.getBucketName())
                .setMenuId(menuId)
                .setUserDto(UserCurrentUtils.getCurrentUser())
                .setTenantId(TenantContextHolder.getTenantId());
        biConsumer.sendUpTask(downFileTaskDto);
        return R.ok(jvsBiFile);
    }

    @Log(back = false)
    @ApiOperation("分页数据")
    @GetMapping("/page")
    public R<Page<JvsBiFile>> page(Page<JvsBiFile> page, JvsBiFile data) {
        LambdaQueryWrapper<JvsBiFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.
                select(JvsBiFile::getErrorMessage, JvsBiFile::getServeType, JvsBiFile::getOperateType, JvsBiFile::getFileName, JvsBiFile::getDataId, JvsBiFile::getId, JvsBiFile::getBucketName, JvsBiFile::getOperateStatus, JvsBiFile::getCreateTime, JvsBiFile::getCreateBy).
                eq(ObjUtil.isNotEmpty(data.getServeType()), JvsBiFile::getServeType, data.getServeType())
                .eq(ObjUtil.isNotEmpty(data.getOperateType()), JvsBiFile::getOperateType, data.getOperateType())
                .orderByDesc(JvsBiFile::getCreateTime);
        page = jvsBiFileService.page(page, lambdaQueryWrapper);
        //添加桶名称 前端ng 直接转发即可
        page.getRecords().stream().filter(e -> e.getOperateType().equals(JvsBiFileOperateTypeEnums.down)).filter(e -> ObjUtil.isNotEmpty(e.getFileName())).peek(e -> e.setFileName(e.getBucketName() + "/" + e.getFileName())).collect(Collectors.toList());
        return R.ok(page);
    }
}
