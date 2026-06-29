package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库成员
 */

@Slf4j
@Api(tags = "知识库成员")
@RestController
@RequestMapping(value = "/dcLibrary")
@AllArgsConstructor
public class DcLibraryUserController {

    private final DcLibraryService dcLibraryService;

    @Log
    @GetMapping("/query/member/{id}")
    @ApiOperation("查询知识库成员")
    public R<Page<DcAuthConfig>> queryUser(Page<DcAuthConfig> page, @PathVariable String id) {
        Page<DcAuthConfig> userPage = dcLibraryService.queryUser(page, id);
        return R.ok(userPage);
    }


}
