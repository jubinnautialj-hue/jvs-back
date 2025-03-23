package cn.bctools.document.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.receiver.DocumentConsumer;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcTemplateService;
import cn.bctools.document.util.FileTreeUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author ：admin
 * [description]：文件操作
 */
@Slf4j
@Api(tags = "文件操作")
@RestController
@RequestMapping(value = "/dcLibrary/file")
public class DcLibraryFileController {
    @Autowired
    DcLibraryService dcLibraryService;
    @Autowired
    OssTemplate ossTemplate;
    @Autowired
    DcTemplateService dcTemplateService;
    @Autowired
    DocumentConsumer documentConsumer;

    @Log
    @ApiOperation("下载文件夹")
    @GetMapping("/get/folder/{id}")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.DOWNLOAD)
    public void getFolder(@PathVariable String id) {
        DcLibrary byId = dcLibraryService.getById(id);
        //获取知识库id
        String knowledgeId = byId.getType().equals(DcLibraryTypeEnum.knowledge) ? byId.getId() : byId.getKnowledgeId();
        //获取整个知识库数据
        List<DcLibrary> list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().eq(DcLibrary::getKnowledgeId, knowledgeId).or().eq(DcLibrary::getId, knowledgeId));
        //对文件进行压缩
        byte[] bytes = FileTreeUtil.getTreeFile(list, id);
        //获取文件名称
        FileTreeUtil.fileOutput(byId.getName() + ".zip", bytes);
    }

    @Log
    @ApiOperation("解压文件")
    @PostMapping("/zip/{id}")
    @SneakyThrows
    public R zipFile(@PathVariable String id) {
        documentConsumer.sendFileDecompression(id);
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("下载文件选择下载的文件类型")
    @GetMapping("/down/{type}/{id}")
    @SneakyThrows
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.DOWNLOAD)
    public R down(@PathVariable String id, @ApiParam(value = "下载的类型，docx,pdf", required = true) @PathVariable String type) {
        String url = dcLibraryService.downOffice(id, type);
        return R.ok(url);
    }


    @Log
    @ApiOperation("预览url方便记录日志")
    @GetMapping("/preview/url/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据id"),
    })
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SEE)
    public R<Dict> previewUrl(@PathVariable String id) {
        DcLibrary byId = dcLibraryService.getById(id);
        if (!Optional.ofNullable(byId).isPresent()) {
            throw new BusinessException("未找到此数据");
        }
        Dict dict = Dict.create();
        String s = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
        dict.put("url", URLUtil.decode(s));
        return R.ok(dict);
    }


    @Log
    @ApiOperation("获取文件url")
    @GetMapping("/get/url/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据id"),
            @ApiImplicitParam(name = "isTemplate", value = "是否为模板"),
            @ApiImplicitParam(name = "isToken", value = "是否为登录")
    })
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SEE)
    public R<Dict> getUrl(@PathVariable String id, Boolean isTemplate) {
        //如果不是模板需要判断是否有权限
        if (!isTemplate) {
            DcLibrary byId = dcLibraryService.getById(id);
            //登录状态下如果参与了文库也能看
            boolean hasAuth;
            Set<String> ids = AuthSystemTool.getDocumentAuth().getIds();
            hasAuth = ids.contains(byId.getId());
            //如果文档不为公开 文库不为完全开放 且 没有当前文库的权限 则此时无法查看文档
            if (!hasAuth) {
                return R.failed("权限不足请联系文档所有者");
            }
        }
        Dict dict = Dict.create();
        String filePath;
        String bucketName;
        //onlyoffice 需要的唯一key
        String key;
        //区分是否为模板id
        if (!isTemplate) {
            DcLibrary byId = dcLibraryService.getById(id);
            filePath = byId.getFilePath();
            bucketName = byId.getBucketName();
            key = byId.getBucketName() + byId.getFilePath();
        } else {
            DcTemplate byId = dcTemplateService.getById(id);
            bucketName = byId.getBucketName();
            filePath = byId.getFilePath();
            key = byId.getBucketName() + byId.getFilePath();
        }
        key = SecureUtil.md5(key);
        //判断当前文件是否为
        String s = ossTemplate.fileLink(filePath, bucketName);
        dict.put("url", URLUtil.decode(s));
        if (StrUtil.isNotBlank(key)) {
            //key值不不能大于20
            if (key.length() > 20) {
                key = Integer.toString(key.hashCode());
            }
            key = key.replace("[^0-9-.a-zA-Z_=]", "_");
            key = key.substring(0, Math.min(key.length(), 20));
            dict.put("key", key);
        }
        return R.ok(dict);
    }

    @Log
    @ApiOperation("下载文件")
    @GetMapping("/get/file/{id}")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.DOWNLOAD)
    public void getFile(@PathVariable String id) {
        dcLibraryService.getFile(id);
    }

    @ApiOperation("获取二进制流数组")
    @GetMapping("/get/file/byte/{id}")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SEE)
    public byte[] getByteFile(@PathVariable String id) {
        DcLibrary byId = dcLibraryService.getById(id);
        if (StrUtil.isBlank(byId.getBucketName()) || StrUtil.isBlank(byId.getFilePath())) {
            throw new BusinessException("该文件内容为空");
        }
        String s = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
        try {
            URL url = new URL(s);
            InputStream inputStream = url.openStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            inputStream.close();
            return bytes;
        } catch (Exception e) {
            log.error("下载文件时获取文件错误", e);
            throw new BusinessException("获取文件错误");
        }
    }
}
