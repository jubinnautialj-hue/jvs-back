package cn.bctools.screen.controller;

import cn.bctools.screen.dto.TemplateUseDto;
import cn.bctools.screen.dto.TypeAndCoverDto;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.entity.ChartTemplate;
import cn.bctools.screen.entity.JvsChartPageCanvas;
import cn.bctools.screen.entity.JvsTemplateTypeRelation;
import cn.bctools.screen.service.ChartPageService;
import cn.bctools.screen.service.ChartTemplateService;
import cn.bctools.screen.service.JvsChartPageCanvasService;
import cn.bctools.screen.service.JvsTemplateTypeRelationService;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "大屏-模板")
@RestController
@AllArgsConstructor
@RequestMapping("/template")
@Slf4j
public class ChartTemplateController {
    private final ChartPageService chartPageService;
    private final ChartTemplateService chartTemplateService;
    private final JvsTemplateTypeRelationService jvsTemplateTypeRelationService;
    private final OssTemplate ossTemplate;

    private final JvsChartPageCanvasService jvsChartPageCanvasService;


    @ApiOperation("发布为模板")
    @PostMapping("/publish/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<ChartTemplate> publishTemplate(@ApiParam("大屏id") @PathVariable String id, @RequestBody @Validated TypeAndCoverDto typeAndCoverDto) {

        ChartTemplate template = BeanUtil.copyProperties(typeAndCoverDto, ChartTemplate.class,Get.name(ChartTemplate::getId));

        //删除原有绑定的条件与数据源配置
        chartTemplateService.save(template);
        //绑定关系入库
        JvsTemplateTypeRelation jvsTemplateTypeRelation = new JvsTemplateTypeRelation()
                .setTemplateId(template.getId())
                .setTypeId(typeAndCoverDto.getTypeId());
        jvsTemplateTypeRelationService.save(jvsTemplateTypeRelation);

        if(CollectionUtil.isNotEmpty(typeAndCoverDto.getCanvasList())){
            typeAndCoverDto.getCanvasList().forEach(e -> e.setId(null));
            //保存画布
            jvsChartPageCanvasService.saveTemplate(template.getId(),typeAndCoverDto.getCanvasList(), null);
        }
        return R.ok(template);
    }


    @ApiOperation("新建模板")
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public R<ChartTemplate> add(@RequestBody  @Validated TypeAndCoverDto typeAndCoverDto) {
        ChartTemplate template = new ChartTemplate();
        BeanUtil.copyProperties(typeAndCoverDto,template);
        chartTemplateService.save(template);
        //设置模板封面
        template.setCoverBucketName(typeAndCoverDto.getCoverBucketName())
                .setName(typeAndCoverDto.getName())
                .setCoverFilePath(typeAndCoverDto.getCoverFilePath());
        //绑定关系入库
        JvsTemplateTypeRelation jvsTemplateTypeRelation = new JvsTemplateTypeRelation()
                .setTemplateId(template.getId())
                .setTypeId(typeAndCoverDto.getTypeId());
        jvsTemplateTypeRelationService.save(jvsTemplateTypeRelation);
        //保存画布
        jvsChartPageCanvasService.saveTemplate(template.getId(),typeAndCoverDto.getCanvasList(),null);
        return R.ok(template);
    }


    @ApiOperation("修改模板")
    @PostMapping("/update")
    public R<ChartTemplate> update(@RequestBody @Validated ChartTemplate chartTemplate) {
        chartTemplateService.updateById(chartTemplate);
        jvsChartPageCanvasService.saveTemplate(chartTemplate.getId(),chartTemplate.getCanvasList(),chartTemplate.getDeleteCanvasIds());
        return R.ok(chartTemplate);
    }


    @ApiOperation("使用模板")
    @PostMapping("/use")
    public R<ChartPage> use(@RequestBody TemplateUseDto templateUseDto) {
        ChartTemplate byId = chartTemplateService.getById(templateUseDto.getTemplateId());
        ChartPage copy = BeanCopyUtil.copy(byId, ChartPage.class);
        copy.setType(templateUseDto.getMenuId())
                .setName(templateUseDto.getChartName())
                .setId(null);
        //用户信息置空
        copy.setCreateById(null);
        copy.setCreateBy(null);
        copy.setUpdateBy(null);
        copy.setCreateTime(null);
        copy.setUpdateTime(null);
        chartPageService.save(copy);
        //复制画布
        jvsChartPageCanvasService.copy(templateUseDto.getTemplateId(),copy.getId());
        return R.ok(copy);
    }


    @ApiOperation("获取详细")
    @PostMapping("/get/{id}")
    public R<ChartTemplate> getById(@ApiParam("模板id") @PathVariable String id) {
        ChartTemplate chartTemplate = chartTemplateService.getById(id);
        if(ObjectUtil.isNull(chartTemplate)){
            return R.failed("该大屏设计未找到");
        }
        if(StrUtil.isNotBlank(chartTemplate.getCoverFilePath()) && StrUtil.isNotBlank(chartTemplate.getCoverBucketName())){
            chartTemplate.setCoverUrl(ossTemplate.fileLink(chartTemplate.getCoverFilePath(),chartTemplate.getCoverBucketName()));
        }
        List<JvsChartPageCanvas> list = jvsChartPageCanvasService.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).select(JvsChartPageCanvas.class, e -> !StrUtil.equals(e.getProperty(), Get.name(JvsChartPageCanvas::getDataJson))).eq(JvsChartPageCanvas::getChartId, id));
        chartTemplate.setCanvasList(list);
        return R.ok(chartTemplate);
    }

    @ApiOperation("获取分页数据")
    @GetMapping("/get/menu/{typeId}")
    public R<Page<ChartTemplate>> getMenu(@ApiParam("分类id如果需要查询全部数据就直接传-1") @PathVariable String typeId, Page<ChartTemplate> page, ChartTemplate chartTemplate) {
        LambdaQueryWrapper<ChartTemplate> queryWrapper = new LambdaQueryWrapper<ChartTemplate>()
                .like(StrUtil.isNotBlank(chartTemplate.getName()), ChartTemplate::getName, chartTemplate.getName());
        if ("-1".equals(typeId)) {
            chartTemplateService.page(page, queryWrapper);
        } else {
            List<JvsTemplateTypeRelation> list = jvsTemplateTypeRelationService.list(new LambdaQueryWrapper<JvsTemplateTypeRelation>().eq(JvsTemplateTypeRelation::getTypeId, typeId));
            if (!list.isEmpty()) {
                List<String> ids = list.stream().map(JvsTemplateTypeRelation::getTemplateId).collect(Collectors.toList());
                queryWrapper.in(ChartTemplate::getId, ids);
                chartTemplateService.page(page, queryWrapper);
            }
        }
        //设置封面url
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            page.getRecords().stream().filter(e->StrUtil.isAllNotBlank(e.getCoverFilePath(),e.getCoverBucketName())).peek(e -> e.setCoverUrl(ossTemplate.fileLink(e.getCoverFilePath(), e.getCoverBucketName()))).collect(Collectors.toList());
        }
        return R.ok(page);
    }


    @ApiOperation("删除")
    @DeleteMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete(@ApiParam("模板id") @PathVariable String id) {
        chartTemplateService.removeById(id);
        jvsTemplateTypeRelationService.remove(new LambdaQueryWrapper<JvsTemplateTypeRelation>().eq(JvsTemplateTypeRelation::getTemplateId, id));
        jvsChartPageCanvasService.removeByParentId(id);
        return R.ok(Boolean.TRUE);
    }


    @ApiOperation("修改分类或者封面")
    @PostMapping("/update/type/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> updateType(@ApiParam("模板id") @PathVariable String id, @RequestBody @Validated TypeAndCoverDto typeAndCoverDto) {
        //修改封面
        chartTemplateService.update(new UpdateWrapper<ChartTemplate>().lambda()
                .set(ChartTemplate::getCoverBucketName, typeAndCoverDto.getCoverBucketName())
                .set(ChartTemplate::getCoverFilePath, typeAndCoverDto.getCoverFilePath())
                .eq(ChartTemplate::getId, id));
        //修改分类
        //先删除所有绑定关系 重新入库
        jvsTemplateTypeRelationService.remove(new LambdaQueryWrapper<JvsTemplateTypeRelation>().eq(JvsTemplateTypeRelation::getTemplateId, id));
        //绑定关系入库
        JvsTemplateTypeRelation jvsTemplateTypeRelation = new JvsTemplateTypeRelation()
                .setTemplateId(id)
                .setTypeId(typeAndCoverDto.getTypeId());
        jvsTemplateTypeRelationService.save(jvsTemplateTypeRelation);
        return R.ok(Boolean.TRUE);
    }

}
