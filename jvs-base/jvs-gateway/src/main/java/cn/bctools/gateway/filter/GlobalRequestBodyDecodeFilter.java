package cn.bctools.gateway.filter;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.gateway.config.JvsServerBearerTokenAuthenticationConverter;
import cn.bctools.gateway.entity.GatewayIgnoreEncode;
import cn.bctools.gateway.entity.GatewayIgnoreIp;
import cn.bctools.gateway.mapper.GatewayIgnoreEncodeMapper;
import cn.bctools.gateway.mapper.GatewayIgnoreIpMapper;
import cn.bctools.gateway.swagger.SwaggerProperties;
import cn.bctools.gateway.utils.CacheUtils;
import cn.bctools.gateway.utils.DataUtil;
import cn.bctools.gateway.utils.IpUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class GlobalRequestBodyDecodeFilter implements GlobalFilter, Ordered {

    private static final String MGR_URL = "/mgr";
    private static final String DOC_URL = "/v3/api-docs";
    private static final String OPENAPI_URL = "/openapi";
    private static final String KEY_404_PATH = "path";
    private static final String KEY_404_STATUS = "error";

    /**
     * OPEN API地址前缀
     */
    public static final String OPEN_API_URL_PREFIX = "/openapi";

    @Autowired
    GatewayIgnoreEncodeMapper ignoreEncodeMapper;
    @Autowired
    SwaggerProperties swaggerProperties;
    @Autowired
    GatewayIgnoreIpMapper ignoreIpMapper;
    static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String clientIp = IpUtils.getIp(exchange.getRequest());
        //处理swagger文件
        if (PATH_MATCHER.match(JvsServerBearerTokenAuthenticationConverter.APIDOC, path)) {
            return swaggerExchange(exchange, chain);
        }
        //如果开启了加密才做加密
        if (path.startsWith(MGR_URL)) {
            boolean present = CacheUtils.cache.get(CacheUtils.Type.ip, () -> ignoreIpMapper.selectList(Wrappers.lambdaQuery()))
                    .stream()
                    .filter(ObjectNull::isNotNull)
                    .map(e -> BeanUtil.toBean(e, GatewayIgnoreIp.class))
                    .anyMatch(e -> ((GatewayIgnoreIp) e).getIp().equals(clientIp));
            if (present) {
                return chain.filter(exchange);
            }
            List<GatewayIgnoreEncode> urls = CacheUtils.cache.get(CacheUtils.Type.encode, () -> ignoreEncodeMapper.selectList(Wrappers.query()));
            if (ObjectNull.isNotNull(urls)) {
                for (GatewayIgnoreEncode url : urls) {
                    if (PATH_MATCHER.matchStart(url.getPath(), path)) {
                        //放开不加密
                        return chain.filter(exchange);
                    }
                }
            }
            //如果开启swagger并在请求头上添加了Request-Origion:则直接不加密
            if (swaggerProperties.getEnable()) {
                if ("Knife4j".equals(exchange.getRequest().getHeaders().getFirst("Request-Origion"))) {
                    return chain.filter(exchange);
                }
            }
            // 对增/改操作，实现出参加密操作
            return operationExchange(exchange, chain);
        } else if (path.startsWith(OPENAPI_URL)) {
            //todo 如果是 Openapi 不加密或走自定义加密
            //匹配路径。判断是否具有权限
            //判断ip白名单

        }
        return chain.filter(exchange);
    }

    private Mono<Void> swaggerExchange(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) originalResponse.bufferFactory();

            @SuppressWarnings("unchecked")
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                if (getStatusCode().equals(HttpStatus.OK) && body instanceof Flux) {
                    String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                    if (StringUtils.isNotBlank(originalResponseContentType) && originalResponseContentType.contains(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
                        Flux<DataBuffer> flux = Flux.from(body);
                        return super.writeWith(flux.buffer().map(dataBuffers -> {

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            dataBuffers.forEach(i -> {
                                byte[] array = new byte[i.readableByteCount()];
                                i.read(array);
                                DataBufferUtils.release(i);
                                outputStream.write(array, 0, array.length);
                            });
                            //针对上传类型的数据,返回不加密
                            String result = outputStream.toString();
                            Map jsonObject = JSONObject.parseObject(result, Map.class);
                            // 从扩展配置中获取是否是OpenAPI，若是OpenAPI则添加前缀
                            JSONObject extensions = Optional.ofNullable(jsonObject.get(Get.name(OpenAPI::getExtensions)))
                                    .map(obj -> (JSONObject)obj)
                                    .orElseGet(JSONObject::new);
                            String basePath = path.replaceAll(DOC_URL, "");
                            if (extensions.containsKey(SysConstant.OPEN_API_MARK)) {
                                basePath = OPEN_API_URL_PREFIX + basePath;
                            }
                            jsonObject.put("basePath", basePath);
                            return bufferFactory.wrap(JSONUtil.toJsonStr(jsonObject, JSONConfig.create().setIgnoreNullValue(false)).getBytes());
                        }));
                    }
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    private Mono<Void> operationExchange(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) originalResponse.bufferFactory();

            @SuppressWarnings("unchecked")
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                if (getStatusCode().equals(HttpStatus.OK) && body instanceof Flux) {
                    String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                    if (StringUtils.isNotBlank(originalResponseContentType) && originalResponseContentType.contains(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
                        Flux<DataBuffer> flux = Flux.from(body);
                        //fix https://github.com/spring-cloud/spring-cloud-gateway/issues/3108 类似问题，但使用了不同的写法处理
                        Flux<DataBuffer> dataBufferFlux = flux
                                .collect(ByteArrayOutputStream::new, (outputStream, dataBuffer) -> {
                                    byte[] array = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(array);
                                    DataBufferUtils.release(dataBuffer);
                                    outputStream.write(array, 0, array.length);
                                }).map(byteArrayOutputStream -> {
                                    try {
                                        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                                    } catch (Exception e) {
                                        // 处理异常
                                        log.error("byteArrayOutputStream异常" + request.getURI().getPath() + e.getMessage());
                                        return "";
                                    } finally {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (IOException e) {
                                            log.error("byteArrayOutputStream close异常" + request.getURI().getPath() + e.getMessage());
                                        }
                                    }
                                })
                                .flux()
                                .flatMap(result -> {
                                    R r;
                                    JSONObject jsonObject = JSONObject.parseObject(result, JSONObject.class);
                                    if (HttpStatus.NOT_FOUND.getReasonPhrase().equals(jsonObject.getString(KEY_404_STATUS))) {
                                        r = R.failed("404 Not Found : " + jsonObject.getString(KEY_404_PATH));
                                    } else {
                                        r = jsonObject.toJavaObject(R.class);
                                    }
                                    if (r.is()) {
                                        if (ObjectUtil.isNotNull(r.getData())) {
                                            byte[] bytes = JSONObject.toJSONString(r.getData(), JSONWriter.Feature.LargeObject).getBytes(Charset.defaultCharset());
                                            String s = DataUtil.encodeBody(bytes);
                                            getHeaders().add("decode", String.valueOf(true));
                                            r.setData(s);
                                        } else {
                                            r.setData(null);
                                        }
                                        return Flux.just(bufferFactory.wrap(JSONObject.toJSONString(r, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.LargeObject).getBytes()));
                                    } else {
                                        log.error("业务异常" + request.getURI().getPath() + r.getMsg());
                                        throw new BusinessException(r.getMsg(), r.getCode());
                                    }
                                });
                        return super.writeWith(dataBufferFlux);
                    }
                }
                return super.writeWith(body);
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body)
                        .flatMapSequential(p -> p));
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

}
