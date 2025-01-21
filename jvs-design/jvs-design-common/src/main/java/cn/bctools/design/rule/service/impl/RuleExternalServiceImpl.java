package cn.bctools.design.rule.service.impl;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.design.rule.entity.ParameterMap;
import cn.bctools.design.rule.entity.RuleExternalPo;
import cn.bctools.design.rule.mapper.RuleExternalMapper;
import cn.bctools.design.util.WebServiceUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.function.RuleExternalHandler;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.rule.dto.RuleFunctionDto;
import cn.bctools.rule.dto.RuleFunctionDtoParameter;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.web.utils.HttpRequestUtils;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.service.ParameterType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Rule external service.
 *
 * @author jvs
 */
@AllArgsConstructor
@Slf4j
@Service
public class RuleExternalServiceImpl extends ServiceImpl<RuleExternalMapper, RuleExternalPo> implements RuleExternalService {
    static final String webservice = "webservice";

    /**
     * The External handler.
     */
    Map<String, RuleExternalHandler> externalHandler;

    /**
     * The Rest template.
     */
    RestTemplate restTemplate;

    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RULE_EXTERNAL = "RULE_EXTERNAL_CACHE:";

    @Override
    public Object execute(String functionName, String group, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction) {
        RuleExternalPo externalPo = getOne(Wrappers.query(new RuleExternalPo().setRuleGroup(group).setName(functionName).setStatus(true)));
        //判断是否是 webservice
        if (webservice.equals(externalPo.getType())) {
            return executeWebService(externalPo, variableMap, expressionExecFunction, false);
        } else {
            return execute(externalPo, variableMap, expressionExecFunction, false);
        }
    }

    @Override
    public Object executeWebService(RuleExternalPo externalPo, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction, boolean test) {
        if (ObjectNull.isNull(externalPo)) {
            throw new BusinessException("没有找到扩展方法");
        }
        String url = externalPo.getUrl();
        //是否是测试执行
        if (!test) {
            //如果启用了 mock数据则返回 mock
            if (ObjectNull.isNotNull(externalPo.getEnableMock()) && externalPo.getEnableMock()) {
                return externalPo.getMock();
            }
        }
        List<RuleFunctionDtoParameter> fieldList = externalPo.getFieldLists();
        Map<String, Object> body = new HashMap<>(fieldList.size());
        Map<String, String> header = new HashMap<>(fieldList.size());

        List<Object> cacheValue = new ArrayList<>();

        for (RuleFunctionDtoParameter dto : fieldList) {
            Object apply = expressionExecFunction.apply(dto.getKey());
            if (ObjectNull.isNull(apply)) {
                apply = variableMap.get(dto.getKey());
            }
            if (dto.getCache()) {
                cacheValue.add(apply);
            }
            if (Optional.ofNullable(dto.getParamType()).filter(e -> StrUtil.equals(ParameterType.HEADER.name(), e.name())).isPresent()) {
                header.put(dto.getKey(), apply.toString());
            } else {
                body.put(dto.getKey(), apply);
            }
            if (dto.isNecessity() && !body.containsKey(dto.getKey())) {
                throw new BusinessException("参数没有找到", dto.getKey());
            } else {
                if (ObjectNull.isNull(body.get(dto.getKey()))) {
                    body.remove(dto.getKey());
                }
            }
        }
        //拼接对象
        String key = "";
        if (!test) {
            if (Optional.ofNullable(externalPo.getCacheTime()).filter(e -> e > 0).isPresent()) {
                //使用参数的 md5进行匹配
                List<String> collect = cacheValue.stream().map(StrUtil::toString).collect(Collectors.toList());
                String s = MD5.create().digestHex16(url) +
                        MD5.create().digestHex16(JSONObject.toJSONString(collect));
                key = SysConstant.redisKey(RULE_EXTERNAL + externalPo.getName(), s);
                //判断是否走了缓存 并匹配 地址，请求头，参数三个值，如果有相同 则进行判断缓存的值
                if (redisUtils.hasKey(key)) {
                    return redisUtils.get(key);
                }
            }
        }

        Object externalBody = WebServiceUtils.execute(externalPo.getUrl(), externalPo.getServerUrl(), externalPo.getFunctionName(), body, header);
        if (!test) {
            //判断即是否启用了缓存
            if (Optional.ofNullable(externalPo.getCacheTime()).filter(e -> e > 0).isPresent()) {
                redisUtils.set(key, externalBody, externalPo.getCacheTime());
            }
        }
        if (ObjectNull.isNotNull(externalPo.getDeleteCaches())) {
            externalPo.getDeleteCaches().forEach(name -> {
                Set<String> keys = redisUtils.keys(SysConstant.redisKey(RULE_EXTERNAL + name, "*"));
                Long delete = redisTemplate.delete(keys);
            });
        }
        return externalBody;
    }

    @Override
    public Object execute(RuleExternalPo externalPo, Map<String, Object> variableMap, Function<String, Object> expressionExecFunction, Boolean test) {
        if (ObjectNull.isNull(externalPo)) {
            throw new BusinessException("没有找到扩展方法");
        }
        String url = externalPo.getUrl();
        //是否是测试执行
        if (!test) {
            //如果启用了 mock数据则返回 mock
            if (ObjectNull.isNotNull(externalPo.getEnableMock()) && externalPo.getEnableMock()) {
                return externalPo.getResponse();
            }
        }

        List<RuleFunctionDtoParameter> fieldList = externalPo.getFieldLists();

        Map<String, Object> body = new HashMap<>(fieldList.size());
        Map<String, Object> uriMap = new HashMap<>(fieldList.size());
        HttpHeaders headerMap = new HttpHeaders();

        List<Object> cacheValue = new ArrayList<>();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
        for (RuleFunctionDtoParameter dto : fieldList) {
            //如果是测试，显示测试的值
            Object apply = test ? dto.getTestValue() : expressionExecFunction.apply(dto.getKey());
            if (ObjectNull.isNull(apply)) {
                apply = variableMap.get(dto.getKey());
            }
            if (dto.getCache()) {
                cacheValue.add(apply);
            }
            if (ParameterType.QUERY.name().equals(dto.getParamType().name())) {
                uriComponentsBuilder.queryParam(dto.getKey(), apply);
                continue;
            }
            if (ParameterType.PATH.name().equals(dto.getParamType().name())) {
                uriMap.put(dto.getKey(), apply.toString());
                continue;
            }
            if (ParameterType.HEADER.name().equals(dto.getParamType().name())) {
                headerMap.put(dto.getKey(), Collections.singletonList(apply.toString()));
                continue;
            }
            if (ObjectNull.isNotNull(dto.getInputType()) && dto.getInputType().equals(InputType.onOff)) {
                body.put(dto.getKey(), Boolean.valueOf(apply.toString()));
            } else {
                //如果公式有值，使用公式，公式没值，使用填写的值
                if (ObjectNull.isNotNull(dto.getInputType())) {
                    switch (dto.getInputType()) {
                        case number:
                            body.put(dto.getKey(), Convert.toNumber(apply.toString().trim()));
                            break;
                        default:
                            body.put(dto.getKey(), apply);
                    }
                } else {
                    body.put(dto.getKey(), apply);
                }
            }

            if (dto.isNecessity() && !body.containsKey(dto.getKey())) {
                throw new BusinessException("参数没有找到", dto.getKey());
            } else {
                if (ObjectNull.isNull(body.get(dto.getKey()))) {
                    body.remove(dto.getKey());
                }
            }
        }
        url = uriComponentsBuilder.uriVariables(uriMap).build().toString();
        //拼接对象
        String key = "";
        if (!test) {
            if (ObjectNull.isNotNull(externalPo.getCacheKey())) {
                List<String> collect = cacheValue.stream().map(StrUtil::toString).collect(Collectors.toList());
                //使用参数的 md5进行匹配
                String s = MD5.create().digestHex16(url) +
                        MD5.create().digestHex16(JSONObject.toJSONString(collect));
                key = SysConstant.redisKey(RULE_EXTERNAL + externalPo.getName(), s);
                //判断是否走了缓存 并匹配 地址，请求头，参数三个值，如果有相同 则进行判断缓存的值
                if (redisUtils.hasKey(key)) {
                    return redisUtils.get(key);
                }
            }
        }
        if (ObjectNull.isNull(body) && ObjectNull.isNotNull(externalPo.getBody())) {
            body = JSONObject.parseObject(externalPo.getBody());
        }
        //获取返回结果
        Object externalBody = getExternalBody(variableMap, body, headerMap, externalPo, url);
        //校验返回数据结果是否符合校验规则
        if (ObjectNull.isNotNull(externalPo.getResponseList())) {
            String error = externalPo.getResponseList()
                    .stream()
                    .filter(e -> ObjectNull.isNotNull(e.getNecessity()))
                    .filter(ParameterMap::getNecessity)
                    .map(e -> {
                        try {
                            Object read = JvsJsonPath.read(externalBody, e.getPath());
                            if (ObjectNull.isNotNull(e.getCondition())) {
                                if (!e.getCondition().equals(read)) {
                                    return e.getLabel() + "校验失败";
                                }
                            }
                        } catch (Exception ex) {
                            return e.getLabel() + "不存在";
                        }
                        return null;
                    }).filter(ObjectNull::isNotNull).collect(Collectors.joining(","));
            if (ObjectNull.isNotNull(error)) {
                throw new BusinessException("调用错误" + error + ",详细信息:" + externalBody.toString());
            }
        }
        if (!test) {
            //判断即是否启用了缓存
            if (Optional.ofNullable(externalPo.getCacheTime()).filter(e -> e > 0).isPresent()) {
                redisUtils.set(key, externalBody, externalPo.getCacheTime() * 60);
            }
        }
        if (ObjectNull.isNotNull(externalPo.getDeleteCaches())) {
            externalPo.getDeleteCaches().forEach(name -> {
                Set<String> keys = redisUtils.keys(SysConstant.redisKey(RULE_EXTERNAL + name, "*"));
                Long delete = redisTemplate.delete(keys);
            });
        }
        return externalBody;
    }

    private Object getExternalBody(Map<String, Object> variableMap, Map<String, Object> body, HttpHeaders headerMap, RuleExternalPo externalPo, String url) {
        String ruleGroup = externalPo.getRuleGroup();
        RuleGroup ruleGroup1 = null;
        try {
            ruleGroup1 = RuleGroup.valueOf(ruleGroup);
        } catch (Exception e) {
            //不处理
        }
        if (ObjectNull.isNotNull(ruleGroup1)) {
            try {
                Object options = variableMap.get("option");
                RuleExternalHandler handler = externalHandler.get(ruleGroup1.name());
                if (ObjectNull.isNotNull(options)) {
                    String decodedPassword = PasswordUtil.decodedPassword(options.toString(), SpringContextUtil.getKey());
                    //其它三方数据解析的时候处理方式
                    Class parameterClass = SystemInit.getParameterClass(handler.getClass());
                    options = JSONObject.parseObject(decodedPassword, parameterClass);
                }
                if (ObjectNull.isNotNull(externalPo.getHandler())) {
                    return handler.handler(url, externalPo.getMethod(), body, options, headerMap);
                }
                //无法区分是否是外部请求
                switch (externalPo.getMethod()) {
                    case GET:
                        //将参数瓶装为url参数值
                        return HttpRequestUtils.getJson(url, String.class, false, headerMap, false);
                    default:
                        return HttpRequestUtils.postJson(url, body, String.class, false, headerMap, false);
                }
            } catch (RuntimeException e) {
                log.error("请求处理异常", e);
                throw new BusinessException("网络请求异常", e.getMessage());
            } catch (Exception e) {
                log.error("请求处理异常", e);
                throw new BusinessException("节点处理错误", e.getMessage());
            }
        }

        try {
            return HttpRequestUtils.execute(url, body, String.class, false, HttpMethod.valueOf(externalPo.getMethod().name()), headerMap, false, e -> {
                //todo 转换错误码
                String responseBodyAsString = e.getResponseBodyAsString();
                throw new BusinessException("服务异常" + responseBodyAsString);
            });
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No instance available")) {
                throw new BusinessException("服务异常", externalPo.getName());
            }
            throw e;
        }

    }

    @Override
    public void checkName(String functionName, String group) {
        RuleExternalPo one = getOne(Wrappers.query(new RuleExternalPo().setStatus(true).setName(functionName).setRuleGroup(group)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("节点不支持执行请检查节点是否可用或已弃用", functionName);
        }

    }

    @Override
    public void register(List<RuleFunctionDto> obj, String applicationName) {
        for (RuleFunctionDto dto : obj) {
            dto.getParameters().forEach(e -> e.setOptions(null));
            //每一条更新数据结构,可能存在更新
            RuleExternalPo one = getOne(Wrappers.query(new RuleExternalPo().setName(dto.getFunctionName())));
            if (ObjectNull.isNotNull(one)) {
                one.setExplainInfo(dto.getExplain()).setName(dto.getFunctionName()).setStatus(true).setIcon(dto.getIcon()).setRuleGroup(dto.getGroup()).setUrl(dto.getFunctionId()).setMethod(dto.getMethod()).setFieldList(dto.getParameters().stream().map(BeanCopyUtil::beanToMap).collect(Collectors.toList()));
                updateById(one);
            } else {
                save(new RuleExternalPo().setUrl(dto.getFunctionId()).setName(dto.getFunctionName()).setStatus(true).setIcon(dto.getIcon()).setRuleGroup(dto.getGroup()).setMethod(dto.getMethod()).setFieldList(dto.getParameters().stream().map(BeanCopyUtil::beanToMap).collect(Collectors.toList())));
            }
        }

    }

}
