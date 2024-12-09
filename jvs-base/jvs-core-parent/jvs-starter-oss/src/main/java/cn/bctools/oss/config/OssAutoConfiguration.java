package cn.bctools.oss.config;

import cn.bctools.auth.api.api.FileApi;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.oss.controller.FileUploadController;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.mapper.SysFileLabelMapper;
import cn.bctools.oss.po.OssFile;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.service.FileDataInterface;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.oss.template.S3Template;
import cn.bctools.oss.template.local.LocalFileTemplate;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

/**
 * aws 自动配置类
 *
 * @author Administrator
 */
@Slf4j
@EnableConfigurationProperties({OssProperties.class})
public class OssAutoConfiguration {

    @Autowired
    private OssProperties ossProperties;

    public OssAutoConfiguration(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 如果用户要上传文件的数据自行保存, 则重新定此bean即可
     *
     * @param fileApi
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(FileDataInterface.class)
    @ConditionalOnProperty(value = "oss.name", matchIfMissing = true)
    public FileDataInterface fileDataInterface(FileApi fileApi) {

        return new FileDataInterface() {

            @Override
            public void insert(BaseFile baseFile) {
                OssFile entity = new OssFile()
                        .setBucketName(baseFile.getBucketName())
                        .setCreateTime(LocalDateTime.now())
                        .setFileName(baseFile.getOriginalName())
                        .setFileType(baseFile.getFileType())
                        .setModule(baseFile.getModule())
                        .setSize(baseFile.getSize())
                        .setFilePath(baseFile.getFileName())
                        .setLabel(SystemThreadLocal.get("label"));
                fileApi.insert(JSONObject.toJSONString(entity));
                log.info("上传成功一个文件:{}", JSONObject.toJSONString(baseFile));
            }

            @Override
            public void newBucket(String bucketName) {
                log.info("创建一个新的桶:{}", bucketName);
            }

        };
    }

    @Bean
    @ConditionalOnProperty(value = "oss.name", havingValue = "local")
    public OssTemplate local(FileDataInterface fileDataInterface, RedisUtils redisUtils) {
        return new LocalFileTemplate(ossProperties, fileDataInterface, redisUtils);
    }

    @Bean
    @ConditionalOnProperty(value = "oss.name", havingValue = "s3")
    @ConditionalOnMissingBean
    public OssTemplate s3(FileDataInterface fileDataInterface, RedisUtils redisUtils) {
        return new S3Template(ossProperties, fileDataInterface, redisUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileUploadController ossEndpoint(FileDataInterface fileDataInterface, OssTemplate ossTemplate, RedisUtils redisUtils, SysFileLabelMapper labelMapper) {
        return new FileUploadController(fileDataInterface, ossTemplate, labelMapper, redisUtils);
    }

}
