package cn.bctools.auth.controller.platform;


import cn.bctools.auth.entity.LoginRules;
import cn.bctools.auth.entity.Opinion;
import cn.bctools.auth.service.LoginRulesService;
import cn.bctools.auth.service.OpinionService;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 *
 */
@Api(tags = "登录规则管理")
@RestController
@RequestMapping("/login/rules")
@AllArgsConstructor
public class LoginRulesController {
    private final LoginRulesService rulesService;

    @Log
    @ApiOperation(value = "分页")
    @GetMapping("/page")
    public R<Page<LoginRules>> page(Page<LoginRules> page, LoginRules vo) {
        rulesService.page(page, Wrappers.query(vo).lambda());
        return R.ok(page);
    }

    @Log
    @ApiOperation(value = "删除")
    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(rulesService.removeById(id));
    }

    @Log
    @ApiOperation(value = "新增")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody LoginRules vo) {
        return R.ok(rulesService.save(vo));
    }

    @Log
    @ApiOperation(value = "修改")
    @PutMapping("/update")
    public R<Boolean> update(@RequestBody LoginRules vo) {
        return R.ok(rulesService.updateById(vo));
    }

}
