package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.service.DcLibraryCollectService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库-收藏
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "文档收藏")
@RestController
@RequestMapping(value = "/dcLibrary/collect")
@AllArgsConstructor
public class DcLibraryCollectController {

    DcLibraryCollectService collectService;
    DcLibraryService dcLibraryService;

    @Log
    @ApiOperation("收藏|取消收藏")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> like(@ApiParam(value = "文档id", required = true) @PathVariable("id") String docId) {
        String msg;
        boolean collected = collectService.collect(docId);
        if (collected) {
            msg = "收藏成功";
        } else {
            msg = "取消收藏";
        }
        return R.ok(collected, msg);
    }

    @Log
    @ApiOperation("我的收藏")
    @GetMapping
    public R<Page<DcLibrary>> collect(Page<DcLibrary> page) {
        List<String> docIds = collectService.getCollection();
        if (ObjectUtils.isEmpty(docIds)) {
            return R.ok(page);
        }
        dcLibraryService.page(page, Wrappers.<DcLibrary>lambdaQuery().in(DcLibrary::getId, docIds));
        return R.ok(page);
    }

}
