package cn.bctools.screen.controller;

import cn.bctools.common.utils.R;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.screen.dto.BatchMaterialDto;
import cn.bctools.screen.dto.BatchRemoveMaterialDto;
import cn.bctools.screen.dto.MaterialDto;
import cn.bctools.screen.dto.MaterialTypeDto;
import cn.bctools.screen.entity.JvsMaterial;
import cn.bctools.screen.service.JvsMaterialService;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 素材管理 前端控制器
 * </p>
 *
 * @author admin
 * @since 2023-09-14
 */
@Api(tags = "大屏-素材管理")
@RestController
@RequestMapping("/material")
@AllArgsConstructor
@Slf4j
public class JvsMaterialController {

    private final OssTemplate ossTemplate;
    private final JvsMaterialService jvsMaterialService;
    private static final String key = "MaterialDto";
    private final TimedCache<String,MaterialDto> cache = new TimedCache<>(8*60*60*1000);

    @ApiOperation("查询素材")
    @GetMapping("/find")
    public R<MaterialDto> find(String search){
        MaterialDto materialDto;
        if (cache.containsKey(key)) {
            materialDto = cache.get(key);
        }else{
            List<JvsMaterial> list = jvsMaterialService.list(Wrappers.lambdaQuery(JvsMaterial.class)
                    .orderByDesc(BasalPo::getCreateTime)
            );
            list.forEach(e -> e.setFileLink(ossTemplate.fileLink(e.getFileName(),e.getBucketName())));
            LinkedHashMap<String, List<JvsMaterial>> jvsMaterialMap = list.stream().collect(Collectors.groupingBy(JvsMaterial::getType, LinkedHashMap::new, Collectors.toList()));
            List<MaterialTypeDto> collect = jvsMaterialMap.keySet().stream().map(e -> new MaterialTypeDto().setId(SecureUtil.md5(e)).setName(e).setMaterialList(jvsMaterialMap.get(e))).collect(Collectors.toList());
            Set<String> types = jvsMaterialMap.keySet();
            materialDto = new MaterialDto().setMaterialMenu(collect).setType(types);
            cache.put(key,materialDto);
        }
        return R.ok(materialDto);
    }

    @ApiOperation("按类型分页查询数据")
    @GetMapping("/page/{type}")
    public R<Page<JvsMaterial>> pageByType(Page<JvsMaterial> page, @ApiParam("类型") @PathVariable("type") String type){
        if("全部".equals(type)){
            type = null;
        }
        LambdaQueryWrapper<JvsMaterial> queryWrapper = Wrappers.lambdaQuery(JvsMaterial.class)
                .eq(StrUtil.isNotBlank(type),JvsMaterial::getType, type)
                .orderByDesc(BasalPo::getCreateTime);
        jvsMaterialService.page(page,queryWrapper);
        page.getRecords().forEach(e -> e.setFileLink(ossTemplate.fileLink(e.getFileName(),e.getBucketName())));
        return R.ok(page);
    }

    @ApiOperation("查询所有的素材分类")
    @GetMapping("/list/material/type")
    public R<List<String>> listMaterialType(){
        List<String> type = jvsMaterialService.list(Wrappers.lambdaQuery(JvsMaterial.class).select(JvsMaterial::getType)).stream().map(JvsMaterial::getType).distinct().collect(Collectors.toList());
        return R.ok(type);
    }

    @ApiOperation("保存素材")
    @PostMapping("/save")
    public R<JvsMaterial> save(@Validated@RequestBody JvsMaterial dto){
        jvsMaterialService.save(dto);
        cache.remove(key);
        return R.ok(dto);
    }

    @ApiOperation("批量保存素材")
    @PostMapping("/batch/save")
    public R<List<JvsMaterial>> saveBatch(@Validated @RequestBody BatchMaterialDto dto){
        List<JvsMaterial> materialList = dto.getMaterialList();
        materialList.forEach(e ->e.setType(dto.getType()));
        jvsMaterialService.saveBatch(materialList);
        cache.remove(key);
        return R.ok(materialList);
    }

    @ApiOperation("删除素材")
    @DeleteMapping("/{id}/remove")
    public R remove(@ApiParam("素材id") @PathVariable("id")String id){
        jvsMaterialService.removeById(id);
        cache.remove(key);
        return R.ok();
    }

    @ApiOperation("批量删除素材")
    @DeleteMapping("/batch/remove")
    public R remove(@RequestBody BatchRemoveMaterialDto dto){
        if(StrUtil.isNotBlank(dto.getType())){
            jvsMaterialService.remove(Wrappers.lambdaQuery(JvsMaterial.class).eq(JvsMaterial::getType,dto.getType()));
        }
        if(CollectionUtil.isNotEmpty(dto.getMaterialIds())){
            jvsMaterialService.removeByIds(dto.getMaterialIds());
        }
        cache.remove(key);
        return R.ok();
    }

    @ApiOperation("重命名")
    @PutMapping("/rename")
    public R rename(@RequestBody JvsMaterial dto){
        jvsMaterialService.update(Wrappers.lambdaUpdate(JvsMaterial.class).set(JvsMaterial::getName,dto.getName()).eq(JvsMaterial::getId,dto.getId()));
        cache.remove(key);
        return R.ok();
    }
}
