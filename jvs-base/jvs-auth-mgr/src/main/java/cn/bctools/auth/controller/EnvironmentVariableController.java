package cn.bctools.auth.controller;

import cn.bctools.auth.api.enums.EnvironmentVariableEnum;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.auth.entity.EnvironmentVariable;
import cn.bctools.auth.service.EnvironmentVariableService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * The type Environment variable controller.
 *
 * @author jvs 环境变量，用于公式，或逻辑获取字段的环境Key
 */
@Slf4j
@AllArgsConstructor
@Api(value = "环境变量", tags = "环境变量")
@RestController
@RequestMapping("/environment/variable")
public class EnvironmentVariableController {

    /**
     * The Service.
     */
    EnvironmentVariableService service;
    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;

    /**
     * Create r.
     *
     * @param vo the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "新增")
    @PostMapping
    public R<String> create(@Validated @RequestBody EnvironmentVariable vo) {
        //判断是否重复
        long count = service.count(Wrappers.query(new EnvironmentVariable().setMode(vo.getMode()).setLabel(vo.getLabel())));
        if (count > 0) {
            return R.failed("数据已经存在");
        }
        service.save(vo);
        redisUtils.publish("jvsEnvironment", null);
        return R.ok();
    }

    /**
     * Edit r.
     *
     * @param vo the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "修改")
    @PutMapping
    public R<String> edit(@Validated @RequestBody EnvironmentVariable vo) {
        //判断是否重复
        EnvironmentVariable count = service.getOne(Wrappers.query(new EnvironmentVariable().setMode(vo.getMode()).setLabel(vo.getLabel())));
        if (ObjectNull.isNotNull(count) && !vo.getId().equals(count.getId())) {
            return R.failed("数据已经存在");
        }
        service.updateById(vo);
        redisUtils.publish("jvsEnvironment", null);

        return R.ok();
    }

    /**
     * Del r.
     *
     * @param id the id
     * @return the r
     */
    @Log
    @ApiOperation(value = "删除")
    @DeleteMapping("/{id}")
    public R<String> del(@PathVariable("id") String id) {
        service.removeById(id);
        //刷新缓存
        redisUtils.publish("jvsEnvironment", null);
        return R.ok();
    }

    /**
     * Page r.
     *
     * @param page the page
     * @param vo   the vo
     * @return the r
     */
    @Log
    @ApiOperation(value = "分页")
    @GetMapping("/page")
    public R<Page<EnvironmentVariable>> page(Page<EnvironmentVariable> page, EnvironmentVariable vo) {
        LambdaQueryWrapper<EnvironmentVariable> query = Wrappers.<EnvironmentVariable>lambdaQuery()
                .eq(ObjectNull.isNotNull(vo.getMode()), EnvironmentVariable::getMode, vo.getMode())
                .like(StringUtils.isNotBlank(vo.getLabel()), EnvironmentVariable::getLabel, vo.getLabel());
        service.page(page, query);
        return R.ok(page);
    }

    /**
     * All r.
     *
     * @param type the type
     * @return the r
     */
    @ApiOperation(value = "根据类型获取环境变量")
    @GetMapping("/all/{type}/{mode}")
    public R<List<String>> all(@PathVariable("type") EnvironmentVariableEnum type, @PathVariable("mode") ModeTypeEnum mode) {
        List<String> list = service.list(Wrappers.query(new EnvironmentVariable().setType(type).setMode(mode))).stream().map(EnvironmentVariable::getLabel).collect(Collectors.toList());
        return R.ok(list);
    }

}
