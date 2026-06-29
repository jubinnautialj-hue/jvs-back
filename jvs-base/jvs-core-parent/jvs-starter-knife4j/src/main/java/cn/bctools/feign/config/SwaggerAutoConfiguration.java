package cn.bctools.feign.config;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @author My_gj
 */
@EnableSwagger2
@Lazy(value = false)
@Configuration
public class SwaggerAutoConfiguration {

    /**
     * 指定扫描包的路径来指定要创建API的目录，一般是控制器这个包
     *
     * @return
     */
    @SneakyThrows
    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket createRestApi(SwaggerProperties swaggerProperties, ServiceInstance instance) {
        String version = Optional.ofNullable(instance.getMetadata().get("jvs-rule-ua")).orElse("");
        return new Docket(DocumentationType.OAS_30)
                .securityContexts(new ArrayList(Collections.singletonList(
                        SecurityContext.builder()
                                .securityReferences(Arrays.asList(
                                        new SecurityReference("Authorization", new AuthorizationScope[]{new AuthorizationScope("global", "用户登陆token")}),
                                        new SecurityReference("jvs-rule-ua", new AuthorizationScope[]{new AuthorizationScope("global", "由路版本号(中文需要URL转码)")})
                                ))
                                .build())))
                .securitySchemes(Arrays.asList(new ApiKey("Authorization", "用户登陆token", "header"), new ApiKey("jvs-rule-ua", "由路版本号(中文需要URL转码)", "header")))
                .apiInfo(new ApiInfoBuilder().license("jvs").contact(new Contact("jvs", "http://www.bctools.cn", null)).version(version).title(swaggerProperties.getTitle() + "  [" + version + "]" + " " + URLEncoder.encode(version,
                        Charset.defaultCharset().name())).description(swaggerProperties.getDescription()).build())
                //开启
                .enable(swaggerProperties.getEnable())
                .select()
                //扫描路径下使用@Api的controller
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

}
