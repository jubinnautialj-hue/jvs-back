package cn.bctools.feign.config;

import cn.bctools.common.annoation.OpenApi;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.util.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.service.*;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static springfox.documentation.service.Tags.toTags;
import static springfox.documentation.spring.web.paths.Paths.ROOT;

/**
 * @author jvs
 * swagger增加openApi接口组
 */
@Slf4j
@AllArgsConstructor
@Component
public class SwaggerOpenApiConfiguration implements ApplicationRunner {

    private final DocumentationCache documentationCache;
    private final SwaggerProperties swaggerProperties;
    private final ServiceInstance serviceInstance;
    private static List<Map<String, HttpMethod>> openApiPaths = null;

    @PostConstruct
    public void openApiMetadata() {
        Package aPackage = deduceMainApplicationClass().getPackage();
        openApiPaths = ClassUtil.scanPackageByAnnotation(aPackage.getName(), OpenApi.class)
                .stream()
                .map(this::openApiPaths)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (ObjectNull.isNull(openApiPaths)) {
            return;
        }
        serviceInstance.getMetadata().put("openApi", swaggerProperties.getOpenApiGroupName());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            fillOpenApiToSwagger();
        } catch (Exception e) {
            log.error("swagger增加openApi接口组异常", e);
        }
    }

    /**
     * 填充所有OpenApi到新组
     * <p>
     * 扫描使用{@link OpenApi}注解的类
     */
    private void fillOpenApiToSwagger() {
        if (ObjectNull.isNull(openApiPaths)) {
            return;
        }
        // 遍历default组下的所有接口，找到OpenApi，并加入新组
        Documentation documentation = documentationCache.documentationByGroup(Docket.DEFAULT_GROUP_NAME);
        StringVendorExtension extension = new StringVendorExtension(SysConstant.OPEN_API_MARK, "1");
        Map<String, List<ApiListing>> openApiListingMap = new HashMap<>();
        Map<String, List<ApiListing>> apiListings = documentation.getApiListings();
        for (Map.Entry<String, List<ApiListing>> stringListEntry : apiListings.entrySet()) {
            List<ApiListing> openApiListings = new ArrayList<>();
            for (ApiListing apiListing : stringListEntry.getValue()) {
                List<ApiDescription> openApis = apiListing.getApis().stream()
                        .filter(api -> openApiPaths.stream()
                                .anyMatch(apiPath -> {
                                    HttpMethod httpMethod = apiPath.get(api.getPath());
                                    if (ObjectNull.isNull(httpMethod)) {
                                        return false;
                                    }
                                    return httpMethod.equals(api.getOperations().get(0).getMethod());
                                }))
                        .collect(Collectors.toList());
                if (ObjectNull.isNull(openApis)) {
                    continue;
                }
                ApiListing openApiListing = new ApiListing(apiListing.getApiVersion(), apiListing.getBasePath(),
                        apiListing.getResourcePath(), apiListing.getProduces(), apiListing.getConsumes(),
                        apiListing.getHost(), apiListing.getProtocols(), apiListing.getSecurityReferences(),
                        openApis, apiListing.getModels(), apiListing.getModelSpecifications(),
                        apiListing.getModelNamesRegistry(), apiListing.getDescription(), apiListing.getPosition(),
                        apiListing.getTags());
                openApiListings.add(openApiListing);
            }
            if (ObjectNull.isNotNull(openApiListings)) {
                openApiListingMap.put(stringListEntry.getKey(), openApiListings);
            }
        }
        Set<Tag> tags = toTags(openApiListingMap);
        Documentation opapiDocumentation = new Documentation(swaggerProperties.getOpenApiGroupName(), ROOT, tags,
                openApiListingMap, null, new HashSet<String>(documentation.getProduces()),
                new HashSet<String>(documentation.getConsumes()), documentation.getHost(),
                new HashSet<String>(documentation.getSchemes()), documentation.getServers(),
                documentation.getExternalDocumentation(), Collections.singletonList(extension));

        if (ObjectNull.isNull(opapiDocumentation)) {
            return;
        }
        documentationCache.addDocumentation(opapiDocumentation);
        openApiPaths.clear();
    }


    /**
     * 获取接口地址
     *
     * @param clazz 类
     * @return 接口地址集合
     */
    private List<Map<String, HttpMethod>> openApiPaths(Class<?> clazz) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        String prefix = Optional.ofNullable(requestMapping).map(req -> ObjectNull.isNull(req.value()) ? null : req.value()[0]).orElse("");

        List<Map<String, HttpMethod>> apiPaths = new ArrayList<>();
        BiConsumer<HttpMethod, String[]> addPath = (httpMethod, values) -> {
            String path = prefix;
            if (ObjectNull.isNotNull(values) && values.length != 0) {
                path += values[0];
            }
            Map<String, HttpMethod> pathMap = new HashMap<>();
            pathMap.put(path, httpMethod);
            apiPaths.add(pathMap);
        };

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            GetMapping getMapping = declaredMethod.getAnnotation(GetMapping.class);
            if (ObjectNull.isNotNull(getMapping)) {
                addPath.accept(HttpMethod.GET, getMapping.value());
                continue;
            }
            PostMapping postMapping = declaredMethod.getAnnotation(PostMapping.class);
            if (ObjectNull.isNotNull(postMapping)) {
                addPath.accept(HttpMethod.POST, postMapping.value());
                continue;
            }
            PutMapping putMapping = declaredMethod.getAnnotation(PutMapping.class);
            if (ObjectNull.isNotNull(putMapping)) {
                addPath.accept(HttpMethod.PUT, putMapping.value());
                continue;
            }
            DeleteMapping deleteMapping = declaredMethod.getAnnotation(DeleteMapping.class);
            if (ObjectNull.isNotNull(deleteMapping)) {
                addPath.accept(HttpMethod.DELETE, deleteMapping.value());
            }
        }
        return apiPaths;
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return null;
    }
}
