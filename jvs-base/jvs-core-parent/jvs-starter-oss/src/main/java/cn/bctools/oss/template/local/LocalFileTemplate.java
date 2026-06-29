package cn.bctools.oss.template.local;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.Etag;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.service.FileDataInterface;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.io.FileUtil;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.bctools.web.utils.HttpRequestUtils.HTTP_PREFIX;

/**
 * 本地文件
 *
 * @author jvs
 */
@RequiredArgsConstructor
public class LocalFileTemplate implements OssTemplate {

    private final OssProperties ossProperties;
    private final FileDataInterface fileDataInterface;
    static String PATH_PREFIX = "/oss";

    public LocalFileTemplate(OssProperties ossProperties, FileDataInterface fileDataInterface, RedisUtils redisUtils) {
        this.ossProperties = ossProperties;
        this.fileDataInterface = fileDataInterface;
    }

    @Override
    public void makeBucket(String bucketName) {
        FileUtil.mkdir(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + bucketName);
    }

    @Override
    public List<String> listBuckets() {
        return null;
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return FileUtil.isDirectory(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + bucketName);
    }

    @Override
    public void listObjects(String bucketName, String path, String label) {
        //暂时不刷新
    }

    @Override
    public String fileLink(String fileName, String bucketName) {
        HttpServletRequest request = WebUtils.getRequest();
        String host = getHost(request);
        fileName = HTTP_PREFIX + host + PATH_PREFIX + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR + fileName;
        return fileName;
    }


    @SneakyThrows
    @Override
    public BaseFile put(String bucketName, String businessId, String module, InputStream stream, String key, boolean cover) {
        ObjectMetadata metadata = new ObjectMetadata();

        Long size = Long.valueOf(stream.available());
        makeBucket(bucketName);
        String originalName = key;
        key = getFileName(module, key, businessId);

        int i = key.lastIndexOf(".") + 1;
        String substring = key.substring(i);

        String contentType = MIME_MAPPINGS.get(substring);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String s3Key = key.replaceAll("//", "/");
        metadata.setContentType(contentType);
        FileUtil.writeFromStream(stream, ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR + key);
        BaseFile file = new BaseFile();
        file.setOriginalName(originalName);
        file.setFileName(s3Key);
        file.setBucketName(bucketName);
        file.setFileType(contentType);
        file.setModule(module);
        file.setSize(size);
        //记录信息
        fileDataInterface.insert(file);
        return file;
    }

    @SneakyThrows
    @Override
    public BaseFile put(String bucketName, String module, InputStream stream, String originalName, boolean cover) {
        return put(bucketName, null, module, stream, originalName, cover);
    }

    @SneakyThrows
    @Override
    public BaseFile put(String bucketName, String businessId, String module, File stream, String key, boolean cover) {
        ObjectMetadata metadata = new ObjectMetadata();

        makeBucket(bucketName);
        key = getFileName(module, key, businessId);

        int i = key.lastIndexOf(".") + 1;
        String substring = key.substring(i);

        String contentType = MIME_MAPPINGS.get(substring);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String s3Key = key.replaceAll("//", "/");
        metadata.setContentType(contentType);
        FileUtil.writeFromStream(new FileInputStream(stream), ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + bucketName + FileUtil.FILE_SEPARATOR + key);
        BaseFile file = new BaseFile();
        file.setOriginalName(key);
        file.setFileName(s3Key);
        file.setBucketName(bucketName);
        file.setFileType(contentType);
        file.setModule(module);
        file.setSize(stream.length());
        //记录信息
        fileDataInterface.insert(file);
        return file;
    }

    @Override
    public BaseFile put(String bucketName, String module, File stream, String originalName, boolean cover) {
        return put(bucketName, null, module, stream, originalName, cover);
    }

    @Override
    public void removeFile(String bucketName, String fileName) {
        FileUtil.del(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + fileName);
    }

    @SneakyThrows
    @Override
    public InputStream getObject(String bucketName, String fileName) {
        return new FileInputStream(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + fileName);
    }

    @Override
    public List<BaseFile> listFiles(String bucketName) {
        return FileUtil.loopFiles(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + bucketName).stream()
                .map(e -> {
                    String absolutePath = e.getAbsolutePath().replace(ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR, "");
                    String fileName = absolutePath.substring(absolutePath.indexOf("/", 2));
                    return new BaseFile().setBucketName(bucketName).setOriginalName(e.getName()).setFileName(fileName).setSize(FileUtil.size(e));
                }).collect(Collectors.toList());
    }

    @Override
    public String createMultipartUpload(String bucketName, String filename) {
        throw new BusinessException("本地不支持分片上传");
    }

    @Override
    public Etag uploadPart(byte[] bytes, String bucketName, Integer partNumber, String fileName, String uploadId) {
        throw new BusinessException("本地不支持分片上传");
    }

    @Override
    public void completeMultipartUpload(String bucketName, String filename, String uploadId, Set<Etag> etagList) {
        throw new BusinessException("本地不支持分片上传");
    }

    @Override
    public void abortMultipartUpload(String bucketName, String filename, String uploadId) {
        throw new BusinessException("本地不支持分片上传");
    }

    @Override
    public String fileJvsPublicLink(String fileName) {
        //获取网关处理。
        HttpServletRequest request = WebUtils.getRequest();
        String host = getHost(request);
        fileName = HTTP_PREFIX + host + ossProperties.getLocalPath() + FileUtil.FILE_SEPARATOR + "jvs-public" + FileUtil.FILE_SEPARATOR + fileName;
        return fileName;
    }

    static final String KEY = "x-forwarded-host";

    public static String getHost(HttpServletRequest request) {
        //判断是否存在代理
        if (ObjectNull.isNotNull(request.getHeader(KEY))) {
            return request.getHeader(KEY);
        }
        return request.getHeader("host");
    }

}
