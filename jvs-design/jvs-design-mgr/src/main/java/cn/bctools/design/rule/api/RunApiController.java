package cn.bctools.design.rule.api;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.*;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.*;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.UrlUtils;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * API 主要是外部系统调用时走的接口
 *
 * @author zx  跑逻辑与设计请求接口  逻辑引擎调用入口【此为通过api调用方式，如前端直接调用，三方系统调用，或集群服务内相互调用】 针对于应用中心创建时的逻辑，并在源码开发标识管理中有声明的。 不同的业务场景需求调用逻辑的方式有所不同。 以下举例几种不同场景： 1、在轻应用中添加自定义开发的页面，调用逻辑引擎。 通过配置的标识，调用 2、三方系统通过api和凭证调用。 <p> 3、同集群同注册中心下不需要登录时调用逻辑引擎。通过feign调用。或通过标识调用。
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "逻辑引擎API")
@RestController
@RequestMapping
public class RunApiController {

    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;
    /**
     * The Rule service.
     */
    RuleDesignService ruleService;
    /**
     * The Rule start utils.
     */
    RuleStartUtils ruleStartUtils;
    /**
     * The Run log service.
     */
    RunLogService runLogService;
    /**
     * The Jvs app service.
     */
    JvsAppService jvsAppService;
    /**
     * The Service.
     */
    IdentificationService service;
    /**
     * The Oss template.
     */
    OssTemplate ossTemplate;

    private final static String RULE_KEY_FORMAT = "rule:run:key:%s";

    /**
     * Identification r.
     *
     * @param appIdentification the app identification
     * @param request           the request
     * @return the r
     */
    @SneakyThrows
    @Log
    @ApiOperation("通过标识发起逻辑引擎")
    @RequestMapping(value = "/rule/openapi/{appIdentification}/**", method = {RequestMethod.DELETE, RequestMethod.GET})
    public R identification(@PathVariable("appIdentification") String appIdentification, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>(8);
        return identification(appIdentification, map, request, response);
    }

    /**
     * 通过标识前端直接调用逻辑引擎
     * 通过应用标识和逻辑标识直接调用此逻辑引擎
     *
     * @param appIdentification the app identification
     * @param map               the map
     * @param request           the request
     * @return the r
     */
    @SneakyThrows
    @Log
    @ApiOperation("通过标识发起逻辑引擎")
    @RequestMapping(value = "/rule/openapi/{appIdentification}/**", consumes = {"multipart/form-data", "application/json"}, method = {RequestMethod.POST, RequestMethod.PUT})
    public R identification(@PathVariable("appIdentification") String appIdentification, Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) {
        TenantContextHolder.clear();
        map = JSONObject.parseObject(IoUtil.read(request.getInputStream()).toString());
        if (ObjectNull.isNull(map)) {
            map = new HashMap<>(8);
        }
        Identification byIdentifier = service.getByIdentifierApp(appIdentification);
        if (ObjectNull.isNull(byIdentifier)) {
            throw new RuleException(RuleExceptionEnum.请求地址不存在或未配置标识);
        }
        //设置租户
        TenantContextHolder.setTenantId(byIdentifier.getTenantId());
        if (ObjectNull.isNull(byIdentifier) && byIdentifier.getDesignType().equals(DesignType.app)) {
            throw new RuleException(RuleExceptionEnum.请求地址不存在或未配置标识);
        }
        String uri = request.getRequestURI();
        Enumeration<String> parameterNames = request.getParameterNames();
        //判断是否存在文件对象信息  如果存在将文件对象也注入请求数据中
        hasFilePart(map, request);
        //拼装url参数
        if (parameterNames.hasMoreElements()) {
            String s = parameterNames.nextElement();
            map.put(s, request.getParameter(s));
        }
        //获取逻辑的标识
        String ruleIdentification = uri.substring(uri.indexOf(appIdentification) + appIdentification.length());
        //将url的参数key放到map中
        //根据逻辑的标识获取具体的逻辑引擎
        Identification identification = service.getByIdentifierRule(ruleIdentification, byIdentifier.getJvsAppId(), map);
        if (ObjectNull.isNull(identification)) {
            throw new RuleException(RuleExceptionEnum.请求地址不存在或未配置标识);
        }
        String appId = byIdentifier.getJvsAppId();
        String ruleId = identification.getDesignId();
        RuleDesignPo ruleDesignPo = ruleService.getEnableDesign(ruleId);
        if (ObjectNull.isNull(ruleDesignPo)) {
            throw new RuleException(RuleExceptionEnum.请求地址不存在或未配置标识);
        }
        if (ObjectNull.isNull(ruleDesignPo.getReqType())) {
            throw new RuleException(RuleExceptionEnum.不支持调用);
        }
        switch (ruleDesignPo.getReqType()) {
            case Source_code_development_docking_logic:
                //判断此逻辑的类型，是否需要登录如果需要登录，则获取一下当前用户
                UserCurrentUtils.getCurrentUser();
                break;
            case External_API_logic:
                //正常执行  判断用户添加的key和传递的key 是否一致 或ip白名单
                String ipAddr = IpUtil.getIpAddr(request);
                if (ObjectNull.isNotNull(ruleDesignPo.getApiCheckDto())) {
                    if (ObjectNull.isNotNull(ruleDesignPo.getApiCheckDto().getIps())) {
                        if (!ruleDesignPo.getApiCheckDto().getIps().contains(ipAddr)) {
                            throw new RuleException(RuleExceptionEnum.ip校验不通过);
                        }
                    }
                    if (ObjectNull.isNotNull(ruleDesignPo.getApiCheckDto().getAccessToken())) {
                        String accessToken = request.getParameter("accessToken");
                        if (ObjectNull.isNull(accessToken)) {
                            throw new RuleException(RuleExceptionEnum.accessToken校验不通过);
                        }
                        if (!ruleDesignPo.getApiCheckDto().getAccessToken().contains(accessToken)) {
                            throw new RuleException(RuleExceptionEnum.accessToken校验不通过);
                        }
                    }
                }
                //删除凭证
                map.remove("accessToken");
                break;
            default:
                throw new RuleException(RuleExceptionEnum.不支持调用);
        }
        return runApiRule(appId, ruleId, map, ruleDesignPo, request, response);
    }

    /**
     * @param map     对象信息
     * @param request 请求参数
     * @throws IOException
     * @throws ServletException
     */
    private void hasFilePart(Map<String, Object> map, HttpServletRequest request) {
        try {
            for (Part part : request.getParts()) {
                if (ObjectNull.isNull(part.getSubmittedFileName())) {
                    continue;
                }
                InputStream inputStream = part.getInputStream();
                BaseFile baseFile = ossTemplate.putFile("jvs-public", "rule/run/file", part.getSubmittedFileName(), inputStream);
                RuleFile copy = BeanCopyUtil.copy(baseFile, RuleFile.class);
                copy.setUrl(ossTemplate.fileLink(baseFile.getFileName(), "jvs-public"));
                map.put(part.getName(), copy);
                if (part.getContentType() != null) {
                    // 如果 part 的 contentType 不为 null，说明它是一个文件对象
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
    /**
     * 添加指定回调地址，用于匹配不同的逻辑规则，需要校验凭证的。
     */
    /**
     * Return test r.
     *
     * @param id the id
     * @return r
     */
    @SneakyThrows
    @Log
    @ApiOperation("获取异步处理结果")
    @GetMapping("/api/{id}")
    public R returnTest(@PathVariable String id) {
        //判断数据库中是否存在
        final String key = String.format(RULE_KEY_FORMAT, id);
        //判断缓存中是否存在
        RuleExecuteDto result = (RuleExecuteDto) redisUtils.get(key);
        if (Objects.isNull(result)) {
            return R.failed("没有数据");
        }
        //如果结束了,直接删除掉缓存数据
        if (Boolean.TRUE.equals(result.getIsEnd())) {
            redisUtils.del(key);
        }
        return R.ok(result.getEndResult().getValue());
    }

    /**
     * 根据逻辑标识key运行逻辑流程, 并返回执行结果
     * <p>
     * 参数优先级:
     * 路径参数 > 请求体参数 > 数据库参数
     *
     * @param request     the request
     * @param appId       应用凭证
     * @param secret      应用密钥
     * @param ruleKey     请求模板参数
     * @param variableMap 数据值对象
     * @return 运行跑出来的结果数据 r
     * @author zx
     */
    @Log
    @ApiOperation("执行逻辑")
    @RequestMapping(value = "/api/run/{ruleKey}", method = {RequestMethod.PUT, RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public R<Object> run(HttpServletRequest request, @RequestHeader("appId") String appId, @RequestHeader("secret") String secret, @PathVariable String ruleKey,
                         @RequestBody(required = false) Map<String, Object> variableMap, HttpServletResponse response) {
        //判断应用是否已经启用
        TenantContextHolder.clear();
        //清楚租户
        log.info("执行逻辑, appId: {}", appId);
        JvsApp byId = jvsAppService.getById(appId);
        if (ObjectNull.isNull(byId)) {
            return R.failed("没有此应用");
        }
        if (!byId.getSecret().equals(secret)) {
            return R.failed("密钥匹配失败");
        }

        //根据租户ID调用自己的服务
        RuleDesignPo po = ruleService.getById(ruleKey);
        return runApiRule(appId, ruleKey, variableMap, po, request, response);
    }

    @NotNull
    public R<Object> runApiRule(String appId, String ruleKey, Map<String, Object> variableMap, RuleDesignPo po, HttpServletRequest request, HttpServletResponse response) {
        //设置id值
        TenantContextHolder.setTenantId(po.getTenantId());
        //直接返回
        if (ObjectNull.isNull(po)) {
            return R.failed("凭证不正确");
        }
        //校验参照是否符合规范
        checkParameterIn(variableMap, po.getParameterIn(), request);
        //取缓存值
        String cacheKey = getRuleCacheKey(po, variableMap, po.getParameterIn());
        if (ObjectNull.isNotNull(cacheKey)) {
            Object cache = redisUtils.get(cacheKey);
            if (ObjectNull.isNotNull(cache)) {
                return R.ok(cache);
            }
        }
        //根据参数获取的值
        RuleSystemThreadLocal.setParameterSelectedOption(variableMap);

        log.info("执行逻辑, ruleKey: {}, 租户id: {}", ruleKey, po.getTenantId());
        // 获取逻辑流程运行时参数
        // 1.获取数据库参数
        Map<String, Object> ruleVariable = new HashMap<>(16);
        if (po.getParameterPos() != null) {
            ruleVariable.putAll((po.getParameterPos()));
        }
        // 2.获取请求体参数
        if (ObjectUtils.isNotEmpty(variableMap)) {
            ruleVariable.putAll(variableMap);
        }
        // 3.获取请求路径上的参数(优先级最高)
        Map<String, Object> urlParams = UrlUtils.getUrlParams();
        if (ObjectUtils.isNotEmpty(urlParams)) {
            ruleVariable.putAll(urlParams);
        }
        RunLogPo logPo = runLogService.create(appId, po.getSecret(), RunType.API, ruleVariable, po.getReqType(), po.getReqType(), po.getSync());
        TenantContextHolder.setTenantId(po.getTenantId());
        logPo.setTenantId(po.getTenantId());
        runLogService.updateById(logPo);
        ruleVariable.put("ruleKey", ruleKey);
        RuleExecuteDto data = new RuleExecuteDto().setReqVariableMap(variableMap).setVariableMap(ruleVariable);
        String key = String.format(RuleConstant.RULE_KEY_FORMAT, logPo.getId());
        SystemThreadLocal.set("redisKey", key);
        redisUtils.set(key, data, Long.valueOf(60 * 5));
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        RuleExecDto ruleExecDto = new RuleExecDto().setExecuteDto(data).setType(RunType.API).setSecret(po.getSecret()).setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
        if (po.getSync()) {
            CompletableFuture.runAsync(() -> {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                ruleStartUtils.start(po, logPo, ruleExecDto);
                redisUtils.set(key, data, Long.valueOf(60 * 5));
            }, RuleStartUtils.EXECUTOR);
            //返回执行日志对象
            return R.ok(logPo.getId());
        } else {
            ruleStartUtils.getRuleReturn(response, po, logPo, data, ruleExecDto);
            boolean notNull = ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getException());
            if (ruleExecDto.getExecuteDto().getEndResult().getFunctionName().equals("提示消息")) {
                if (notNull) {
                    throw ruleExecDto.getExecuteDto().getException();
                }
            }
            //返回执行日志对象
            if (ObjectNull.isNull(data.getEndResult())) {
                return R.ok();
            }
            if (ObjectNull.isNotNull(cacheKey) && !notNull) {
                //默认为存储 1 小时
                redisUtils.set(cacheKey, data.getEndResult().getValue(), Duration.ofMinutes(po.getCacheMinute()));
            }
            return R.ok(data.getEndResult().getValue());
        }
    }

    /**
     * 根据参数获取缓存的 key
     *
     * @param po          对象
     * @param variableMap 请求参数对象值
     * @param parameterIn 所有的参数校验规则
     * @return
     */
    private String getRuleCacheKey(RuleDesignPo po, Map<String, Object> variableMap, RuleParameterInDto parameterIn) {
        if (ObjectNull.isNotNull(po.getCacheMinute(), parameterIn)) {
            List<Object> list = new ArrayList<>();
            if (ObjectNull.isNotNull(parameterIn.getQueryList())) {
                parameterIn.getQueryList().stream().filter(e -> ObjectNull.isNotNull(e.getCache())).filter(ParameterMap::getCache).map(e -> JvsJsonPath.read(variableMap, e.getKey())).forEach(list::add);
            }
            if (ObjectNull.isNotNull(parameterIn.getHeaderList())) {
                parameterIn.getHeaderList().stream().filter(e -> ObjectNull.isNotNull(e.getCache())).filter(ParameterMap::getCache).map(e -> JvsJsonPath.read(variableMap, e.getKey())).forEach(list::add);
            }
            getBodyCacheKey(parameterIn.getBodyList(), variableMap, list);
            if (ObjectNull.isNotNull(list)) {
                String key = list.stream().map(Object::toString).collect(Collectors.joining("."));
                return SysConstant.redisKey("rule:cache", po.getSecret() + key);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取请求参数中的缓存是否命中缓存数据
     *
     * @param bodyList    body结构
     * @param variableMap 请求参数变量
     * @param list        数据
     */
    private void getBodyCacheKey(List<BodyInDto> bodyList, Map<String, Object> variableMap, List<Object> list) {
        if (ObjectNull.isNotNull(bodyList)) {
            for (BodyInDto bodyInDto : bodyList) {
                if (ObjectNull.isNotNull(bodyInDto.getCache()) && bodyInDto.getCache()) {
                    Object read = JvsJsonPath.read(variableMap, bodyInDto.getPath());
                    list.add(read);
                }
                if (ObjectNull.isNotNull(bodyInDto.getChildren())) {
                    getBodyCacheKey(bodyInDto.getChildren(), variableMap, list);
                }
            }
        }
    }

    /**
     * 校验请求参数是否符合规范
     *
     * @param variableMap 请求参数
     * @param parameterIn 校验规则
     * @param request
     */
    private void checkParameterIn(Map<String, Object> variableMap, RuleParameterInDto parameterIn, HttpServletRequest request) {
        //校验类型
        if (ObjectNull.isNull(parameterIn)) {
            return;
        }
        //参数
        for (ParameterMap parameterMap : parameterIn.getHeaderList()) {
            if (ObjectNull.isNotNull(parameterMap.getNecessity()) && parameterMap.getNecessity()) {
                String header = request.getHeader(parameterMap.getKey());
                if (ObjectNull.isNull(header)) {
                    throw new RuleException(RuleExceptionEnum.入参校验不通过, parameterMap.getExplain() + "参数不存在");
                } else {
                    variableMap.put(parameterMap.getKey(), header);
                }
            } else {
                Optional<String> header = Optional.ofNullable(request.getHeader(parameterMap.getKey()));
                if (header.isPresent()) {
                    variableMap.put(parameterMap.getKey(), header.get());
                }
            }
        }
        //参数
        for (ParameterMap parameterMap : parameterIn.getQueryList()) {
            if (ObjectNull.isNotNull(parameterMap.getNecessity(), parameterMap.getKey()) && parameterMap.getNecessity()) {
                String parameter = request.getParameter(parameterMap.getKey());
                if (ObjectNull.isNull(parameter)) {
                    throw new RuleException(RuleExceptionEnum.入参校验不通过, parameterMap.getExplain() + "参数不存在");
                } else {
                    variableMap.put(parameterMap.getKey(), parameter);
                }
            } else {
                Optional<String> parameter = Optional.ofNullable(request.getParameter(parameterMap.getKey()));
                if (parameter.isPresent()) {
                    variableMap.put(parameterMap.getKey(), parameter.get());
                }
            }
        }
        String method = request.getMethod();
        if (ObjectNull.isNotNull(parameterIn.getMethod()) && !parameterIn.getMethod().toString().equals(method)) {
            throw new RuleException(RuleExceptionEnum.入参校验不通过, "请求类型不正常");
        }
        List<BodyInDto> bodyList = parameterIn.getBodyList();
        if (HttpMethod.PUT.equals(parameterIn.getMethod()) || HttpMethod.POST.equals(parameterIn.getMethod())) {
            recursiveVerification(variableMap, bodyList);
        }
    }

    private static void recursiveVerification(Map<String, Object> variableMap, List<BodyInDto> bodyList) {
        //处理body
        for (BodyInDto bodyInDto : bodyList) {
            //取值是否能取到
            Object read = JvsJsonPath.read(variableMap, bodyInDto.getPath());
            if (ObjectNull.isNotNull(bodyInDto.getNecessity()) && bodyInDto.getNecessity()) {
                if (ObjectNull.isNull(read)) {
                    throw new RuleException(RuleExceptionEnum.入参校验不通过, bodyInDto.getLabel() + "必填写");
                }
            } else {
                //设置默认值
                if (ObjectNull.isNotNull(bodyInDto.getDefaultValue())) {
                    JvsJsonPath.set(variableMap, bodyInDto.getPath(), bodyInDto.getDefaultValue());
                }
            }
            //正则校验
            if (ObjectNull.isNotNull(bodyInDto.getRule())) {
                if (!Pattern.compile(bodyInDto.getRule()).matcher(read.toString()).matches()) {
                    throw new RuleException(RuleExceptionEnum.入参校验不通过, bodyInDto.getLabel() + "校验不通过");
                }
            }
            if (ObjectNull.isNotNull(bodyInDto.getChildren())) {
                recursiveVerification(variableMap, bodyInDto.getChildren());
            }
        }
    }


}

