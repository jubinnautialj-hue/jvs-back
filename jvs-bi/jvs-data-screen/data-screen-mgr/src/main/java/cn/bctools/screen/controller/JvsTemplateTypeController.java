package cn.bctools.screen.controller;

import cn.bctools.screen.entity.JvsTemplateType;
import cn.bctools.screen.entity.JvsTemplateTypeRelation;
import cn.bctools.screen.service.JvsTemplateTypeRelationService;
import cn.bctools.screen.service.JvsTemplateTypeService;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "大屏-模板分类")
@RestController
@AllArgsConstructor
@RequestMapping("/type")
@Slf4j
public class JvsTemplateTypeController {
    private final JvsTemplateTypeService jvsTemplateTypeService;
    private final JvsTemplateTypeRelationService jvsTemplateTypeRelationService;



    @ApiOperation("获取模板所有分类")
    @GetMapping("/list/{isCount}")
    public R<List<JvsTemplateType>> getList(@ApiParam("是否需要统计总条数") @PathVariable Boolean isCount) {
        List<JvsTemplateType> list = jvsTemplateTypeService.list();
        if (isCount) {
            //统计条数
            Map<String, Long> map = jvsTemplateTypeRelationService.list()
                    .stream().collect(Collectors.groupingBy(JvsTemplateTypeRelation::getTypeId, Collectors.counting()));
            list.forEach(e -> e.setCount(map.getOrDefault(e.getId(), 0L)));
        }
        return R.ok(list);
    }


    @ApiOperation("添加模板分类")
    @PostMapping("/add")
    public R<JvsTemplateType> add(@RequestBody JvsTemplateType jvsTemplateType) {
        jvsTemplateTypeService.save(jvsTemplateType);
        return R.ok(jvsTemplateType);
    }


    @ApiOperation("删除")
    @DeleteMapping("/delete/{id}")
    public R deleteMenu(@PathVariable String id) {
        //删除分类需要先判断此分类下面是否存在模板数据
        long count = jvsTemplateTypeRelationService.count(new LambdaQueryWrapper<JvsTemplateTypeRelation>().eq(JvsTemplateTypeRelation::getTypeId, id));
        if (count > BigDecimal.ROUND_UP) {
            return R.failed("模板存在绑定数据,请先解绑在删除");
        }
        jvsTemplateTypeService.removeById(id);
        return R.ok();
    }


    @ApiOperation("修改名称")
    @PostMapping("/update/name")
    public R<JvsTemplateType> update(@RequestBody JvsTemplateType jvsTemplateType) {
        jvsTemplateTypeService.updateById(jvsTemplateType);
        return R.ok(jvsTemplateType);
    }
}
