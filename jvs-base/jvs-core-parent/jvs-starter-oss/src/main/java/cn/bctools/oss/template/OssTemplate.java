package cn.bctools.oss.template;

import cn.bctools.common.utils.*;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.Etag;
import cn.bctools.oss.utils.OssUtils;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import io.undertow.util.MimeMappings;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈OssTemplate〉
 *
 * @author auto
 * @since 1.0.0
 */
public interface OssTemplate {

    /**
     * The constant STR.
     */
    String STR = "{\n" +
            "    \"Version\":\"2012-10-17\",\n" +
            "    \"Statement\":[\n" +
            "        {\n" +
            "            \"Effect\":\"Allow\",\n" +
            "            \"Principal\":{\n" +
            "                \"AWS\":[\n" +
            "                    \"*\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"Action\":[\n" +
            "                \"s3:GetBucketLocation\",\n" +
            "                \"s3:ListBucket\",\n" +
            "                \"s3:ListBucketMultipartUploads\"\n" +
            "            ],\n" +
            "            \"Resource\":[\n" +
            "                \"arn:aws:s3:::%s\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"Effect\":\"Allow\",\n" +
            "            \"Principal\":{\n" +
            "                \"AWS\":[\n" +
            "                    \"*\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"Action\":[\n" +
            "                \"s3:DeleteObject\",\n" +
            "                \"s3:GetObject\",\n" +
            "                \"s3:ListMultipartUploadParts\",\n" +
            "                \"s3:PutObject\",\n" +
            "                \"s3:AbortMultipartUpload\"\n" +
            "            ],\n" +
            "            \"Resource\":[\n" +
            "                \"arn:aws:s3:::%s/*\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    /**
     * The constant MIME_MAPPINGS.
     */
    Map<String, String> MIME_MAPPINGS = new HashMap<String, String>(101) {
        {
            // 初始化文件类型，并添加更多类型
            putAll(MimeMappings.DEFAULT_MIME_MAPPINGS);
            put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            put("doc", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
    };

    /**
     * 获取所有文件桶
     *
     * @return list
     */
    List<String> listBuckets();

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void makeBucket(String bucketName);

    /**
     * 存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 判断结果 boolean
     */
    boolean bucketExists(String bucketName);

    /**
     * 刷新桶文件到数据库中
     *
     * @param bucketName 桶文件
     * @param path       前匹配路径
     * @param label      label
     */
    void listObjects(String bucketName, String path, String label);

    /**
     * 获取文件外链
     *
     * @param fileName   文件名称
     * @param bucketName 源存储桶
     * @return String string
     */
    String fileLink(String fileName, String bucketName);

    /**
     * 上传文件
     *
     * @param bucketName 存储桶名称
     * @param businessId the business id
     * @param module     模块
     * @param stream     the stream
     * @param key        the key
     * @param cover      the cover
     * @return BaseFile base file
     */
    BaseFile put(String bucketName, String businessId, String module, InputStream stream, String key, boolean cover);

    /**
     * Put file base file.
     *
     * @param bucketName   the bucket name
     * @param module       the module
     * @param originalName the original name
     * @param file         the file
     * @return the base file
     */
    @SneakyThrows
    default BaseFile putFile(String bucketName, String module, String originalName, MultipartFile file) {
        //没有文件后坠
        String str = ".";
        if (originalName.indexOf(str) == -1) {
            if (MIME_MAPPINGS.containsValue(file.getContentType())) {
                String value = MIME_MAPPINGS.keySet().stream()
                        .filter(e -> MIME_MAPPINGS.get(e).equals(file.getContentType()))
                        .findFirst().get();
                originalName += str + value;
            }
        }
        return putFile(bucketName, module, originalName, file.getInputStream());
    }


    /**
     * 上传文件
     *
     * @param bucketName   存储桶名称
     * @param businessId   the business id
     * @param module       模块
     * @param originalName 上传文件名
     * @param inputStream  文件
     * @return BaseFile base file
     */
    default BaseFile putFile(String bucketName, String businessId, String module, String originalName, InputStream inputStream) {
        return put(bucketName, businessId, module, inputStream, originalName, false);
    }

    /**
     * Put file base file.
     *
     * @param bucketName   the bucket name
     * @param module       the module
     * @param originalName the original name
     * @param inputStream  the input stream
     * @return the base file
     */
    default BaseFile putFile(String bucketName, String module, String originalName, InputStream inputStream) {
        return put(bucketName, null, module, inputStream, originalName, false);
    }

    /**
     * 上传文件
     *
     * @param bucketName   存储桶名称
     * @param module       模块
     * @param stream       上传文件InputStream
     * @param originalName 上传文件名
     * @param cover        是否覆盖
     * @return BaseFile base file
     */
    BaseFile put(String bucketName, String module, InputStream stream, String originalName, boolean cover);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param fileName   文件名称
     */
    void removeFile(String bucketName, String fileName);


    /**
     * 根据规则生成文件名称规则
     * <p>
     * 文件名称规则: 模块名称/日期-雪花id-原文件名
     *
     * @param module           模块
     * @param originalFilename 原始文件名
     * @param businessId       业务的 id，默认为服务名
     * @return string file name
     */
    default String getFileName(String module, String originalFilename, String businessId) {
        // 日期-雪花id-原文件名
        String today = DateUtil.format(new Date(), "yyyy/MM/dd/");
        String uniqueFilename = today + DateUtil.today() + IdUtil.getSnowflakeNextId() + StringPool.DASH + originalFilename;
        if (ObjectUtil.isEmpty(module)) {
            return uniqueFilename;
        }
        if (!module.startsWith(StringPool.SLASH)) {
            // 去掉开头的 '/'
            module = StringPool.SLASH + module;
        }
        //添加项目名加模块名,或添加一个文件上传的请求头进行数据归类,如轻应用的id，知识库的id
        if (ObjectNull.isNull(businessId)) {
            businessId = Optional.ofNullable(OssUtils.getOssTemplateBusinessId()).map(Object::toString).orElse(null);
        }
        if (ObjectNull.isNull(businessId)) {
            businessId = Optional.ofNullable(businessId).orElse(SpringContextUtil.getVersion().replaceAll("\\.", "_"));
        }
        //租户 + 业务 + 服务 + 功能目录
        module = "ten_" + TenantContextHolder.getTenantId() + StringPool.SLASH + businessId + StringPool.SLASH + SpringContextUtil.getApplicationContextName() + StringPool.SLASH + module;
        if (!module.endsWith(StringPool.SLASH)) {
            // 末尾添加 '/'
            module = module + StringPool.SLASH;
        }
        // 拼接模块名称
        return (module + uniqueFilename).replaceAll("//", "/");
    }

    /**
     * 获取文件流
     *
     * @param bucketName 存储桶
     * @param fileName   文件名称
     * @return InputStream object
     */
    InputStream getObject(String bucketName, String fileName);

    /**
     * 根据桶名获取文件列表信息
     *
     * @param bucketName 桶名
     * @return 桶下文件集 list
     */
    List<BaseFile> listFiles(String bucketName);

    /**
     * 创建分段上传
     *
     * @param bucketName 桶
     * @param filename   文件名
     * @return 分段上传ID string
     */
    String createMultipartUpload(String bucketName, String filename);

    /**
     * 分段上传文件
     *
     * @param bytes      文件
     * @param bucketName 桶
     * @param partNumber 分片序号
     * @param fileName   the file name
     * @param uploadId   分片上传凭证
     * @return 分片etag etag
     */
    Etag uploadPart(byte[] bytes, String bucketName, Integer partNumber, String fileName, String uploadId);

    /**
     * 完成分段上传
     *
     * @param bucketName 桶
     * @param filename   文件名
     * @param uploadId   凭证
     * @param etagList   分片etag
     */
    void completeMultipartUpload(String bucketName, String filename, String uploadId, Set<Etag> etagList);

    /**
     * 终止分段上传
     *
     * @param bucketName 桶
     * @param filename   文件名
     * @param uploadId   凭证
     */
    void abortMultipartUpload(String bucketName, String filename, String uploadId);

    /**
     * 推送文件到默认桶下面。默认为项目名的桶
     *
     * @param originalName 文件名
     * @param inputStream  文件内容的输入流
     * @param catalogue    文件目录
     * @return 文件上传后的信息 base file
     */
    default BaseFile putFile(String originalName, InputStream inputStream, String... catalogue) {
        String module = String.join(StringPool.SLASH, catalogue);
        return putFile(SpringContextUtil.getApplicationContextName(), module, originalName, inputStream);
    }

    /**
     * 上传流文件
     *
     * @param bucketName   桶
     * @param originalName 名称
     * @param inputStream  流文件
     * @param catalogue    存储目录
     * @return base file
     */
    default BaseFile putFile(String bucketName, String originalName, InputStream inputStream, String... catalogue) {
        String module = String.join(StringPool.SLASH, catalogue);
        return putFile(bucketName, module, originalName, inputStream);
    }

    /**
     * 推送文本到出桶中
     *
     * @param originalName 文件名
     * @param content      文件内容
     * @param catalogue    文件目录
     * @return 文件上传后的信息 base file
     */
    default BaseFile putContent(String originalName, String content, String... catalogue) {
        byte[] serialize = ObjectUtil.serialize(content);
        String module = String.join(StringPool.SLASH, catalogue);
        return putFile(SpringContextUtil.getApplicationContextName(), module, originalName, new ByteArrayInputStream(serialize));
    }

    /**
     * 转换连接
     *
     * @param bucketName 文件桶名
     * @param baseFiles  对应的文件文件对象信息
     * @return list
     */
    default List<BaseFile> echoList(String bucketName, List<BaseFile> baseFiles) {
        return baseFiles.stream().map(e -> {
            String fileLink = fileLink(e.getFileName(), bucketName);
            //转换
            e.setUrl(fileLink);
            return e;
        }).collect(Collectors.toList());
    }

    /**
     * 公共桶文件
     *
     * @param fileName 文件名
     * @return 返回的链接 以  / 路径返回为了保证前端信息
     */
    String fileJvsPublicLink(String fileName);
}
