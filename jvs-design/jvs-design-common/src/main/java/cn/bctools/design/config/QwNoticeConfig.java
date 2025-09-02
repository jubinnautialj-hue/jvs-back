package cn.bctools.design.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qw.notice")
public class QwNoticeConfig {

   private String create;
   private String close;
   private String formUrl;
   private String appId;
   private String appSecret;
}
