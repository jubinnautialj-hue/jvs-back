package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.data.factory.source.entity.SyncStructureLog;
import cn.bctools.data.factory.source.service.SyncStructureLogService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@Api(tags = "函数")
@RestController
@AllArgsConstructor
@RequestMapping("/sync/structure/log")
@Slf4j
public class SyncStructureLogController {
    private final SyncStructureLogService syncStructureLogService;

    @Log(back = false)
    @ApiOperation("分页数据")
    @GetMapping("/page")
    public R<Page<SyncStructureLog>> page(Page<SyncStructureLog> page, SyncStructureLog data) {
        LambdaQueryWrapper<SyncStructureLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SyncStructureLog::getDataSourceId, data.getDataSourceId())
                .orderByDesc(SyncStructureLog::getCreateTime);
        syncStructureLogService.page(page, queryWrapper);
        return R.ok(page);
    }
}
