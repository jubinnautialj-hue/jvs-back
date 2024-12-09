package cn.bctools.design.crud;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.crud.dto.PrintTemplateDto;
import cn.bctools.design.crud.entity.PrintTemplate;
import cn.bctools.design.crud.entity.enums.DesignTypeEnum;
import cn.bctools.design.crud.service.PrintTemplateService;
import cn.bctools.design.crud.utils.PrintTemplateFileUtil;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "打印模板设计")
@RestController
@RequestMapping("/app/design/{appId}/print/template")
@AllArgsConstructor
public class PrintTemplateDesignController {

    private final PrintTemplateService service;
    private final OssTemplate ossTemplate;


    @ApiOperation("创建打印模板")
    @PostMapping
    public R<String> create(@Validated @RequestBody PrintTemplateDto dto, @PathVariable String appId) {
        PrintTemplate po = BeanCopyUtil.copy(dto, PrintTemplate.class);
        po.setJvsAppId(appId);
        service.saveOrUpdate(po);
        return R.ok();
    }

    @ApiOperation("修改打印模板")
    @PutMapping
    public R<String> update(@Validated @RequestBody PrintTemplateDto dto, @PathVariable String appId) {
        Optional.ofNullable(dto.getId()).orElseThrow(() -> new BusinessException("id为空"));
        PrintTemplate po = BeanCopyUtil.copy(dto, PrintTemplate.class);
        po.setJvsAppId(appId);
        service.saveOrUpdate(po);
        return R.ok();
    }

    @ApiOperation("获取设计所有打印模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "designId", value = "设计id", required = true),
    })
    @GetMapping("/{designId}/all")
    public R<List<PrintTemplate>> getDesignAll(@PathVariable String designId, @PathVariable String appId) {
        return R.ok(service.getDesignAll(appId, designId));
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public R<FlowTaskProxy> revoke(@PathVariable String id, @PathVariable String appId) {
        service.remove(Wrappers.query(new PrintTemplate().setId(id).setJvsAppId(appId)));
        return R.ok();
    }

    @SneakyThrows
    @ApiOperation("上传打印模板")
    @PostMapping("/{designId}/upload/{bucketName}")
    public R<FileNameDto> upload(@PathVariable String appId,
                                 @PathVariable String designId,
                                 @PathVariable String bucketName,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "module", required = false) String module,
                                 @RequestParam(value = "label", defaultValue = "默认") String label) {
        // 校验文件
        String fileName = file.getOriginalFilename();
        PrintTemplateFileUtil.checkFileType(PrintTemplateFileUtil.getFileTypeByName(fileName));
        // 上传文件
        SystemThreadLocal.set("label", label);
        BaseFile source = ossTemplate.putFile(bucketName, module, file.getInputStream());
        String fileLink = ossTemplate.fileLink(source.getFileName(), bucketName);
        // 保存文件模板
        PrintTemplateDto printTemplateDto = new PrintTemplateDto()
                .setDesignId(designId)
                .setName(fileName)
                .setDesignType(DesignTypeEnum.WORD)
                .setFileUrl(fileLink)
                .setFileType(source.getFileType())
                .setEnableFlag(Boolean.FALSE);
        PrintTemplate po = BeanCopyUtil.copy(printTemplateDto, PrintTemplate.class);
        po.setJvsAppId(appId);
        service.saveOrUpdate(po);
        return R.ok();
    }
}
