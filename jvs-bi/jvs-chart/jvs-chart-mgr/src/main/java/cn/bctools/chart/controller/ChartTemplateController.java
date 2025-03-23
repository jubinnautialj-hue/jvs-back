package cn.bctools.chart.controller;

import cn.bctools.chart.chart.bo.ChartSettingBo;
import cn.bctools.chart.dto.TemplateUseDto;
import cn.bctools.chart.dto.TypeAndCoverDto;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.entity.ChartTemplate;
import cn.bctools.chart.entity.JvsTemplateTypeRelation;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.chart.service.ChartTemplateService;
import cn.bctools.chart.service.JvsTemplateTypeRelationService;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "图表-模板")
@RestController
@AllArgsConstructor
@RequestMapping("/template")
@Slf4j
public class ChartTemplateController {
    private final ChartPageService chartPageService;
    private final ChartTemplateService chartTemplateService;
    private final JvsTemplateTypeRelationService jvsTemplateTypeRelationService;
    private final OssTemplate ossTemplate;

    @Log
    @ApiOperation("发布为模板")
    @PostMapping("/publish/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<ChartTemplate> publishTemplate(@ApiParam("图表id") @PathVariable String id, @RequestBody TypeAndCoverDto typeAndCoverDto) {
        //删除数据源和过滤条件
        List<JSONObject> setting = JSONObject.parseArray(typeAndCoverDto.getDataJson())
                .stream()
                .map(e -> {
                    JSONObject jsonObject = JSONObject.parseObject(e.toString());
                    if (jsonObject.containsKey("setting")) {
                        JSONObject settingJson = jsonObject.getJSONObject("setting");
                        settingJson.remove(Get.name(ChartSettingBo::getDataFilterJson));
                        settingJson.remove(Get.name(ChartSettingBo::getDataSource));
                        settingJson.remove(Get.name(ChartSettingBo::getSearchFilterJson));
                    }
                    return jsonObject;
                }).collect(Collectors.toList());
        ChartTemplate template = new ChartTemplate();
        template.setCoverBucketName(typeAndCoverDto.getCoverBucketName())
                .setName(typeAndCoverDto.getName())
                .setId(null)
                .setDataJson(JSONObject.toJSONString(setting))
                .setDescription(typeAndCoverDto.getDescription())
                .setCoverFilePath(typeAndCoverDto.getCoverFilePath());
        //删除原有绑定的条件与数据源配置
        chartTemplateService.save(template);
        //绑定关系入库
        JvsTemplateTypeRelation jvsTemplateTypeRelation = new JvsTemplateTypeRelation()
                .setTemplateId(template.getId())
                .setTypeId(typeAndCoverDto.getTypeId());
        jvsTemplateTypeRelationService.save(jvsTemplateTypeRelation);
        return R.ok(template);
    }

    @Log
    @ApiOperation("新建模板")
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public R<ChartTemplate> add(@RequestBody TypeAndCoverDto typeAndCoverDto) {
        ChartTemplate template = new ChartTemplate();
        //设置模板封面
        template.setCoverBucketName(typeAndCoverDto.getCoverBucketName())
                .setName(typeAndCoverDto.getName())
                .setCoverFilePath(typeAndCoverDto.getCoverFilePath());
        chartTemplateService.save(template);
        //绑定关系入库
        //绑定关系入库
        JvsTemplateTypeRelation typeRelation = new JvsTemplateTypeRelation()
                .setTemplateId(template.getId())
                .setTypeId(typeAndCoverDto.getTypeId());
        jvsTemplateTypeRelationService.save(typeRelation);
        return R.ok(template);
    }

    @Log
    @ApiOperation("修改模板")
    @PostMapping("/update")
    public R<ChartTemplate> update(@RequestBody ChartTemplate chartTemplate) {
        chartTemplateService.updateById(chartTemplate);
        return R.ok(chartTemplate);
    }

    @Log
    @ApiOperation("使用模板")
    @PostMapping("/use")
    public R<ChartPage> use(@RequestBody TemplateUseDto templateUseDto) {
        ChartTemplate byId = chartTemplateService.getById(templateUseDto.getTemplateId());
        ChartPage copy = BeanCopyUtil.copy(byId, ChartPage.class);
        copy.setType(templateUseDto.getMenuId())
                .setName(templateUseDto.getChartName())
                .setId(null);
        //用户信息制空
        copy.setCreateById(null);
        copy.setCreateBy(null);
        copy.setUpdateBy(null);
        copy.setCreateTime(null);
        copy.setUpdateTime(null);
        chartPageService.save(copy);
        return R.ok(copy);
    }

    @Log
    @ApiOperation("获取详细")
    @PostMapping("/get/{id}")
    public R<ChartTemplate> getById(@ApiParam("模板id") @PathVariable String id) {
        ChartTemplate chartTemplate = chartTemplateService.getById(id);
        return R.ok(chartTemplate);
    }


    @Log
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
        if (!page.getRecords().isEmpty()) {
            page.getRecords().stream().filter(e -> StrUtil.isAllNotBlank(e.getCoverFilePath(), e.getCoverBucketName())).peek(e -> e.setCoverUrl(ossTemplate.fileLink(e.getCoverFilePath(), e.getCoverBucketName()))).collect(Collectors.toList());
        }
        return R.ok(page);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete(@ApiParam("模板id") @PathVariable String id) {
        chartTemplateService.removeById(id);
        jvsTemplateTypeRelationService.remove(new LambdaQueryWrapper<JvsTemplateTypeRelation>().eq(JvsTemplateTypeRelation::getTemplateId, id));
        return R.ok(Boolean.TRUE);
    }


    @Log
    @ApiOperation("修改分类或者封面")
    @PostMapping("/update/type/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> updateType(@ApiParam("模板id") @PathVariable String id, @RequestBody TypeAndCoverDto typeAndCoverDto) {
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
