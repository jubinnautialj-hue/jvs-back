package cn.bctools.design.crud;

import cn.bctools.auth.api.api.DictApi;
import cn.bctools.auth.api.dto.SysDictDto;
import cn.bctools.auth.api.dto.SysDictItemDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.sensitive.SensitiveInfoUtils;
import cn.bctools.database.util.WrapperUtil;
import cn.bctools.design.crud.entity.JvsRegExp;
import cn.bctools.design.crud.service.JvsRegExpService;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.design.util.DynamicFieldAttributeUtils;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
@Api(tags = "表单设计")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/base/form/design")
public class FormBaseController {

    DictApi dictApi;
    JvsTreeService treeService;
    JvsRegExpService regExpService;

    @GetMapping("/dicts")
    @ApiOperation("获取字典")
    public R<List<SysDictDto>> dict(@RequestParam(value = "description", required = false) String description) {
        return dictApi.list(description);
    }

    @GetMapping("/items")
    @ApiOperation("获取字典")
    public R<List<SysDictItemDto>> listItems(@RequestParam(value = "uniqId", required = false) String uniqId) {
        return dictApi.listItems(uniqId);
    }

    @GetMapping("/tree/{uniqueName}")
    @ApiOperation("获取字典树")
    public R<Map<String, Object>> tree(@PathVariable("uniqueName") String uniqueName) {
        return R.ok(treeService.getByUniqueName(uniqueName));
    }

    /**
     * 脱敏选择项
     */
    @ApiOperation("脱敏选择项")
    @GetMapping("/sensitive")
    public R sensitive() {
        return R.ok(SensitiveInfoUtils.map);
    }

    /**
     * 此功能与
     * @return
     */
    @ApiOperation("动态表单组件的属性列表")
    @GetMapping("/dynamic/field/attribute")
    public R dynamicAttribute() {
        List list = DynamicFieldAttributeUtils.get();
        return R.ok(list);
    }

    @GetMapping("/regexp")
    @ApiOperation("获取正则列表页")
    public R<List<JvsRegExp>> regexp(@RequestParam(required = false, defaultValue = "") String name) {
        LambdaQueryWrapper<JvsRegExp> queryWrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(JvsRegExp::getName, WrapperUtil.parseLike(name));
        }
        List<JvsRegExp> list = regExpService.list(queryWrapper);
        return R.ok(list);
    }


}
