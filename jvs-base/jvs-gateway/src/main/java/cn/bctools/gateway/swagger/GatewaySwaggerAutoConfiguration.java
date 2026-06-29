package cn.bctools.gateway.swagger;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.BeanCopyUtil;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@EnableOpenApi
@Configuration
@AllArgsConstructor
public class GatewaySwaggerAutoConfiguration {

    SwaggerProperties swaggerProperties;
    public static final String APIDOC = "/v3/api-docs";

    /**
     * 使用注册中心的元数据进行请求访问多版本操作
     *
     * @param discoveryClient
     * @return
     */
    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerProvider(DiscoveryClient discoveryClient) {
        return () -> discoveryClient.getServices().stream()
                //忽略服务
                .filter(e -> !swaggerProperties.getIgnore().contains(e))
                .filter(e -> discoveryClient.getInstances(e).size() > 0)
                .flatMap(e -> discoveryClient.getInstances(e).stream().filter(s -> s.getMetadata().containsKey("name")).limit(1))
                .flatMap(e -> {
                    ArrayList<SwaggerResource> objects = new ArrayList<>();
                    //根据元数据api添加返回swagger界面地址
                    SwaggerResource swaggerResource = new SwaggerResource();
                    swaggerResource.setName("[" + e.getMetadata().get("jvs-rule-ua") + "]" + e.getMetadata().get("name"));
                    if (e.getServiceId().endsWith(SysConstant.MGR)) {
                        swaggerResource.setUrl(SysConstant.MGR_PATH + e.getServiceId().replace(SysConstant.MGR, "") + APIDOC);
                    } else if (e.getServiceId().endsWith(SysConstant.BIZ)) {
                        swaggerResource.setUrl("/api/" + e.getServiceId().replace(SysConstant.BIZ, "") + APIDOC);
                    } else {
                        //其它情况默认以两个项目名做路由头
                        swaggerResource.setUrl(e.getServiceId() + e.getServiceId() + APIDOC);
                    }
                    swaggerResource.setSwaggerVersion("3.0");
                    if (swaggerProperties.getEnable()) {
                        //只关闭默认的数据
                        objects.add(swaggerResource);
                    }
                    if (e.getServiceId().equals("jvs-design-mgr")) {
                        SwaggerResource copy = BeanCopyUtil.copy(swaggerResource, SwaggerResource.class);
                        copy.setUrl(copy.getUrl() + "?group=开发模式");
                        copy.setName(copy.getName() + "开发模式");
                        objects.add(copy);
                        SwaggerResource copy1 = BeanCopyUtil.copy(swaggerResource, SwaggerResource.class);
                        copy1.setUrl(copy1.getUrl() + "?group=测试模式");
                        copy1.setName(copy1.getName() + "测试模式");
                        objects.add(copy1);
                        SwaggerResource copy2 = BeanCopyUtil.copy(swaggerResource, SwaggerResource.class);
                        copy2.setUrl(copy2.getUrl() + "?group=正式模式");
                        copy2.setName(copy2.getName() + "正式模式");
                        objects.add(copy2);
                    }
                    //是否启用了开放api的注解
                    if (e.getMetadata().containsKey("openapi")) {
                        SwaggerResource copy2 = BeanCopyUtil.copy(swaggerResource, SwaggerResource.class);
                        copy2.setUrl(copy2.getUrl() + "?group=openApi");
                        copy2.setName(copy2.getName() + "openapi");
                        objects.add(copy2);
                    }
                    return objects.stream();
                })
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());
    }

}
