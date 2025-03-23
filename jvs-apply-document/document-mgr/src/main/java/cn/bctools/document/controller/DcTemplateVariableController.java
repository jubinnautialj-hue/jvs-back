package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.document.dto.DcTemplateVariableDto;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.entity.DcTemplateVariable;
import cn.bctools.document.service.DcTemplateService;
import cn.bctools.document.service.DcTemplateVariableService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档模板
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "文档变量")
@RestController
@RequestMapping(value = "/template/variable")
@AllArgsConstructor
public class DcTemplateVariableController {

    private final DcTemplateVariableService dcTemplateVariableService;
    private final DcTemplateService dcTemplateService;

    @Log
    @GetMapping
    @ApiOperation("获取变量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateId", value = "模板id")
    })
    public R<List<DcTemplateVariableDto>> list(String templateId) {
        LambdaQueryWrapper<DcTemplateVariable> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotEmpty(templateId)) {
            queryWrapper.eq(DcTemplateVariable::getTemplateId, templateId);
        }
        List<DcTemplateVariable> list = dcTemplateVariableService.list(queryWrapper);
        if (list.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        LinkedHashMap<String, List<DcTemplateVariable>> map = list.parallelStream().collect(Collectors.groupingBy(DcTemplateVariable::getTemplateId, LinkedHashMap::new, Collectors.toList()));
        Map<String, String> dcTemplateNameMap = dcTemplateService.listByIds(map.keySet()).parallelStream().collect(Collectors.toMap(DcTemplate::getId, DcTemplate::getName));
        List<DcTemplateVariableDto> dtoList = map.keySet().parallelStream().map(e ->
                new DcTemplateVariableDto()
                        .setTemplateId(e)
                        .setTemplateName(dcTemplateNameMap.getOrDefault(e, ""))
                        .setVariableList(map.get(e))
        ).collect(Collectors.toList());
        return R.ok(dtoList);
    }

    @Log
    @PostMapping("/update")
    @ApiOperation("新增或者修改变量")
    public R<DcTemplateVariable> list(@RequestBody DcTemplateVariable dcTemplateVariable) {
        dcTemplateVariableService.saveOrUpdate(dcTemplateVariable);
        return R.ok(dcTemplateVariable);
    }

    @Log
    @DeleteMapping("/{id}")
    @ApiOperation("删除变量")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteType(@PathVariable String id) {
        dcTemplateVariableService.removeById(id);
        return R.ok(Boolean.TRUE);
    }
}
