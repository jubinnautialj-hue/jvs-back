package cn.bctools.rule.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.Operation;
import springfox.documentation.service.ParameterType;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.SpringfoxWebConfiguration;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gj
 */
@Slf4j
@Import({SpringContextUtil.class, SpringfoxWebConfiguration.class})
@AllArgsConstructor
@Lazy(value = false)
@Component
public class RuleSpringContextUtil implements ApplicationRunner {

    private static final String SYS_NAME = "jvs-design-mgr";
    DocumentationCache documentationCache;
    RestTemplate restTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (SYS_NAME.equals(SpringContextUtil.getApplicationContextName())) {
            return;
        }
        Package aPackage = deduceMainApplicationClass().getPackage();
        //获取所有有逻辑声明的注解
        Map<String, Method> methodMap = ClassUtil.scanPackageByAnnotation(aPackage.getName(), RequestMapping.class).stream()
                .flatMap(e -> Arrays.stream(e.getMethods()).filter(s -> ObjectNull.isNotNull(s.getAnnotation(Rule.class))))
                .collect(Collectors.toMap(e -> e.getAnnotation(Rule.class).value(), Function.identity()));

        Documentation aDefault = documentationCache.all().get("default");
        if (ObjectNull.isNotNull(aDefault)) {

            List<ApiDescription> apiList = aDefault.getApiListings().values().stream().map(e -> e.get(0))
                    .flatMap(e -> e.getApis().stream())
                    .filter(e -> methodMap.containsKey(e.getOperations().get(0).getSummary())).collect(Collectors.toList());

            Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(aPackage.getName(), RequestMapping.class);
            List<Dict> map = new ArrayList<>(classes.size());
            if (ObjectNull.isNotNull(apiList)) {
                for (ApiDescription apiDescription : apiList) {
                    getMethod(map, methodMap, apiDescription);
                }
            }

            if (map.isEmpty()) {
                return;
            }
            try {
                String s = restTemplate.postForObject("http://jvs-design-mgr/register/" + SpringContextUtil.getApplicationContextName(), map, String.class);
                R r = JSONObject.parseObject(s, R.class);
                if (!r.is()) {
                    log.info("逻辑注册成功");
                } else {
                    log.error("逻辑注册失败,请重启服务");
                }
            } catch (Exception e) {
                log.error("服务未自动注册");
            }
        }
        //标记是否为逻辑扩展
    }

    /**
     * 初始化服务
     *
     * @param map            组装对象
     * @param methodMap      方法对象
     * @param apiDescription swagger api
     */
    private void getMethod(List<Dict> map, Map<String, Method> methodMap, ApiDescription apiDescription) {
        Operation operation = apiDescription.getOperations().get(0);
        Method method = methodMap.get(operation.getSummary());
        Rule annotation = method.getAnnotation(Rule.class);
        //只用于第一个参数做为请求对象
        List<Dict> collect = new ArrayList<>();
        operation.getParameters().forEach(e -> {
            if (e.getParamType().equals(ParameterType.BODY.getIn())) {
                Arrays.stream(e.getType().get().getErasedType().getDeclaredFields())
                        .filter(s -> !"serialVersionUID".equals(s.getName()))
                        .forEach(s -> {
                            ApiModelProperty property = s.getAnnotation(ApiModelProperty.class);
                            if (ObjectNull.isNull(property)) {
                                throw new BusinessException(s + "没有属性注解ApiModelProperty.class");
                            }
                            collect.add(Dict.create()
                                    .set("key", s.getName())
                                    .set("info", property.value())
                                    .set("paramType", e.getParamType().toUpperCase())
                                    .set("explain", property.value())
                                    .set("necessity", property.required())
                                    .set("inputType", "input"));
                        });
            } else {
                collect.add(Dict.create()
                        .set("key", e.getName())
                        .set("paramType", e.getParamType().toUpperCase())
                        .set("info", e.getDescription())
                        .set("explain", e.getDescription())
                        .set("necessity", e.getRequired())
                        .set("inputType", "input"));
            }
        });
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(annotation));
        Dict parse = Dict.parse(jsonObject)
                .set("parameters", collect)
                .set("functionName", annotation.value())
                .set("method", operation.getMethod())
                .set("icon", annotation.iconUrl())
                .set("explain", annotation.explain())
                .set("group", annotation.group())
                //获取请求的url地址  固定使用lb 服务内请求
                .set("functionId", "lb://" + SpringContextUtil.getApplicationContextName() + apiDescription.getPath());
        map.add(parse);

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
