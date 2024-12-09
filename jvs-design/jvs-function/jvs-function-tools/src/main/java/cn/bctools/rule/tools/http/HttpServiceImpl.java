package cn.bctools.rule.tools.http;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.io.resource.BytesResource;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.*;

import static org.springframework.util.MimeTypeUtils.TEXT_XML_VALUE;

/**
 * @author guojing
 * @describe 发起一个Get请求
 * 允许自定义结构，不是自动识别的结构
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "网络请求", group = RuleGroup.工具插件, test = true, returnType = ClassType.对象, testShowEnum = TestShowEnum.JSON, order = 21,
//        iconUrl = "rule-ga",
        customStructure = true, explain = "请求外部http接口，可设置header、请求头、body等参数  "

)
public class HttpServiceImpl implements BaseCustomFunctionInterface<HttpFunctionDto> {

    @Override
    public Object execute(HttpFunctionDto dto, Map<String, Object> params) {
        HashMap<String, Object> body = new HashMap<>();
        boolean isStr = dto.getBody() instanceof String;
        if (ObjectNull.isNull(dto.getUrl()) || "http://www.bctools.cn".equals(dto.getUrl())) {
            return JSONObject.parseObject(dto.getJson());
        }
        if (ObjectNull.isNotNull(dto.getBody()) && !isStr) {
            body.putAll((Map<? extends String, ?>) dto.getBody());
        }
        log.info("执行 入参为:" + JSONObject.toJSONString(dto));
        Map<String, String> headerMap = new HashMap<>(8);

        if (ObjectNull.isNull(dto.getSystemHeader()) || !dto.getSystemHeader()) {
            try {
                HttpServletRequest request = WebUtils.getRequest();
                //如果是监听,或是定时,没有请求对象不能直接操作
                if (ObjectNull.isNotNull(request)) {
                    Enumeration<String> headerNames = request.getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        if ("Authorization".equals(name)) {
                            continue;
                        }
                        String value = request.getHeader(name);
                        headerMap.put(name, value);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        if (ObjectNull.isNotNull(dto.getHeader())) {
            headerMap.putAll(dto.getHeader());
        }
        //设置默认请求的 mediaType
        String contentType = Optional.ofNullable(dto.getMediaType()).orElse(MediaType.APPLICATION_JSON_VALUE);
        HttpRequest request = new HttpRequest(UrlBuilder.of(dto.getUrl())).contentType(contentType).method(dto.getType());

        switch (dto.getType()) {
            case GET:
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(dto.getUrl());
                Set<String> set = body.keySet();
                for (String key : set) {
                    uriComponentsBuilder.queryParam(key, body.get(key));
                }
                String url = uriComponentsBuilder.uriVariables(body).build().toString();
                request.setUrl(url);
                break;
            case DELETE:
            case PUT:
            case POST:
                //根据类型判断
                switch (contentType) {
                    case MediaType.APPLICATION_OCTET_STREAM_VALUE:
                        //将内容转为二进制
                        Map<String, Object> map = new HashMap<>();
                        for (String s : body.keySet()) {
                            Object o = body.get(s);
                            if (o instanceof RuleFile) {
                                String originalName = ((RuleFile) o).getOriginalName();
                                String url1 = ((RuleFile) o).getUrl();
                                //对文件信息进行转换
                                map.put(s, new InputStreamResource(new ByteArrayInputStream(HttpUtil.downloadBytes(url1)), originalName));
                            } else if (o instanceof Map) {
                                boolean b = ((Map<?, ?>) o).containsKey("url");
                                if (b) {
                                    RuleFile copy = BeanCopyUtil.copy(o, RuleFile.class);
                                    request.body(new BytesResource((HttpUtil.downloadBytes(Optional.ofNullable(copy.getPreviewUrl()).orElse(dto.getUrl()))), copy.getOriginalName()));
                                } else {
                                    throw new BusinessException(s + "变量不是资源文件");
                                }
                            } else {
                                throw new BusinessException(s + "变量不是资源文件");
                            }
                        }
                        break;
                    case MediaType.APPLICATION_JSON_VALUE:
                        request.body(JSONObject.toJSONString(dto.getBody()));
                        break;
                    case MediaType.APPLICATION_XML_VALUE:
                        String next = body.keySet().iterator().next();
                        HashMap<String, Object> o1 = (HashMap<String, Object>) body.getOrDefault(next, new HashMap<String, Object>());
                        request.body(XmlUtil.mapToXmlStr(o1, next));
                        break;
                    case MediaType.APPLICATION_FORM_URLENCODED_VALUE:
                    case MediaType.MULTIPART_FORM_DATA_VALUE:
                        Map<String, Object> objectMap = new HashMap<>();
                        for (String s : body.keySet()) {
                            Object o = body.get(s);
                            if (o instanceof RuleFile) {
                                String originalName = ((RuleFile) o).getOriginalName();
                                String url1 = ((RuleFile) o).getUrl();
                                //对文件信息进行转换
                                objectMap.put(s, new InputStreamResource(new ByteArrayInputStream(HttpUtil.downloadBytes(url1)), originalName));
                            } else if (o instanceof Map) {
                                boolean b = ((Map<?, ?>) o).containsKey("url");
                                if (b) {
                                    RuleFile copy = BeanCopyUtil.copy(o, RuleFile.class);
                                    objectMap.put(s, new BytesResource((HttpUtil.downloadBytes(Optional.ofNullable(copy.getPreviewUrl()).orElse(dto.getUrl()))), copy.getOriginalName()));
                                } else {
                                    objectMap.put(s, o);
                                }
                            } else {
                                objectMap.put(s, o);
                            }
                        }
                        //判断 value中是否有文件流信息
                        request.form(objectMap);
                        break;
                    case MediaType.TEXT_XML_VALUE:
                        request.body(dto.getBody().toString());
                        break;
                    default:
                        request.body(dto.getBody().toString());

                }
            default:

        }
        if (ObjectNull.isNotNull(headerMap)) {
            headerMap.keySet().forEach(e -> request.header(e, String.valueOf(headerMap.get(e))));
        }
        String bodyStr = request.execute().body();
        //如果返回为 xml 则需要解析 xml
        if (MediaType.APPLICATION_XML_VALUE.equals(contentType)) {
            return XmlUtil.xmlToMap(bodyStr);
        }
        if (JSONUtil.isTypeJSON(bodyStr)) {
            if (JSONUtil.isTypeJSONArray(bodyStr)) {
                return JSONArray.parseArray(bodyStr);
            }
            if (JSONUtil.isTypeJSONObject(bodyStr)) {
                return JSONObject.parseObject(bodyStr);
            }
        }
        return bodyStr;
    }
}
