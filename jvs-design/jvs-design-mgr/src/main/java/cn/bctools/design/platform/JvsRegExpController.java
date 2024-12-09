package cn.bctools.design.platform;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.crud.entity.JvsRegExp;
import cn.bctools.design.crud.service.JvsRegExpService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author : GaoZeXi
 */
@Slf4j
@AllArgsConstructor
@Api(value = "正则字典管理", tags = "正则字典管理")
@RestController
@RequestMapping("/platform/app/regexp")
public class JvsRegExpController {

    JvsRegExpService sysRegExpService;

    @Log
    @ApiOperation(value = "分页查询正则")
    @GetMapping("/list")
    public R<Page<JvsRegExp>> list(Page<JvsRegExp> page, JvsRegExp sysRegExp) {
        String name = sysRegExp.getName();
        String type = sysRegExp.getType();
        sysRegExpService.page(page, Wrappers.<JvsRegExp>lambdaQuery()
                .eq(StringUtils.isNotBlank(type), JvsRegExp::getType, type)
                .like(StringUtils.isNotBlank(name), JvsRegExp::getName, name));
        return R.ok(page);
    }

    @Log
    @ApiOperation(value = "查询分类")
    @GetMapping("/types")
    public R<Set<String>> types() {
        List<JvsRegExp> expList = sysRegExpService.list(Wrappers.<JvsRegExp>lambdaQuery().select(JvsRegExp::getType));
        Set<String> typeSet = expList.stream().map(JvsRegExp::getType).filter(Objects::nonNull).collect(Collectors.toSet());
        return R.ok(typeSet);
    }

    @ApiOperation(value = "新增正则", notes = "名称不允许重复")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<JvsRegExp> addRegExp(@RequestBody @Validated JvsRegExp sysRegExp) {
        String name = sysRegExp.getName();
        long count = sysRegExpService.count(Wrappers.<JvsRegExp>lambdaQuery().eq(JvsRegExp::getName, name));
        if (count > 0) {
            throw new BusinessException("新增失败名称重复", name);
        }
        log.info("新增正则: {}", JSONObject.toJSONString(sysRegExp));
        sysRegExp.setUniqueName(UUID.randomUUID().toString());
        sysRegExpService.save(sysRegExp);
        return R.ok(sysRegExp);
    }

    @ApiOperation(value = "修改正则")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R<?> updateRegexp(@RequestBody @Validated JvsRegExp sysRegExp) {
        if (StringUtils.isBlank(sysRegExp.getId())) {
            return R.failed("必选参数缺失id");
        }
        // 不允许修改该字段
        sysRegExp.setUniqueName(null);
        sysRegExpService.updateById(sysRegExp);
        String name = sysRegExp.getName();
        long count = sysRegExpService.count(Wrappers.<JvsRegExp>lambdaQuery().eq(JvsRegExp::getName, name));
        if (count > 1) {
            return R.failed("修改失败名称重复", name);
        }
        return R.ok("修改成功");
    }

    @ApiOperation(value = "删除正则")
    @DeleteMapping("/{id}")
    public R<?> deleteById(@PathVariable("id") String id) {
        sysRegExpService.removeById(id);
        return R.ok("删除成功");
    }

}
