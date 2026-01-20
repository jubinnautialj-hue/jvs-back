package cn.bctools.oss.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.Etag;
import cn.bctools.oss.dto.FileLinkDto;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.mapper.SysFileLabelMapper;
import cn.bctools.oss.po.OssFileLabel;
import cn.bctools.oss.service.FileDataInterface;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 〈文件上传管理〉
 *
 * @since: 1.0.0
 * @author: auto
 */
@Slf4j
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "文件上传下载")
public class FileUploadController {
    static final String PUBLIC_BUCKET_NAME = "jvs-public";

    FileDataInterface fileDataInterface;
    OssTemplate ossTemplate;
    SysFileLabelMapper fileLabelMapper;
    RedisUtils redisUtils;

    /**
     * 〈文件上传〉
     *
     * @param file       文件对象
     * @param bucketName 文件桶名
     * @param module     模块对象，为不同服务模块区分不同的服务
     * @return BaseFile 文件基本信息
     * @throws BusinessException 文件上传异常
     * @since: 1.0.0
     * @author: auto
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload/{bucketName}")
    public R<FileNameDto> upload(@RequestPart("file") MultipartFile file, @RequestParam(value = "module", required = false) String module, @PathVariable String bucketName, @RequestParam(value = "label",
            defaultValue = "默认") String label) throws BusinessException {
        // 校验 file 参数
        if (ObjectNull.isNull(file)) {
            log.error("[文件上传] 上传失败 - file参数为null, bucketName: {}, module: {}", bucketName, module);
            return R.failed(null, "上传文件不能为空");
        }
        
        String originalFileName = file.getOriginalFilename();
        Long fileSize = file.getSize();
        
        // 校验文件名
        if (ObjectNull.isNull(originalFileName) || originalFileName.trim().isEmpty()) {
            log.error("[文件上传] 上传失败 - 文件名为空, bucketName: {}, module: {}, fileSize: {}", bucketName, module, fileSize);
            return R.failed(null, "文件名不能为空");
        }
        
        log.info("[文件上传] 开始上传文件 - bucketName: {}, module: {}, label: {}, originalFileName: {}, fileSize: {}", bucketName, module, label, originalFileName, fileSize);
        
        // 校验 module 参数
        if (ObjectNull.isNull(module)) {
            log.warn("[文件上传] 上传失败 - module参数不存在, bucketName: {}, fileName: {}", bucketName, originalFileName);
            return R.failed(null, "'module' 目录参数不存在,需根据上传规范(需要包含上传路径)上传文件,推荐:/前端项目名/功能名/");
        }
        SystemThreadLocal.set("label", label);

        try {
            log.debug("[文件上传] 调用OSS存储 - bucketName: {}, module: {}, fileName: {}", bucketName, module, originalFileName);
            
            BaseFile source = ossTemplate.putFile(bucketName, module, originalFileName, file);
            
            // 校验 OSS 返回结果
            if (ObjectNull.isNull(source)) {
                log.error("[文件上传] OSS存储失败 - 返回结果为null, bucketName: {}, module: {}, fileName: {}", bucketName, module, originalFileName);
                throw new BusinessException("文件存储失败");
            }
            
            log.debug("[文件上传] OSS存储成功 - fileName: {}, size: {}", source.getFileName(), source.getSize());
            
            FileNameDto target = BeanCopyUtil.copy(source, FileNameDto.class);
            
            // 校验复制结果
            if (ObjectNull.isNull(target)) {
                log.error("[文件上传] 对象复制失败 - target为null, source: {}", source);
                throw new BusinessException("文件信息处理失败");
            }
            
            OssFileLabel entity = new OssFileLabel().setLabel(label);
            Long l = fileLabelMapper.selectCount(Wrappers.lambdaQuery(entity));
            if (l == 0) {
                log.debug("[文件上传] 插入新标签 - label: {}", label);
                fileLabelMapper.insert(entity);
            }
            target.setOriginalFileName(originalFileName);
            
            // 生成文件链接
            String fileName = target.getFileName();
            if (ObjectNull.isNull(fileName)) {
                log.error("[文件上传] 文件名为空 - target: {}", target);
                throw new BusinessException("文件名获取失败");
            }
            
            if (bucketName.equals(PUBLIC_BUCKET_NAME)) {
                target.setFileLink(ossTemplate.fileJvsPublicLink(fileName));
                log.debug("[文件上传] 生成公共文件链接 - fileLink: {}", target.getFileLink());
            } else {
                String targetBucketName = target.getBucketName();
                if (ObjectNull.isNull(targetBucketName)) {
                    log.warn("[文件上传] target.bucketName为空，使用请求参数bucketName: {}", bucketName);
                    targetBucketName = bucketName;
                }
                target.setFileLink(ossTemplate.fileLink(fileName, targetBucketName));
                log.debug("[文件上传] 生成私有文件链接 - fileLink: {}", target.getFileLink());
            }
            target.setFileSize(source.getSize());
            
            log.info("[文件上传] 上传成功 - bucketName: {}, fileName: {}, originalFileName: {}, fileSize: {}, fileLink: {}",
                    target.getBucketName(), target.getFileName(), target.getOriginalFileName(), target.getFileSize(), target.getFileLink());
            
            return R.ok(target);
        } catch (Exception e) {
            log.error("[文件上传] 上传失败 - bucketName: {}, module: {}, fileName: {}, 异常: {}",
                    bucketName, module, originalFileName, e.getMessage(), e);
            throw e;
        }
    }
    @ApiOperation("获取文件类型集")
    @GetMapping("/fileTypes")
    public R<List<String>> fileTypes(@RequestParam(value = "bucketName", required = false) String bucketName) {
        return R.ok();
    }
    /**
     * 将三方服务地址下载到本地服务器后返回给前端， 用于富文本粘贴处理
     *
     * @param file       文件的url
     * @param bucketName 文件桶名
     * @param module     模块对象，为不同服务模块区分不同的服务
     * @return BaseFile 文件基本信息
     * @throws BusinessException 文件上传异常
     * @since: 1.0.0
     * @author: auto
     */
    @ApiOperation("上传在线文件")
    @PostMapping("/upload/url/{bucketName}")
    public R<FileNameDto> fileUrl(@RequestParam("file") String file, @RequestParam(value = "module", required = false) String module,
                                  @PathVariable String bucketName,
                                  @RequestParam(value = "label", defaultValue = "默认") String label) throws BusinessException, URISyntaxException {
        SystemThreadLocal.set("label", label);
        byte[] bytes = HttpUtil.downloadBytes(file);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BaseFile source = ossTemplate.put(bucketName, module, byteArrayInputStream, file.substring(file.lastIndexOf("/") + 1), false);
        FileNameDto target = BeanCopyUtil.copy(source, FileNameDto.class);
        target.setOriginalFileName(source.getFileName());
        target.setFileLink(ossTemplate.fileLink(target.getFileName(), target.getBucketName()));
        target.setFileSize(source.getSize());
        return R.ok(target);
    }

    /**
     * 〈上传base64〉
     *
     * @param file       base64
     * @param bucketName 文件桶名
     * @param module     模块对象，为不同服务模块区分不同的服务
     * @return BaseFile 文件基本信息
     * @throws BusinessException 文件上传异常
     * @since: 1.0.0
     * @author: auto
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload/base64/{bucketName}")
    public R<FileNameDto> upload(@RequestParam(value = "file", required = false) String file, @RequestParam(value = "module", required = false) String module, @PathVariable String bucketName,
                                 @RequestParam(value = "label", defaultValue = "默认") String label, @RequestBody Map<String, String> body) throws BusinessException {
        if (ObjectNull.isNull(file)) {
            file = body.get("file");
        }
        SystemThreadLocal.set("label", label);


        String fileType = "";
        int split = file.indexOf(",", 0);
        String dataUri = file.substring(0, split);
        String png = "png";
        if (dataUri.contains(png)) {
            fileType = ".png";
        } else {
            String svg = "svg";
            if (dataUri.contains(svg)) {
                fileType = ".svg";
            } else {
                String jpg = "jpg";
                if (dataUri.contains(jpg)) {
                    fileType = ".jpg";
                } else {
                    String gif = "gif";
                    if (dataUri.contains(gif)) {
                        fileType = ".gif";
                    }
                }
            }
        }
        String fileName = UUID.randomUUID().toString() + fileType;
        String base64 = file.substring(split + 1);
        byte[] decode = Base64.getDecoder().decode(base64);
        InputStream byteArrayInputStream = new ByteArrayInputStream(decode);
        BaseFile source = ossTemplate.putFile(bucketName, module, fileName, byteArrayInputStream);
        FileNameDto target = BeanCopyUtil.copy(source, FileNameDto.class);
        target.setOriginalFileName(source.getFileName());
        target.setFileLink(ossTemplate.fileLink(target.getFileName(), target.getBucketName()));
        target.setFileSize(source.getSize());
        return R.ok(target);
    }

    /**
     * 〈文件外链〉
     *
     * @param bucketName 桶名称
     * @return 文件外链
     * @since: 1.0.0
     * @author: auto
     */
    @SneakyThrows
    @ApiOperation("预览文件")
    @GetMapping("/file/link/{bucketName}")
    public R<String> fileLink(@PathVariable("bucketName") String bucketName, @RequestParam("fileName") String fileName) {
        fileName = URLDecoder.decode(fileName, Charset.defaultCharset().name());
        String fileLink = ossTemplate.fileLink(fileName, bucketName);
        return R.ok(fileLink);
    }

    @ApiOperation("预览")
    @GetMapping("/file/preview/{bucketName}")
    public void preview(HttpServletResponse response, @PathVariable("bucketName") String bucketName, @RequestParam("fileName") String fileName) {
        String fileLink = ossTemplate.fileLink(fileName, bucketName);
        try {
            response.sendRedirect(fileLink);
        } catch (IOException e) {
            log.error("文件预览失败");
        }
    }

    @ApiOperation("获取文件集合的文件地址")
    @PostMapping("/file/links")
    public R<List<FileLinkDto>> filelinks(@RequestBody List<FileLinkDto> list) {
        list.parallelStream().filter(f0 -> StrUtil.isAllNotBlank(f0.getBucketName(), f0.getFileName()))
                .forEach(m -> {
                    try {
                        if (m.getBucketName().equals(PUBLIC_BUCKET_NAME)) {
                            m.setFileLink(ossTemplate.fileJvsPublicLink(m.getFileName()));
                        } else {
                            m.setFileLink(ossTemplate.fileLink(m.getFileName(), m.getBucketName()));
                        }
                    } catch (Exception ignored) {
                    }
                });
        return R.ok(list);
    }

    @SneakyThrows
    @ApiOperation("打开下载文件")
    @GetMapping("/bytes/{bucketName}")
    public ResponseEntity<byte[]> bytes(@PathVariable("bucketName") String bucketName, @RequestParam("fileName") String fileName) {
        InputStream object = ossTemplate.getObject(bucketName, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLEncoder.encode(fileName, Charset.defaultCharset().name())))
                .body(IoUtil.readBytes(object));
    }

    /**
     * 获取大文件上传凭证
     *
     * @param bucketName      桶
     * @param filename        文件名
     * @param totalPartNumber 分片数量：大文件切分的数量
     * @return 凭证
     */
    @ApiOperation(value = "获取大文件上传凭证", notes = "需要对大文件按5M进行严格切割，只能最后一个切片的大小可以不为5M，其余切片必须为5M，且对切片进行按顺序进行标号。凭证有时效性，为3小时。")
    @GetMapping("/largeFile/uploadCertificate/{bucketName}/{filename}/{totalPartNumber}")
    public R<Etag> getLargeFileUploadCertificate(@PathVariable("bucketName") String bucketName, @PathVariable("filename") String filename, @PathVariable("totalPartNumber") Integer totalPartNumber, @RequestParam(
            value = "module", required = false) String module, @RequestHeader(value = "businessId", required = false) String businessId) {
        String key = ossTemplate.getFileName(module, filename, businessId);
        String uploadId = ossTemplate.createMultipartUpload(bucketName, key);
        Etag etag = new Etag().setUploadId(uploadId).setBucketName(bucketName)
                .setOriginFileName(filename).setFileName(key).setTotalPartNumber(totalPartNumber).setPartNumber(0).setCreateTime(System.currentTimeMillis());
        String redisKey = "temp:" + uploadId;
        for (int i = 0; i < totalPartNumber; i++) {
            redisUtils.lSetList(redisKey, JSONObject.toJSONString(new Etag()));
        }
        redisUtils.expire(redisKey, 10, TimeUnit.MINUTES);
        return R.ok(etag);
    }

    /**
     * 上传片段
     *
     * @param file       文件
     * @param bucketName 桶
     * @param uploadId   凭证
     * @param partNumber 分片序号
     * @return R
     */
    @SneakyThrows
    @ApiOperation("分片上传")
    @PostMapping(value = "/uploadPart/{bucketName}/{uploadId}/{partNumber}")
    public R<String> uploadPart(@RequestHeader(value = "extra", required = false) String extra, @RequestPart("file") MultipartFile file, @RequestParam(value = "module", required = false) String module, @RequestParam("fileName") String fileName
            , @PathVariable("bucketName") String bucketName, @PathVariable("uploadId") String uploadId, @PathVariable("partNumber") Integer partNumber) {
        R<FileNameDto> fileNameDtoR = uploadPartFile(extra, file, bucketName, fileName, module, uploadId, partNumber);
        if (ObjectNull.isNotNull(fileNameDtoR.getData())) {
            return R.ok(fileNameDtoR.getData().getFileLink());
        }
        return R.ok();
    }

    @SneakyThrows
    @ApiOperation("分片上传")
    @PostMapping(value = "/uploadPart/file/{bucketName}/{uploadId}/{partNumber}")
    public R<FileNameDto> uploadPartFile(@RequestHeader(value = "extra", required = false) String extra, @RequestPart("file") MultipartFile file, @PathVariable("bucketName") String
            bucketName, @RequestParam("fileName") String fileName, @RequestParam(value = "module", required = false) String module, @PathVariable("uploadId") String uploadId, @PathVariable("partNumber") Integer partNumber) {
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String key = fileName;
        Etag tag = ossTemplate.uploadPart(bytes, bucketName, partNumber, key, uploadId);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletResponse response = attributes.getResponse();
            if (response != null) {
                response.addHeader("extra", extra);
            }
        }
        String redisKey = "temp:" + tag.getUploadId();
        redisUtils.lUpdateIndex(redisKey, Long.valueOf(partNumber - 1), JSONObject.toJSONString(tag));
        Set<Etag> collect = redisUtils.lGet(redisKey, 0, -1)
                .stream()
                .map(e -> JSONObject.parseObject(e.toString(), Etag.class))
                .collect(Collectors.toSet());
        Set<Etag> list = collect.stream().filter(e -> ObjectNull.isNull(e.getUploadId()))
                .collect(Collectors.toSet());
        if (list.size() == 0) {
            ossTemplate.completeMultipartUpload(bucketName, key, uploadId, collect);
            redisUtils.del(redisKey);

            String link;
            if (bucketName.equals(PUBLIC_BUCKET_NAME)) {
                link = ossTemplate.fileJvsPublicLink(key);
            } else {
                link = ossTemplate.fileLink(key, bucketName);
            }
            FileNameDto fileNameDto = new FileNameDto();
            fileNameDto.setBucketName(bucketName)
                    .setFileName(key)
                    .setFileLink(link);
            return R.ok(fileNameDto);
        } else {
            redisUtils.expire(redisKey, 10, TimeUnit.MINUTES);
            //如果没完直接返回
            return R.ok();
        }
    }

}
