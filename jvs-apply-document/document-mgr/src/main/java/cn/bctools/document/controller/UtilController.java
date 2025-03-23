package cn.bctools.document.controller;


import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.message.aspect.MessagePush;
import cn.bctools.document.office.pdf.PdfMergeDocUtils;
import cn.bctools.document.office.word.WordMergeDocUtils;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.document.util.FileTransitionUtil;
import cn.bctools.document.util.OCRUtil;
import cn.bctools.document.vo.OnlyOfficeFileTransitionVo;
import cn.bctools.document.vo.UtilFileTransitionVo;
import cn.bctools.document.vo.UtilOCRVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "工具类")
@RestController
@RequestMapping(value = "/util")
@AllArgsConstructor
public class UtilController {
    private final OssTemplate ossTemplate;
    private final DcLibraryService dcLibraryService;
    private final OssProperties ossProperties;

    @Log
    @ApiOperation(value = "ocr识别")
    @PostMapping("/ocr")
    @SneakyThrows
    public R<String> ocrContent(@RequestBody @Validated UtilOCRVo utilOcrVo) {
        InputStream inputStream = new URL(ossProperties.getEndpoint() + utilOcrVo.getFileUrl()).openStream();
        String content = OCRUtil.getContent(inputStream, IdGenerator.getIdStr() + "." + utilOcrVo.getSuffix(), utilOcrVo.getIsGetText());
        return R.ok(content);
    }

    @Log
    @ApiOperation(value = "判断文档是否为当前登录用户同一租户下面")
    @PostMapping("/check/tenant/{id}")
    @SneakyThrows
    public R<Boolean> checkTenant(@ApiParam(value = "文档id", required = true) @PathVariable String id) {
        DcLibrary byId = dcLibraryService.getById(id);
        if (ObjectUtil.isNull(byId)) {
            return R.failed(Boolean.FALSE);
        }
        return R.ok(Boolean.TRUE);
    }

    @Log
    @ApiOperation(value = "文件合并")
    @PostMapping("/merge")
    @SneakyThrows
    public R<String> merge(@RequestBody List<BaseFile> list) {
        if (list.size() < 2) {
            return R.failed("至少需要2个文件");
        }
        List<InputStream> inputStreams = list.stream().map(e -> FileOssUtils.getInputStream(ossTemplate.fileLink(e.getFileName(), e.getBucketName()))).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        if (inputStreams.size() != list.size()) {
            return R.failed("转换失败");
        }
        String suffix = FileUtil.getSuffix(list.get(0).getFileName());
        if (StrUtil.isBlank(suffix)) {
            return R.failed("文件类型错误");
        }
        File outFile;
        switch (suffix) {
            case "pdf":
                outFile = new File("test.pdf");
                PdfMergeDocUtils.mergePdf(inputStreams, outFile);
                break;
            case "docx":
                outFile = new File("test.docx");
                WordMergeDocUtils.mergeDoc(inputStreams, outFile);
                break;
            default:
                return R.failed("文件类型错误");
        }
        BufferedInputStream inputStream = FileUtil.getInputStream(outFile);
        BaseFile baseFile = ossTemplate.putFile(SpringContextUtil.getApplicationContextName(), SpringContextUtil.getApplicationContextName(), IdGenerator.getIdStr() + outFile.getName(), inputStream);
        inputStream.close();
        boolean delete = outFile.delete();
        log.info("文件合并成功，删除临时文件:{}", delete);
        //获取url
        String fileLink = ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName());
        return R.ok(fileLink);
    }

    @Log
    @ApiOperation(value = "ocr内容保存")
    @PostMapping("/ocr/save/{id}")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> ocrSaveContent(@PathVariable("id") String id,
                                       @ApiParam(value = "文件名称", required = true) @RequestParam(value = "fileName") String fileName,
                                       @ApiParam(value = "文件内容", required = true) @RequestParam(value = "content") String content) {
        fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
        File file = FileUtil.writeString(content, fileName, "utf-8");
        DcLibrary byId = dcLibraryService.getById(id);
        String knowledgeName = byId.getName();
        if (!byId.getType().equals(DcLibraryTypeEnum.knowledge)) {
            knowledgeName = dcLibraryService.getById(byId.getKnowledgeId()).getName();
        }
        BaseFile baseFile = ossTemplate.putFile(fileName, FileUtil.getInputStream(file), knowledgeName, byId.getName());
        DcLibrary dcLibrary = dcLibraryService.fileToSave(id, baseFile, null);
        boolean delete = file.delete();
        log.info("ocr内容保存后 删除本地临时文件,{}", delete);
        return R.ok(dcLibrary);
    }


    @Log
    @ApiOperation(value = "文件转换")
    @PostMapping("/file/transition")
    @SneakyThrows
    public R<String> fileTransition(@RequestBody @Validated UtilFileTransitionVo utilFileTransitionVo) {
        String idStr = IdGenerator.getIdStr();
        String endpoint = ossProperties.getEndpoint();
        endpoint = endpoint.trim().startsWith("http") ? endpoint : "http://" + endpoint;
        OnlyOfficeFileTransitionVo onlyOfficeFileTransitionVo = new OnlyOfficeFileTransitionVo()
                .setAsync(Boolean.FALSE)
                .setFiletype(utilFileTransitionVo.getSuffix())
                .setOutputtype(utilFileTransitionVo.getOutSuffix())
                .setUrl(endpoint + utilFileTransitionVo.getFileUrl())
                .setTitle(idStr + "." + utilFileTransitionVo.getOutSuffix())
                .setKey(idStr);
        String transition = FileTransitionUtil.transition(onlyOfficeFileTransitionVo);
        return R.ok(transition);
    }

    @SneakyThrows
    @ApiOperation("文件转换后进行保存")
    @PostMapping("/transition/save/file/{id}")
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> saveFile(@PathVariable("id") String id,
                                 @ApiParam(value = "文件url", required = true) @RequestParam(value = "fileUrl") String fileUrl,
                                 @ApiParam(value = "文件名称", required = true) @RequestParam(value = "fileName") String fileName,
                                 @ApiParam(value = "文件后缀名", required = true) @RequestParam(value = "suffix") String suffix) {
        //判断是否包含后缀名
        String suffix1 = FileUtil.getSuffix(fileName);
        fileName = fileName + "." + suffix;
        if (StrUtil.isNotBlank(suffix1)) {
            fileName = fileName.replace(suffix1, suffix);
        }
        InputStream inputStream = FileOssUtils.getInputStream(fileUrl);
        BaseFile baseFile = ossTemplate.putFile(SpringContextUtil.getApplicationContextName(), SpringContextUtil.getApplicationContextName(), IdGenerator.getIdStr() + fileName, inputStream);
        DcLibrary dcLibrary = dcLibraryService.fileToSave(id, baseFile, fileName);
        inputStream.close();
        return R.ok(dcLibrary);
    }


}
