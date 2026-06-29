package cn.bctools.auth.controller;


import cn.bctools.auth.entity.SysDictItem;
import cn.bctools.auth.service.SysDictItemService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "公共配置")
@RestController
@AllArgsConstructor
@RequestMapping("/public")
public class BaseController {

    SysDictItemService sysDictItemService;

    @Log
    @ApiOperation("通过字典类型查找字典")
    @GetMapping("/dict/type/{type}")
    @Cacheable(value = SysConstant.CACHE_DICT)
    public R<List<SysDictItem>> getDictByType(@PathVariable("type") String type) {
        List<SysDictItem> items = sysDictItemService.getByType(type);
        return R.ok(items);
    }

}
