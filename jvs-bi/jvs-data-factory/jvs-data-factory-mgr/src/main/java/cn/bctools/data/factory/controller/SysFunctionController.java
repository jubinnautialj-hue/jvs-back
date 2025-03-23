package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.dto.GetFunctionDto;
import cn.bctools.data.factory.entity.FieldSettingFunction;
import cn.bctools.data.factory.entity.SysFunction;
import cn.bctools.data.factory.service.FieldSettingFunctionService;
import cn.bctools.data.factory.service.SysFunctionService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Api(tags = "函数")
@RestController
@AllArgsConstructor
@RequestMapping("/sys/function")
@Slf4j
public class SysFunctionController {
    private final SysFunctionService sysFunctionService;
    private final FieldSettingFunctionService fieldSettingFunctionService;

    @ApiOperation("获取函数")
    @GetMapping
    public R<List<GetFunctionDto>> getFunction() {
        List<SysFunction> list = sysFunctionService.list();
        List<GetFunctionDto> functionDto = list.stream().collect(Collectors.groupingBy(SysFunction::getFunctionType, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream().map(e ->
                        new GetFunctionDto()
                                .setName(e.getKey())
                                .setChildren(e.getValue()))
                .collect(Collectors.toList());
        return R.ok(functionDto);
    }

    @Log(back = false)
    @ApiOperation("分页数据")
    @GetMapping("/page")
    public R<Page<SysFunction>> page(Page<SysFunction> page, SysFunction sysFunction) {
        LambdaQueryWrapper<SysFunction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysFunction::getCreateTime);
        if (StrUtil.isNotEmpty(sysFunction.getName())) {
            queryWrapper.like(SysFunction::getName, sysFunction.getName());
        }
        page = sysFunctionService.page(page, queryWrapper);
        return R.ok(page);
    }

    @Log
    @ApiOperation("新增或者修改")
    @PutMapping("/edit")
    @Deprecated
    public R<SysFunction> edit(@RequestBody SysFunction sysFunction) {
        if (StrUtil.isNotBlank(sysFunction.getId())) {
            SysFunction old = sysFunctionService.getById(sysFunction.getId());
            if (ObjectUtil.isNull(old)) {
                return R.failed("函数不存在");
            }
            if (old.getIsDorisFunction()) {
                return R.failed("doris函数不能修改");
            }
        } else {
            long count = sysFunctionService.count(new LambdaQueryWrapper<SysFunction>().eq(SysFunction::getName, sysFunction.getName()));
            if (count > 0) {
                return R.failed("函数名称不能重复");
            }
        }
        sysFunctionService.saveOrUpdate(sysFunction);
        return R.ok(sysFunction);
    }
    @Log(back = false)
    @ApiOperation("获取字段设置节点映射关系")
    @GetMapping("/field/setting/function")
    public R<List<FieldSettingFunction>> list() {
        List<FieldSettingFunction> list = fieldSettingFunctionService.list();
        return R.ok(list);
    }

}
