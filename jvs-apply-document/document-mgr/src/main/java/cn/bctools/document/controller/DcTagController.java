package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcTag;
import cn.bctools.document.entity.DcTagBinding;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcTagBindingService;
import cn.bctools.document.service.DcTagService;
import cn.bctools.document.service.DocumentElasticService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "标签")
@RestController
@AllArgsConstructor
@RequestMapping("/dc/tg")
public class DcTagController {
    private final DcTagService dcTagService;
    private final DcTagBindingService bindingService;
    private final DcLibraryService dcLibraryService;
    private final DocumentElasticService documentElasticService;
    private final DcTagBindingService dcTagBindingService;

    @Log
    @ApiOperation("获取所有标签 如果查询条件给了 文档id 那么就会标识 那些数据是当前文档选中的")
    @GetMapping("/labels")
    public R<Page<DcTag>> labels(Page<DcTag> page, DcTag tag) {
        LambdaQueryWrapper<DcTag> wrapper = Wrappers.<DcTag>lambdaQuery()
                .like(StrUtil.isNotBlank(tag.getTagName()), DcTag::getTagName, tag.getTagName())
                .orderByDesc(BasalPo::getCreateTime);
        dcTagService.page(page, wrapper);
        if (StrUtil.isNotBlank(tag.getDcId())) {
            List<String> tagIds = bindingService.list(new LambdaQueryWrapper<DcTagBinding>().eq(DcTagBinding::getDcId, tag.getDcId()))
                    .parallelStream()
                    .map(DcTagBinding::getTagId)
                    .collect(Collectors.toList());
            page.getRecords().stream().peek(e -> e.setIsSelect(tagIds.parallelStream().anyMatch(v -> v.equals(e.getId())))).collect(Collectors.toList());
        }
        return R.ok(page);
    }

    @Log
    @ApiOperation("修改或新增单条记录")
    @PostMapping("/update/one")
    @Transactional(rollbackFor = Exception.class)
    public R<DcTag> update(@RequestBody DcTag dcTag) {
        dcTagService.saveOrUpdate(dcTag);
        return R.ok(dcTag);
    }
    @Log
    @ApiOperation("修改或新增")
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public R<List<DcTag>> update(@RequestBody List<DcTag> dcTag) {
        //获取原来的所有
        List<DcTag> list = dcTagService.list();
        //新增的数据
        List<DcTag> add = dcTag.parallelStream().filter(e -> StrUtil.isBlank(e.getId())).collect(Collectors.toList());
        //修改的数据
        List<DcTag> update = dcTag.parallelStream().filter(e -> StrUtil.isNotBlank(e.getId())).collect(Collectors.toList());
        //删除的数据
        List<String> ids = update.parallelStream().map(DcTag::getId).collect(Collectors.toList());
        List<String> delete = list.parallelStream().map(DcTag::getId).filter(id -> !ids.contains(id)).collect(Collectors.toList());
        dcTagService.saveBatch(add);
        dcTagService.updateBatchById(update);
        dcTagService.removeByIds(delete);
        return R.ok(dcTag);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del/{tagId}")
    public R update(@ApiParam("标签id")@PathVariable("tagId")String tagId) {
        //删除数据
        dcTagService.removeById(tagId);
        //删除绑定关系
        dcTagBindingService.remove(Wrappers.<DcTagBinding>lambdaQuery().eq(DcTagBinding::getTagId,tagId));
        return R.ok();
    }

    @Log
    @ApiOperation("文档绑定标签")
    @PostMapping("/tag/binding/{dcId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> tagBinding(@ApiParam(value = "文档id", required = true) @PathVariable String dcId, @RequestBody DcTag dcTag) {
        //查询当前文档绑定的标签
        LambdaQueryWrapper<DcTagBinding> eq = Wrappers.<DcTagBinding>lambdaQuery().eq(DcTagBinding::getDcId, dcId);
        List<String> tagIds = dcTagBindingService.list(eq)
                .stream().map(DcTagBinding::getTagId).collect(Collectors.toList());
        //选中则新增标签id
        if(dcTag.getIsSelect()){
            tagIds.add(dcTag.getId());
        }else{
            tagIds.removeIf(e -> StrUtil.equals(dcTag.getId(),e));
        }
        //重新新增 先删再增
        dcTagBindingService.remove(eq);
        List<String> tagName = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(tagIds)){
            List<DcTagBinding> collect = tagIds.stream().distinct().map(e -> new DcTagBinding().setDcId(dcId).setTagId(e)).collect(Collectors.toList());
            dcTagBindingService.saveBatch(collect);
            //设置es中该文档的tagName
            tagName = dcTagService.listByIds(tagIds).stream().map(DcTag::getTagName).collect(Collectors.toList());
        }

        DcLibrary dcLibrary = dcLibraryService.getById(dcId);
        Document document = Document.create();
        document.put(Get.name(DocumentEsPo::getTagName), tagName.parallelStream().collect(Collectors.joining(" ")));
        documentElasticService.update(dcLibrary, document);
        return R.ok(Boolean.TRUE);
    }

}
