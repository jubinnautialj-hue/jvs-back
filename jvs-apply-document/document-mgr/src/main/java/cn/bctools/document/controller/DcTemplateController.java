package cn.bctools.document.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.entity.DcTemplateType;
import cn.bctools.document.entity.DcTemplateVariable;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.TemplateTypeEnum;
import cn.bctools.document.office.OfficeContentUtil;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcTemplateService;
import cn.bctools.document.service.DcTemplateVariableService;
import cn.bctools.document.service.impl.DcTemplateTypeServiceImpl;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文档模板
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "文档模板")
@RestController
@RequestMapping(value = "/dcLibrary/template")
@AllArgsConstructor
public class DcTemplateController {

    private final OssTemplate ossTemplate;
    private final DcTemplateService dcTemplateService;
    private final DcTemplateTypeServiceImpl dcTemplateTypeService;
    private final DcTemplateVariableService dcTemplateVariableService;
    private final DcLibraryService dcLibraryService;

    @Log
    @GetMapping("/type")
    @ApiOperation("获取所有分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateType", value = "模板类型 文本 变量")
    })
    public R<List<DcTemplateType>> list(TemplateTypeEnum templateType) {
        LambdaQueryWrapper<DcTemplateType> queryWrapper = new LambdaQueryWrapper<DcTemplateType>().orderByDesc(DcTemplateType::getCreateTime);
        if (Optional.ofNullable(templateType).isPresent()) {
            queryWrapper.eq(DcTemplateType::getTemplateType, templateType);
        }
        List<DcTemplateType> list = dcTemplateTypeService.list(queryWrapper);
        return R.ok(list);
    }

    @Log
    @PostMapping("/add/type")
    @ApiOperation("新增分类")
    public R<Boolean> list(@RequestBody DcTemplateType dcTemplateType) {
        dcTemplateTypeService.saveOrUpdate(dcTemplateType);
        return R.ok(Boolean.TRUE);
    }

    @Log
    @DeleteMapping("/type/{id}")
    @ApiOperation("删除分类")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteType(@PathVariable String id) {
        dcTemplateTypeService.removeById(id);
        dcTemplateService.remove(new LambdaQueryWrapper<DcTemplate>().eq(DcTemplate::getTypeId, id));
        return R.ok(Boolean.TRUE);
    }


    @Log
    @PostMapping("/save/path")
    @ApiOperation("修改动态模板生成的位置")
    public R<Boolean> savePath(@RequestBody DcTemplate dcTemplate) {
        dcTemplateService.update(new UpdateWrapper<DcTemplate>().lambda().eq(DcTemplate::getId, dcTemplate.getId()).set(DcTemplate::getSavePathId, dcTemplate.getSavePathId()));
        return R.ok(Boolean.TRUE);
    }

    @Log
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public R<Page<DcTemplate>> page(Page<DcTemplate> page, DcTemplate dcTemplate) {
        LambdaQueryWrapper<DcTemplate> queryWrapper = new LambdaQueryWrapper<DcTemplate>()
                .orderByDesc(DcTemplate::getCreateTime)
                .eq(ObjectUtil.isNotNull(dcTemplate.getTemplateType()), DcTemplate::getTemplateType, dcTemplate.getTemplateType())
                .eq(StrUtil.isNotBlank(dcTemplate.getName()), DcTemplate::getName, dcTemplate.getName())
                .eq(StrUtil.isNotBlank(dcTemplate.getTypeId()), DcTemplate::getTypeId, dcTemplate.getTypeId());
        dcTemplateService.page(page, queryWrapper);
        page.getRecords().parallelStream().peek(e ->
                {
                    //如果CoverBucketName为空 则视为传的静态资源地址
                    if (StrUtil.isBlank(e.getCoverBucketName())) {
                        e.setCoverUrl(e.getCoverFilePath());
                        return;
                    }
                    e.setCoverUrl(ossTemplate.fileLink(e.getCoverFilePath(), e.getCoverBucketName()));
                }
        ).collect(Collectors.toList());
        return R.ok(page);
    }


    @PostMapping("/save")
    @ApiOperation("保存模板")
    public R<DcTemplate> save(@RequestBody DcTemplate dcTemplate) {
        if (StrUtil.isBlank(dcTemplate.getId())) {
            //新增才会生成文件
            List<String> nameSuffixS = Arrays.asList("docx", "xlsx", "pptx", "pdf", "csv");
            if (StrUtil.isNotBlank(dcTemplate.getNameSuffix()) && nameSuffixS.contains(dcTemplate.getNameSuffix())) {
                BaseFile office = OfficeContentUtil.createOffice(dcTemplate.getNameSuffix());
                dcTemplate.setBucketName(office.getBucketName())
                        .setFilePath(office.getFileName())
                        .setType(DcLibraryTypeEnum.suffixToEnum(dcTemplate.getNameSuffix()));
            }
        }
        dcTemplateService.saveOrUpdate(dcTemplate);
        return R.ok(dcTemplate);
    }

    @PostMapping("/save/content/{documentId}")
    @ApiOperation("保存模板内容")
    public R<Boolean> saveContent(@RequestBody String content, @PathVariable String documentId) {
        //数据拆分
        String otherJson = null;
        if (content.contains(Constant.OTHER_JSON_WEB)) {
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(content);
            if (jsonObject.containsKey(Constant.OTHER_JSON_WEB)) {
                otherJson = jsonObject.getString(Constant.OTHER_JSON_WEB);
                jsonObject.remove(Constant.OTHER_JSON_WEB);
                content = jsonObject.toJSONString();
            }
        }
        DcTemplate dcTemplate = dcTemplateService.get(documentId);
        //获取分类
        DcTemplateType byId = dcTemplateTypeService.getById(dcTemplate.getTypeId());
        // 将知识库名称当做文件的一级目录
        BaseFile baseFile = ossTemplate.putContent(FileOssUtils.getMultipartFileName(dcTemplate.getType()), content, byId.getTypeName(), dcTemplate.getName());
        dcTemplate.setBucketName(baseFile.getBucketName()).setFilePath(baseFile.getFileName()).setOtherJson(otherJson);
        dcTemplateService.updateById(dcTemplate);
        return R.ok(true, "保存成功");
    }

    @SneakyThrows
    @GetMapping("/get/{id}")
    @ApiOperation("获取模板内容")
    public R<DcTemplate> preview(@PathVariable("id") String id) {
        DcTemplate dcTemplate = dcTemplateService.getById(id);
        if (dcTemplate == null) {
            throw new BusinessException("数据不存在");
        }
        if (StrUtil.isNotBlank(dcTemplate.getFilePath()) && StrUtil.isNotBlank(dcTemplate.getBucketName())) {
            String objectUrl = ossTemplate.fileLink(dcTemplate.getFilePath(), dcTemplate.getBucketName());
            List<String> nameSuffixS = Arrays.asList("docx", "xlsx", "pptx", "pdf", "csv");
            if (StrUtil.isNotBlank(dcTemplate.getNameSuffix()) && nameSuffixS.contains(dcTemplate.getNameSuffix())) {
                if (StrUtil.isNotEmpty(dcTemplate.getSavePathId())) {
                    //查看保存的名称
                    DcLibrary dcLibrary = dcLibraryService.getById(dcTemplate.getSavePathId());
                    if (ObjectUtil.isNotNull(dcLibrary)) {
                        dcTemplate.setSavePathName(dcLibrary.getName());
                    }
                }
                //变量直接返回url
                dcTemplate.setFileLink(objectUrl);
              String  key = SecureUtil.md5(dcTemplate.getBucketName()+dcTemplate.getFilePath());
                //key值不不能大于20
                if (key.length() > 20) {
                    key = Integer.toString(key.hashCode());
                }
                key = key.replace("[^0-9-.a-zA-Z_=]", "_");
                key = key.substring(0, Math.min(key.length(), 20));
                dcTemplate.setKey(key);
            } else {
                byte[] bytes = HttpUtil.downloadBytes(objectUrl);
                dcTemplate.setContent(bytes == null ? null : ObjectUtil.deserialize(bytes));
            }
        }
        return R.ok(dcTemplate);
    }

    @Log
    @DeleteMapping("/{id}")
    @ApiOperation("删除模板")
    public R<Boolean> delete(@PathVariable String id) {
        dcTemplateService.removeById(id);
        //删除变量
        dcTemplateVariableService.remove(new LambdaQueryWrapper<DcTemplateVariable>().eq(DcTemplateVariable::getTemplateId, id));
        return R.ok(true, "删除成功");
    }


}
