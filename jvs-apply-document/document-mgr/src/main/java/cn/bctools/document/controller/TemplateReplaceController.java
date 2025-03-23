package cn.bctools.document.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.office.OfficeContentUtil;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcTemplateService;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.document.vo.req.TemplateGenerateVo;
import cn.bctools.document.vo.req.TemplateReplaceDataVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 * @Description: 模板替换
 */

@Slf4j
@Api(tags = "模板变量替换")
@RestController
@RequestMapping(value = "/template/replace")
@AllArgsConstructor
public class TemplateReplaceController {

    private final DcLibraryService dcLibraryService;
    private final OssTemplate ossTemplate;
    private final DcTemplateService dcTemplateService;
    private final DcAuthConfigService dcAuthConfigService;


    @Log
    @ApiOperation("获取可新增文档的文库")
    @GetMapping("/get/library")
    public R<List<DcLibrary>> getLibrary() {
        List<DcAuthConfig> authConfig = dcAuthConfigService.getAuthConfig(IdentifyingKeyEnum.document_add);
        if (CollectionUtil.isEmpty(authConfig)) {
            return R.ok(Collections.emptyList());
        }
        List<String> ids = authConfig.stream().map(DcAuthConfig::getDcId).collect(Collectors.toList());
        List<DcLibrary> dcLibraries = dcLibraryService.listByIds(ids);
        return R.ok(dcLibraries);
    }


    @Log
    @ApiOperation(value = "文档生成")
    @PostMapping("/generate")
    public R<DcLibrary> generate(@RequestBody @Validated TemplateGenerateVo templateGenerateVo) {
        DcTemplate dcTemplate = getFileLink(templateGenerateVo.getDcId());

        if (StrUtil.isNotBlank(templateGenerateVo.getSavePathId())) {
            dcTemplate.setSavePathId(templateGenerateVo.getSavePathId());
        }
        //判断是否存在文档存储位置与存储位置数据是否还存在
        if (StrUtil.isEmpty(dcTemplate.getSavePathId())) {
            throw new BusinessException("未设置文档存储位置");
        }
        DcLibrary dcLibrary = dcLibraryService.getById(dcTemplate.getSavePathId());
        if (!Optional.ofNullable(dcLibrary).isPresent()) {
            throw new BusinessException("文档存储位置未找到请重新设置");
        }
        String saveLibraryId = dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge) ? dcLibrary.getId() : dcLibrary.getKnowledgeId();
        List<String> ids = dcAuthConfigService.getAuthConfig(IdentifyingKeyEnum.document_add).stream().map(DcAuthConfig::getDcId).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(ids) || !ids.contains(saveLibraryId)) {
            return R.failed("权限不足选择存储文档的文库未拥有新增文档权限");
        }

        try (InputStream inputStream = new URL(dcTemplate.getFileLink()).openStream()) {
            Map<String, Object> data = templateGenerateVo.getData().stream().collect(Collectors.toMap(TemplateReplaceDataVo::getName, TemplateReplaceDataVo::getValue));
            InputStream replaceInputStream = OfficeContentUtil.replace(inputStream, dcTemplate.getNameSuffix(), data);
            //入库
            String multipartFileName = FileOssUtils.getMultipartFileName(dcTemplate.getType()) + "." + dcTemplate.getNameSuffix();
            BaseFile baseFile = ossTemplate.putFile(multipartFileName, replaceInputStream, dcLibrary.getName());
            DcLibrary fileToSave = dcLibraryService.fileToSave(dcTemplate.getSavePathId(), baseFile, null);
            //获取url
            String s = ossTemplate.fileLink(fileToSave.getFilePath(), fileToSave.getBucketName());
            fileToSave.setFileLink(s);
            return R.ok(fileToSave);
        } catch (Exception e) {
            log.info("模板替换错误！", e);
            throw new BusinessException("模板替换错误");
        }
    }


    /**
     * 获取url
     *
     * @param id 文档id
     */
    private DcTemplate getFileLink(String id) {
        DcTemplate byId = dcTemplateService.getById(id);
        List<DcLibraryTypeEnum> typeEnums = Arrays.asList(DcLibraryTypeEnum.office_xlsx, DcLibraryTypeEnum.office_doc);
        if (!Optional.ofNullable(byId).isPresent()) {
            throw new BusinessException("模板不存在");
        }
        if (!typeEnums.contains(byId.getType())) {
            throw new BusinessException("模板类型错误");
        }
        if (ObjectUtil.isEmpty(byId.getSavePathId())) {
            throw new BusinessException("请设置文档保存地址");
        }
        byId.setFileLink(ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName()));
        return byId;
    }
}
