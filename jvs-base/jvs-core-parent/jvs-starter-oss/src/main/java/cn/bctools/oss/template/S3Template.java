package cn.bctools.oss.template;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.Etag;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.service.FileDataInterface;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
public class S3Template implements OssTemplate {

    private AmazonS3Client s3;
    private OssProperties ossProperties;
    private FileDataInterface fileDataInterface;

    public S3Template(OssProperties ossProperties, FileDataInterface fileDataInterface, RedisUtils redisUtils) {
        this.ossProperties = ossProperties;
        this.fileDataInterface = fileDataInterface;
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setRequestTimeout(ossProperties.getTimeOut());
        clientConfiguration.setSocketTimeout(ossProperties.getTimeOut());
        clientConfiguration.setConnectionTimeout(ossProperties.getTimeOut());
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
                ossProperties.getEndpoint().trim().startsWith("http") ? ossProperties.getEndpoint() : "http://" + ossProperties.getEndpoint(), null);
        AWSCredentials awsCredentials = new BasicAWSCredentials(ossProperties.getAccessKey(),
                ossProperties.getSecretKey());
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        this.s3 = (AmazonS3Client) AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfiguration)
                .withClientConfiguration(clientConfiguration).withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding().withPathStyleAccessEnabled(true).build();
        //初始化公开桶信息
        if (ObjectNull.isNotNull(ossProperties.getPublicBuckets())) {
            ossProperties.getPublicBuckets().forEach(e -> {
                makeBucket(e);
                //如果不同则更新
                s3.setBucketPolicy(e, String.format(STR, e, e));
            });
        }
    }

    /**
     * 获取s3对象
     */
    public AmazonS3Client getS3Client() {
        return this.s3;
    }

    @Override
    public void makeBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            s3.createBucket(bucketName);
        }
    }

    @Override
    public List<String> listBuckets() {
        return s3.listBuckets().stream().map(Bucket::getName).collect(Collectors.toList());
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return s3.doesBucketExist(bucketName);
    }


    @Override
    public void listObjects(String bucketName, String path, String label) {
        //暂时不刷新
    }

    @Override
    public String fileLink(String fileName, String bucketName) {
        //判断是否公开的
        if (ossProperties.getPublicBuckets().contains(bucketName)) {
            URL resUrl = s3.getUrl(bucketName, fileName);
            if (resUrl != null) {
                if (StrUtil.isNotBlank(ossProperties.getOutsideEndpoint())) {
                    return StringUtils.join(ossProperties.getOutsideEndpoint(), resUrl.getFile());
                }
                return resUrl.toString();
            }
            return null;
        }
        long time = System.currentTimeMillis() + Long.valueOf(ossProperties.getTimelinessHour() * 3600 * 1000);
        URL objUrl = s3.generatePresignedUrl(bucketName, fileName, new Date(time));
        if (StrUtil.isNotBlank(ossProperties.getOutsideEndpoint())) {
            return StringUtils.join(ossProperties.getOutsideEndpoint(), objUrl.getFile());
        }
        return objUrl.toString();
    }

    @SneakyThrows
    @Override
    public BaseFile put(String bucketName, String businessId, String module, File stream, String key, boolean cover) {

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
        s3.putObject(bucketName, s3Key, stream);
        BaseFile file = new BaseFile();
        file.setOriginalName(originalName);
        file.setFileName(s3Key);
        file.setBucketName(bucketName);
        file.setFileType(contentType);
        file.setModule(module);
        file.setSize(stream.length());
        //记录信息
        fileDataInterface.insert(file);
        return file;
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
        s3.putObject(bucketName, s3Key, new ByteArrayInputStream(IOUtils.toByteArray(stream)), metadata);

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

    public BaseFile put(String bucketName, String module, InputStream stream, String key, boolean cover) {
        return put(bucketName, null, module, stream, key, cover);
    }

    @Override
    public BaseFile put(String bucketName, String module, File stream, String originalName, boolean cover) {
        return put(bucketName, null, module, stream, originalName, cover);
    }


    @Override
    public void removeFile(String bucketName, String fileName) {
        s3.deleteObject(bucketName, fileName);
    }

    @Override
    public InputStream getObject(String bucketName, String fileName) {
        return s3.getObject(bucketName, fileName).getObjectContent();
    }

    @Override
    public List<BaseFile> listFiles(String bucketName) {
        return s3.listObjects(bucketName).getObjectSummaries().stream()
                .map(e -> new BaseFile().setFileName(e.getKey()).setOriginalName(e.getKey()).setBucketName(bucketName))
                .collect(Collectors.toList());
    }

    /**
     * 分片处理文件上传
     *
     * @param bucketName 桶
     * @param filename   文件名
     * @return
     */
    @Override
    public String createMultipartUpload(String bucketName, String filename) {
        makeBucket(bucketName);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, filename);
        return s3.initiateMultipartUpload(request).getUploadId();
    }

    @Override
    public Etag uploadPart(byte[] bytes, String bucketName, Integer partNumber, String key, String uploadId) {

        UploadPartRequest request = new UploadPartRequest();
        request.setUploadId(uploadId);
        request.setKey(key);
        request.setPartNumber(partNumber);
        request.setInputStream(new ByteArrayInputStream(bytes));
        request.setPartSize(bytes.length);
        request.setBucketName(bucketName);
        UploadPartResult uploadPartResult = s3.uploadPart(request);
        return new Etag().setEtag(uploadPartResult.getETag()).setPartNumber(partNumber).setBucketName(bucketName).setFileName(key).setUploadId(uploadId);
    }

    @Override
    public void completeMultipartUpload(String bucketName, String filename, String uploadId, Set<Etag> etagList) {
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest();
        request.setKey(filename);
        request.setBucketName(bucketName);
        request.setUploadId(uploadId);
        request.setPartETags(etagList.stream().map(e -> new PartETag(e.getPartNumber(), e.getEtag())).collect(Collectors.toList()));
        s3.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String bucketName, String filename, String uploadId) {
        AbortMultipartUploadRequest abortMultipartUploadRequest = new AbortMultipartUploadRequest(bucketName, filename, uploadId);
        s3.abortMultipartUpload(abortMultipartUploadRequest);
    }

    @Override
    public String fileJvsPublicLink(String fileName) {
        return s3.getUrl("jvs-public", fileName).getFile();
    }

}
