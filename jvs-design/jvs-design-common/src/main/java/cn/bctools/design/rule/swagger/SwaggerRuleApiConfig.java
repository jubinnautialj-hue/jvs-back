package cn.bctools.design.rule.swagger;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static springfox.documentation.spring.web.paths.Paths.ROOT;

/**
 * @author jvs
 */
@Configuration
@AllArgsConstructor
public class SwaggerRuleApiConfig {

    public static final Set<String> PRODUCES = Stream.of("*/*").collect(Collectors.toSet());
    public static final Set<String> CONSUMES = Stream.of("application/json").collect(Collectors.toSet());

    private final DocumentationCache documentationCache;
    private final SwaggerRuleApiCacheService swaggerRuleApiCacheService;


    /**
     * 初始化模式对应的Swagger API组
     */
    @PostConstruct
    public void initDocumentation() {
        for (SwaggerApiGroupEnum group : SwaggerApiGroupEnum.values()) {
            Documentation documentation = new Documentation(group.getGroupName(), ROOT, new HashSet<>(),
                    new HashMap<>(), null, PRODUCES,
                    CONSUMES, "",
                    new HashSet<>(), new ArrayList<>(),
                    null, new ArrayList<>());
            documentationCache.addDocumentation(documentation);
        }
        new Thread(() -> swaggerRuleApiCacheService.initAllSwaggerRuleApi()).start();
    }
}
