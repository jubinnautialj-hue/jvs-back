package cn.bctools.design.rule.swagger.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.rule.entity.BodyInDto;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RuleParameterInDto;
import cn.bctools.design.rule.entity.RuleType;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.swagger.SwaggerApiGroupEnum;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiConfig;
import cn.bctools.design.rule.swagger.listener.RefreshRuleSwaggerApiEvent;
import cn.bctools.design.rule.swagger.listener.RefreshSwaggerApiMessage;
import cn.bctools.design.rule.swagger.listener.RefreshSwaggerApiMqConfig;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import springfox.documentation.builders.ModelSpecificationBuilder;
import springfox.documentation.builders.PropertySpecificationBuilder;
import springfox.documentation.builders.QualifiedModelNameBuilder;
import springfox.documentation.schema.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.ModelSpecificationRegistry;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.scanners.DefaultModelNamesRegistryFactory;
import springfox.documentation.spring.web.scanners.ModelSpecificationRegistryBuilder;

import java.util.Collections;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author jvs
 * 将逻辑引擎设计解析为swagger可查看的接口
 * <p>
 * 接口按模式分组， 然后按轻应用分tag
 *
 * <p>
 * 刷新接口缓存方式说明：
 * - 初始化缓存： 服务启动后获取所有逻辑API初始化到本地swagger缓存。
 * - 动态刷新缓存（逻辑API设计变更、应用变更、应用迭代时触发）： 广播缓存变更消息  -> 所有design微服务消费该广播消息，更新本地Swagger对应的缓存数据
 */
@Slf4j
@Service
@AllArgsConstructor
public class SwaggerRuleApiCacheServiceImpl implements SwaggerRuleApiCacheService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JvsAppService jvsAppService;
    private final RuleDesignService ruleDesignService;
    private final IdentificationService identificationService;
    private final JvsAppVersionService jvsAppVersionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentationCache documentationCache;

    /**
     * 逻辑API地址前缀
     */
    private static final String RULE_API_URL_PREFIX = "/rule/openapi/";

    /**
     * 逻辑API地址默认应用标识
     */
    private static final String RULE_API_URL_APP_IDENTIFICATION_DEFAULT = "{appIdentification}";
    private static final SimpleParameterSpecification SIMPLE_PARAMETER_SPECIFICATION = new SimpleParameterSpecification(ParameterStyle.SIMPLE,
            null,
            true,
            true,
            false,
            "",
            null,
            new ArrayList<>());

    @Getter
    @AllArgsConstructor
    public enum ParamTypeEnum {
        REQ("请求参数"),
        RES("响应参数");
        private final String name;
    }

    @Override
    public void initAllSwaggerRuleApi() {
        log.info("初始化所有逻辑API填充swagger缓存 begin");
        Map<String, JvsApp> jvsAppMap = jvsAppService.list().stream().collect(Collectors.toMap(JvsApp::getId, Function.identity()));
        if (ObjectNull.isNotNull(jvsAppMap)) {
            List<String> appIdList = new ArrayList<>(jvsAppMap.keySet());
            Map<String, String> appIdentifierMap = identificationService.list(Wrappers.<Identification>lambdaQuery()
                            .eq(Identification::getDesignType, DesignType.app)
                            .in(Identification::getDesignId, appIdList))
                    .stream()
                    .collect(Collectors.toMap(Identification::getDesignId, Identification::getIdentifier, (k1, k2) -> k1));
            Map<AppVersionTypeEnum, Set<String>> modeAppIdMap = jvsAppVersionService.groupAppIdByVersionType(appIdList);
            modeAppIdMap.forEach((modeType, appIds) -> {
                String mode = modeType.getValue();
                appIds.forEach(appId -> {
                    List<RuleDesignPo> ruleDesignPoList = ruleDesignService.list(Wrappers.<RuleDesignPo>lambdaQuery()
                            .eq(RuleDesignPo::getJvsAppId, appId)
                            .eq(RuleDesignPo::getReqType, RuleType.External_API_logic));
                    String appIdentifier = appIdentifierMap.get(appId);
                    ruleDesignPoList.forEach(rule -> addOrUpdateRuleApiSwaggerCache(mode, jvsAppMap.get(appId), appIdentifier, rule));
                });
            });
        }
        log.info("初始化所有逻辑API填充swagger缓存 end");
    }

    @Override
    public void publishSwaggerRuleApiEvent(Boolean delete, String appId) {
        publishSwaggerRuleApiEvent(ModeUtils.getMode(), delete, appId);
    }

    @Override
    public void publishSwaggerRuleApiEvent(AppVersionTypeEnum mode, Boolean delete, String appId) {
        publishEvent(mode.getValue(), delete, appId, null);
    }

    @Override
    public void publishSwaggerRuleApiEvent(Boolean delete, String appId, RuleDesignPo rule) {
        publishEvent(ModeUtils.getMode().getValue(), delete, appId, rule);
    }

    /**
     * 发布刷新逻辑设计到swagger任务
     *
     * @param mode   模式
     * @param delete true-删除接口
     * @param appId  应用id
     * @param rule   指定要刷新的逻辑设计，不指定，则刷新应用下所有逻辑接口
     */
    private void publishEvent(String mode, Boolean delete, String appId, RuleDesignPo rule) {
        if (ObjectNull.isNull(appId)) {
            log.warn("刷新swagger中的逻辑API缓存必须传递应用id");
            return;
        }
        String ruleId = null;
        if (ObjectNull.isNotNull(rule)) {
            if (!RuleType.External_API_logic.equals(rule.getReqType())) {
                return;
            }
            ruleId = rule.getId();
        }
        applicationEventPublisher.publishEvent(new RefreshRuleSwaggerApiEvent(this, delete, appId, ruleId, mode));
    }

    /**
     * 消费刷新Swagger中的逻辑API事件通知消息
     *
     * @param channel
     * @param message
     */
    @SneakyThrows
    @RabbitListener(queues = RefreshSwaggerApiMqConfig.QUEUE)
    public void refreshSwaggerApi(Channel channel, Message message) {
        try {
            RefreshSwaggerApiMessage msg = objectMapper.readValue(message.getBody(), RefreshSwaggerApiMessage.class);
            String appId = msg.getAppId();
            String ruleId = msg.getRuleId();
            Boolean delete = msg.getDelete();
            String mode = msg.getMode();
            JvsApp app = jvsAppService.getById(appId);
            if (ObjectNull.isNull(ruleId)) {
                if (delete) {
                    // 删除指定应用所有逻辑接口缓存
                    removeRuleApiSwaggerTagCache(mode, appId);
                } else {
                    // 刷新指定应用所有逻辑接口
                    List<RuleDesignPo> ruleDesignPoList = ruleDesignService.list(Wrappers.<RuleDesignPo>lambdaQuery()
                            .eq(RuleDesignPo::getJvsAppId, appId)
                            .eq(RuleDesignPo::getReqType, RuleType.External_API_logic));
                    String appIdentifier = Optional.ofNullable(identificationService.getOne(Wrappers.query(new Identification().setDesignType(DesignType.app).setDesignId(appId))))
                            .map(Identification::getIdentifier).orElseGet(() -> null);
                    ruleDesignPoList.forEach(ruleDesignPo -> addOrUpdateRuleApiSwaggerCache(mode, app, appIdentifier, ruleDesignPo));
                }
            } else {
                // 刷新指定逻辑接口
                if (delete) {
                    removeRuleApiSwaggerSingleCache(mode, ruleId);
                } else {
                    RuleDesignPo ruleDesignPo = ruleDesignService.getOne(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setId(ruleId)));
                    String appIdentifier = Optional.ofNullable(identificationService.getOne(Wrappers.query(new Identification().setDesignType(DesignType.app).setDesignId(appId))))
                            .map(Identification::getIdentifier).orElseGet(() -> null);
                    addOrUpdateRuleApiSwaggerCache(mode, app, appIdentifier, ruleDesignPo);
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("刷新Swagger中的逻辑API事件通知消息，MQ消费异常：", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }


    /**
     * 从缓存中移除整个应用的接口
     *
     * @param appId 应用id
     */
    private void removeRuleApiSwaggerTagCache(String mode, String appId) {
        Documentation documentation = getDocumentationByMode(mode);
        Set<String> tagNames = listTagNameByAppId(appId, documentation);
        removeTagCache(tagNames, documentation);
    }

    /**
     * 根据应用id获取tagName
     *
     * @param appId         应用id
     * @param documentation swagger缓存
     * @return 应用id对应的tagName集合
     */
    private Set<String> listTagNameByAppId(String appId, Documentation documentation) {
        String tagNameSuffix = buildTagNameSuffix(appId);
        return documentation.getApiListings().keySet()
                .stream()
                .filter(tagName -> tagName.contains(tagNameSuffix))
                .collect(Collectors.toSet());
    }

    /**
     * 删除指定tag
     *
     * @param tagNames      tagName集合
     * @param documentation swagger缓存
     */
    private void removeTagCache(Set<String> tagNames, Documentation documentation) {
        Map<String, List<ApiListing>> stringListMap = documentation.getApiListings();
        Set<Tag> tags = documentation.getTags();
        tagNames.forEach(tagName -> {
            stringListMap.remove(tagName);
            tags.removeIf(tag -> tag.getName().equals(tagName));
        });
    }

    /**
     * 从缓存中移除指定逻辑的接口
     *
     * @param ruleId 逻辑id
     */
    private void removeRuleApiSwaggerSingleCache(String mode, String ruleId) {
        Documentation documentation = getDocumentationByMode(mode);
        Map<String, List<ApiListing>> stringListMap = documentation.getApiListings();
        Set<Tag> tags = documentation.getTags();
        // 没有API的tag名
        Set<String> tagNameWithoutApi = new HashSet<>();
        for (Map.Entry<String, List<ApiListing>> stringListEntry : stringListMap.entrySet()) {
            List<ApiListing> apiListingList = stringListEntry.getValue();
            for (ApiListing apiListing : apiListingList) {
                apiListing.getApis().removeIf(api -> api.getOperations().stream().anyMatch(o -> o.getUniqueId().equals(ruleId)));
            }
            apiListingList.removeIf(apiListing -> ObjectNull.isNull(apiListing.getApis()));
            if (ObjectNull.isNull(apiListingList)) {
                tagNameWithoutApi.add(stringListEntry.getKey());
            }
        }
        // 删除没有API的tag
        tagNameWithoutApi.forEach(tagName -> {
            stringListMap.remove(tagName);
            tags.removeIf(tag -> tag.getName().equals(tagName));
        });
    }

    /**
     * 构造tag名的后缀
     *
     * @param appId 应用id
     * @return 后缀
     */
    private String buildTagNameSuffix(String appId) {
        return "[" + appId + "]";
    }

    /**
     * 新增或修改api缓存
     *
     * @param mode          模式
     * @param app           应用
     * @param appIdentifier 应用唯一标识
     * @param rule          逻辑设计
     */
    private void addOrUpdateRuleApiSwaggerCache(String mode, JvsApp app, String appIdentifier, RuleDesignPo rule) {
        Documentation documentation = getDocumentationByMode(mode);
        String appId = app.getId();

        // 按应用分Tag，包含应用下的所有逻辑
        String tagName = app.getName() + buildTagNameSuffix(appId);

        // 若应用名变更，则需先删除原应用名对应的tag
        Set<String> tagNames = listTagNameByAppId(appId, documentation);
        boolean changeTagName = tagNames.stream().anyMatch(name -> !name.equals(tagName));
        if (changeTagName) {
            removeTagCache(tagNames, documentation);
        }

        // 解析接口入参出参模型
        RuleParameterInDto parameterIn = rule.getParameterIn();
        if (ObjectNull.isNull(parameterIn) || ObjectNull.isNull(parameterIn.getMethod())) {
            log.warn("逻辑[{}]未配置入参或请求方法", rule.getId());
            return;
        }
        // 以逻辑id作为接口模型id
        String apiModelId = rule.getId();
        // 入参模型
        Map<String, ModelSpecification> modelSpecificationReqMap = iteratorParseBody(apiModelId, ParamTypeEnum.REQ, rule.getParameterIn());
        // 出参模型
        Map<String, ModelSpecification> modelSpecificationRespMap = iteratorParseBody(apiModelId, ParamTypeEnum.RES, rule.getParameterOut());
        Map<String, ModelSpecification> modelSpecificationMap = new HashMap<>();
        modelSpecificationMap.putAll(modelSpecificationReqMap);
        modelSpecificationMap.putAll(modelSpecificationRespMap);
        buildResponseModel(apiModelId, modelSpecificationMap);

        // 新增或修改接口
        Tag tag = new Tag(tagName, app.getDescription());
        // 解析消息，生成ApiDescription
        ApiDescription apiDescription = buildApiDescription(appIdentifier, apiModelId, rule, tag);
        if (ObjectNull.isNull(apiDescription)) {
            return;
        }
        // 将新的接口信息插入本地缓存
        List<ApiListing> apiListingCaches = documentation.getApiListings().get(tagName);
        if (ObjectNull.isNull(apiListingCaches)) {
            initApi(tag, apiDescription, documentation, modelSpecificationMap);
        } else {
            updateApi(rule.getId(), tag, apiDescription, modelSpecificationMap, apiListingCaches, documentation);
        }
    }

    /**
     * 获取指定模式swagger缓存
     *
     * @param mode 模式
     * @return swagger缓存
     */
    private Documentation getDocumentationByMode(String mode) {
        String apiGroupName = SwaggerApiGroupEnum.getGroupNameByMode(mode);
        return documentationCache.documentationByGroup(apiGroupName);
    }

    /**
     * 本地缓存中没有目标应用的API时进行初始化操作
     *
     * @param tag                   组
     * @param apiDescription        api
     * @param documentation         本地缓存
     * @param modelSpecificationMap 模型
     */
    private void initApi(Tag tag, ApiDescription apiDescription, Documentation documentation, Map<String, ModelSpecification> modelSpecificationMap) {
        String version = SpringContextUtil.getVersion();
        String basesPath = "/";
        String resourcePath = "/";
        String host = "";
        ModelSpecificationRegistryBuilder modelRegistryBuilder = new ModelSpecificationRegistryBuilder();
        modelRegistryBuilder.addAll(modelSpecificationMap.values());
        ModelSpecificationRegistry modelSpecificationRegistry = modelRegistryBuilder.build();
        DefaultModelNamesRegistryFactory defaultModelNamesRegistryFactory = new DefaultModelNamesRegistryFactory();
        ModelNamesRegistry modelNamesRegistry = defaultModelNamesRegistryFactory.modelNamesRegistry(modelSpecificationRegistry);
        Set<Tag> tags = new HashSet<>();
        tags.add(tag);
        ApiListing apiListing = new ApiListing(
                version,
                basesPath,
                resourcePath,
                new HashSet<>(),
                new HashSet<>(),
                host,
                new TreeSet<>(),
                new ArrayList<>(),
                Collections.singletonList(apiDescription),
                new TreeMap<>(),
                modelSpecificationMap,
                modelNamesRegistry,
                tag.getName(),
                0,
                tags);

        documentation.getApiListings().put(tag.getName(), Stream.of(apiListing).collect(Collectors.toList()));
        documentation.getTags().add(tag);
    }

    /**
     * 跟新API缓存
     *
     * @param apiId            接口id
     * @param apiDescription   api
     * @param apiListingCaches 应用下所有API缓存
     */
    private void updateApi(String apiId, Tag tag, ApiDescription apiDescription, Map<String, ModelSpecification> modelSpecificationMap, List<ApiListing> apiListingCaches, Documentation documentation) {
        ApiListing api = apiListingCaches.get(0);
        List<ApiDescription> apiDescriptionCaches = api.getApis();
        OptionalInt optional = IntStream.range(0, api.getApis().size())
                .filter(index -> apiId.equals(apiDescriptionCaches.get(index).getOperations().get(0).getUniqueId()))
                .findFirst();
        if (optional.isPresent()) {
            int index = optional.getAsInt();
            apiDescriptionCaches.add(index, apiDescription);
            apiDescriptionCaches.remove(index + 1);
        } else {
            apiDescriptionCaches.add(apiDescription);
        }

        Map<String, ModelSpecification> modelSpecifications = api.getModelSpecifications();
        List<String> removeKeys = modelSpecifications.keySet().stream().filter(key -> key.contains(apiId)).collect(Collectors.toList());
        removeKeys.forEach(key -> api.getModelSpecifications().remove(key));
        api.getModelSpecifications().putAll(modelSpecificationMap);

        ModelSpecificationRegistryBuilder modelRegistryBuilder = new ModelSpecificationRegistryBuilder();
        modelRegistryBuilder.addAll(api.getModelSpecifications().values());
        ModelSpecificationRegistry modelSpecificationRegistry = modelRegistryBuilder.build();

        // ModelNames
        DefaultModelNamesRegistryFactory defaultModelNamesRegistryFactory = new DefaultModelNamesRegistryFactory();
        ModelNamesRegistry modelNamesRegistry = defaultModelNamesRegistryFactory.modelNamesRegistry(modelSpecificationRegistry);

        Set<Tag> tags = api.getTags();
        ApiListing apiListing = new ApiListing(
                api.getApiVersion(),
                api.getBasePath(),
                api.getResourcePath(),
                api.getProduces(),
                api.getConsumes(),
                api.getHost(),
                new TreeSet<>(),
                new ArrayList<>(),
                api.getApis(),
                new TreeMap<>(),
                api.getModelSpecifications(),
                modelNamesRegistry,
                tag.getName(),
                0,
                tags);

        documentation.getApiListings().put(tag.getName(), Stream.of(apiListing).collect(Collectors.toList()));
    }


    /**
     * 构造swagger接口详情
     *
     * @param appIdentifier 应用标识
     * @param modelId       接口模型id
     * @param rule          待解析的逻辑设计
     * @param tag           接口组
     * @return swagger的ApiDescription
     */
    private ApiDescription buildApiDescription(String appIdentifier, String modelId, RuleDesignPo rule, Tag tag) {
        RuleParameterInDto parameterIn = rule.getParameterIn();
        RuleParameterInDto parameterOut = rule.getParameterOut();
        // 设置应用标识
        String identifier = ObjectNull.isNull(appIdentifier) ? RULE_API_URL_APP_IDENTIFICATION_DEFAULT : appIdentifier;
        // 设置请求地址
        parameterIn.setUrl(RULE_API_URL_PREFIX + identifier + "/" + parameterIn.getUrl());
        // 封装接口详情
        Set<String> tagNames = new HashSet<>();
        tagNames.add(tag.getName());
        Operation operation = new Operation(
                parameterIn.getMethod(),
                rule.getName(),
                rule.getDescription(),
                null,
                null,
                rule.getId(),
                0,
                tagNames,
                SwaggerRuleApiConfig.getPRODUCES(),
                SwaggerRuleApiConfig.getCONSUMES(),
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                false,
                Collections.emptyList(),
                buildRequestParameter(modelId, parameterIn),
                null,
                buildResponseParameter(modelId, parameterOut)
        );
        return new ApiDescription(tag.getName(), parameterIn.getUrl(), rule.getName(), rule.getDescription(), Collections.singletonList(operation), false);
    }


    /**
     * 构建响应参数
     *
     * @return swagger的Response
     */
    private Set<Response> buildResponseParameter(String modelId, RuleParameterInDto parameterOut) {
        Set<Response> responses = new HashSet<>();
        Set<Representation> representations = new HashSet<>();
        if (ObjectNull.isNotNull(parameterOut) && ObjectNull.isNotNull(parameterOut.getBodyList())) {
            String modelName = buildResponseModelName(ParamTypeEnum.RES.getName() + "[" + modelId + "]");
            ModelSpecification modelSpecification = new ModelSpecificationBuilder()
                    // 引用其它模型是对象时，设置引用
                    .referenceModel(ref -> ref
                            .key(modelKey -> {
                                QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder().name(modelName).namespace(buildNamespace(modelName)).build();
                                modelKey.qualifiedModelName(qualifiedModelName).build();
                            }))
                    .build();
            representations.add(new Representation(MediaType.APPLICATION_JSON, modelSpecification, new HashSet<>()));
        }
        responses.add(new Response("0", "成功", false, Collections.emptyList(), representations, Collections.emptyList(), Collections.emptyList()));
        Arrays.stream(RuleExceptionEnum.values())
                .forEach(e -> responses.add(new Response(String.valueOf(e.getCode()), e.getMsg(), false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList())));
        return responses;
    }


    /**
     * 构造非body的请求入参
     *
     * @param parameterIn 入参
     * @return swagger的RequestParameter
     */
    private Set<RequestParameter> buildRequestParameter(String modelId, RuleParameterInDto parameterIn) {
        AtomicInteger i = new AtomicInteger();
        Set<RequestParameter> requestParameters = new HashSet<>();
        if (ObjectNull.isNotNull(parameterIn.getPathList())) {
            parameterIn.getPathList().forEach(e -> {
                requestParameters.add(new RequestParameter(e.getKey(), ParameterType.PATH, e.getExplain(), e.getNecessity(), false, false, new ParameterSpecification(SIMPLE_PARAMETER_SPECIFICATION, null),
                        null,
                        new ArrayList<>(), 0,
                        new ArrayList<>(),
                        i.getAndIncrement()));
            });
        }

        if (ObjectNull.isNotNull(parameterIn.getHeaderList())) {
            parameterIn.getHeaderList().forEach(e -> {
                requestParameters.add(new RequestParameter(e.getKey(), ParameterType.HEADER, e.getExplain(), e.getNecessity(), false, false, new ParameterSpecification(SIMPLE_PARAMETER_SPECIFICATION, null),
                        null,
                        new ArrayList<>(), 0,
                        new ArrayList<>(),
                        i.getAndIncrement()));
            });
        }

        if (ObjectNull.isNotNull(parameterIn.getQueryList())) {
            parameterIn.getQueryList().forEach(e -> {
                requestParameters.add(new RequestParameter(e.getKey(), ParameterType.QUERY, e.getExplain(), e.getNecessity(), false, false, new ParameterSpecification(SIMPLE_PARAMETER_SPECIFICATION, null),
                        null,
                        new ArrayList<>(), 0,
                        new ArrayList<>(),
                        i.getAndIncrement()));
            });
        }

        if (ObjectNull.isNotNull(parameterIn.getBodyList())) {
            String modelName = ParamTypeEnum.REQ.getName() + "[" + modelId + "]";
            ModelSpecification modelSpecification = new ModelSpecificationBuilder()
                    // 引用其它模型是对象时，设置引用
                    .referenceModel(ref -> ref
                            .key(modelKey -> {
                                QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder().name(modelName).namespace(buildNamespace(modelName)).build();
                                modelKey.qualifiedModelName(qualifiedModelName).build();
                            }))
                    .build();
            Set<Representation> representations = new HashSet<>();
            representations.add(new Representation(MediaType.APPLICATION_JSON, modelSpecification, new HashSet<>()));
            requestParameters.add(
                    new RequestParameter(modelName, ParameterType.BODY, modelName, true, false, false, new ParameterSpecification(null, new ContentSpecification(false, representations)),
                            null,
                            new ArrayList<>(), 0,
                            new ArrayList<>(),
                            i.getAndIncrement()));
        }

        return requestParameters;
    }


    /**
     * 构造响应参数
     * <p>
     * 默认响应正常返回的响应参数数据结构为{"code":0, "msg":"", "timestamp":null, data: 引用具体的参数模型}
     *
     * @param modelId               参数模型id
     * @param modelSpecificationMap 接口参数模型集合
     */
    private void buildResponseModel(String modelId, Map<String, ModelSpecification> modelSpecificationMap) {
        ModelSpecification codeModel = new ModelSpecificationBuilder().scalarModel(ScalarType.INTEGER).build();
        PropertySpecification codeProperty = new PropertySpecificationBuilder("code")
                .description("code码，通常为0为正常，其它code码见异常对照表,为规范code码报错异常操作")
                .type(codeModel)
                .build();

        ModelSpecification msgModel = new ModelSpecificationBuilder().scalarModel(ScalarType.STRING).build();
        PropertySpecification msgProperty = new PropertySpecificationBuilder("msg")
                .description("返回code码为0 时， 默认为success，其它的情况为具体的消息")
                .type(msgModel)
                .build();

        ModelSpecification timestampModel = new ModelSpecificationBuilder().scalarModel(ScalarType.DATE_TIME).build();
        PropertySpecification timestampProperty = new PropertySpecificationBuilder("timestamp")
                .description("接口返回时间，默认带有毫秒数值,用于数据排查问题")
                .type(timestampModel)
                .build();

        String refModelName = ParamTypeEnum.RES.getName() + "[" + modelId + "]";
        ModelSpecification dataModel = new ModelSpecificationBuilder()
                .referenceModel(ref -> ref
                        .key(modelKey -> {
                            QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder().name(refModelName).namespace(buildNamespace(refModelName)).build();
                            modelKey.qualifiedModelName(qualifiedModelName).build();
                        }))
                .build();
        PropertySpecification dataProperty = new PropertySpecificationBuilder("data")
                .description("业务返回具体的数据")
                .type(dataModel)
                .build();

        String modelName = buildResponseModelName(refModelName);
        ModelSpecification res = buildModelSpecification(modelName, buildNamespace(modelName), Stream.of(codeProperty, msgProperty, timestampProperty, dataProperty).collect(Collectors.toList()));
        modelSpecificationMap.put(modelName, res);
    }


    /**
     * 解析body每层参数,构造模型集合
     *
     * @param modelId   模型id
     * @param paramType 参数类型
     * @param parameter 入参/出参
     * @return 当前层body转swagger的PropertySpecification
     */
    private Map<String, ModelSpecification> iteratorParseBody(String modelId, ParamTypeEnum paramType, RuleParameterInDto parameter) {
        if (ObjectNull.isNull(parameter) || ObjectNull.isNull(parameter.getBodyList())) {
            return Collections.emptyMap();
        }

        String suffix = paramType.getName();
        Map<String, ModelSpecification> modelSpecificationMap = new HashMap<>();
        ModelProperty modelProperty = new ModelProperty();
        modelProperty.setModelName(paramType.getName() + "[" + modelId + "]");
        modelProperty.setBodyList(parameter.getBodyList());

        Queue<ModelProperty> queue = new ArrayDeque<>();
        queue.add(modelProperty);
        while (!queue.isEmpty()) {
            ModelProperty model = queue.remove();
            // 解析当前层级参数
            List<PropertySpecification> propertySpecifications = model.getBodyList().stream()
                    .map(body -> {
                        if (ObjectNull.isNull(body.getInputType())) {
                            return null;
                        }
                        ScalarType scalarType = getScalarType(body.getInputType());
                        ModelSpecification modelSpecification = null;
                        if (ObjectNull.isNull(scalarType)) {
                            List<BodyInDto> childrenBodys = body.getChildren();
                            if (ObjectNull.isNull(childrenBodys)) {
                                return null;
                            }
                            BodyInDto bodyInDto = childrenBodys.get(0);
                            if ("Items".equals(bodyInDto.getKey())) {
                                // 不能有"."，否则不能显示多层级参数
                                String path = bodyInDto.getPath().replace(".", "-");
                                String refModelName = path + "[" + modelId + "]" + suffix;
                                modelSpecification = new ModelSpecificationBuilder()
                                        .collectionModel(collectionSpecificationBuilder ->
                                                collectionSpecificationBuilder
                                                        .collectionType(CollectionType.LIST)
                                                        .model(modelSpecificationBuilder -> modelSpecificationBuilder.referenceModel(ref -> ref
                                                                // 设置引用其它模型
                                                                .key(modelKey -> {
                                                                    QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder().name(refModelName).namespace(buildNamespace(refModelName)).build();
                                                                    modelKey.qualifiedModelName(qualifiedModelName).build();
                                                                })))
                                                        .build()
                                        )
                                        .build();
                                ModelProperty collectionModelProperty = new ModelProperty();
                                collectionModelProperty.setModelName(refModelName);
                                collectionModelProperty.setBodyList(bodyInDto.getChildren());
                                queue.add(collectionModelProperty);
                            }
                        } else if (ScalarType.OBJECT.equals(scalarType)) {
                            // 不能有"."，否则不能显示多层级参数
                            String path = body.getPath().replace(".", "-");
                            String refModelName = path + "[" + modelId + "]" + suffix;
                            modelSpecification = new ModelSpecificationBuilder()
                                    // 引用其它模型是对象时，设置引用
                                    .referenceModel(ref -> ref
                                            .key(modelKey -> {
                                                QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder().name(refModelName).namespace(buildNamespace(refModelName)).build();
                                                modelKey.qualifiedModelName(qualifiedModelName).build();
                                            }))
                                    .build();
                            ModelProperty refModelProperty = new ModelProperty();
                            refModelProperty.setModelName(refModelName);
                            refModelProperty.setBodyList(body.getChildren());
                            queue.add(refModelProperty);
                        } else {
                            modelSpecification = new ModelSpecificationBuilder().scalarModel(scalarType).build();
                        }


                        return new PropertySpecificationBuilder(body.getKey())
                                .description(body.getLabel())
                                .type(modelSpecification)
                                .build();

                    })
                    .filter(ObjectNull::isNotNull)
                    .collect(Collectors.toList());

            ModelSpecification modelSpecification = buildModelSpecification(model.getModelName(), buildNamespace(model.getModelName()), propertySpecifications);
            modelSpecificationMap.put(model.getModelName(), modelSpecification);
        }
        return modelSpecificationMap;
    }


    /**
     * 构造模型（就是接口的入参、出参对象）
     *
     * @param modeName   对象名，作为map的key，唯一
     * @param namespace  对象所在命名空间，唯一
     * @param properties 该对象的参数
     * @return 模型
     */
    private static ModelSpecification buildModelSpecification(String modeName, String namespace, List<PropertySpecification> properties) {
        QualifiedModelName qualifiedModelName = new QualifiedModelNameBuilder()
                .name(modeName)
                .namespace(namespace)
                .build();
        return new ModelSpecificationBuilder()
                .name(modeName)
                .compoundModel(compound ->
                        compound.modelKey(modelKeyBuilder ->
                                        modelKeyBuilder.qualifiedModelName(qualifiedModelName).build())
                                .properties(properties)
                                .maxProperties(properties.size())
                                .minProperties(properties.size()).build()
                )
                .facets(facets ->
                        facets.title(modeName)
                )
                .build();
    }

    @Data
    private static class ModelProperty {
        private String modelName;
        private List<BodyInDto> bodyList;
    }


    private String buildNamespace(String modelName) {
        return modelName + "namespace";
    }

    private String buildResponseModelName(String modelName) {
        return "R《" + modelName + "》";
    }

    /**
     * 根据配置的参数类型，找到返回给swagger的参数入参类型
     *
     * @param inputType 配置的参数入参类型
     * @return swagger能识别的参数类型
     */
    private ScalarType getScalarType(String inputType) {
        switch (inputType) {
            case "integer":
                return ScalarType.INTEGER;
            case "boolean":
                return ScalarType.BOOLEAN;
            case "array":
                return null;
            case "object":
                return ScalarType.OBJECT;
            case "number":
                return ScalarType.DOUBLE;
            case "string":
            default:
                return ScalarType.STRING;
        }
    }
}
