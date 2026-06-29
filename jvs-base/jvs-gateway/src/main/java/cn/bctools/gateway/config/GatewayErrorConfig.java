package cn.bctools.gateway.config;

import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.ErrorMessageSendDingConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.dingding.DingRes;
import cn.bctools.dingding.DingSendUtils;
import cn.bctools.gateway.entity.GatewayCodePo;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.mapper.ConfigMapper;
import cn.bctools.gateway.mapper.GatewayCodeMapper;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @author jvs 统一异常处理,
 */
@Slf4j
@Configuration
@Primary
@Order(Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
public class GatewayErrorConfig implements ErrorWebExceptionHandler {
    /**
     * The System error.
     */
    static String[] systemError = new String[]{"Connection", "GATEWAY_TIMEOUT", "SERVICE_UNAVAILABLE", "Error querying database", ".mapper.", ".ibatis.", "[404Not Found]"};
    /**
     * The Msg cache.
     */
    static TimedCache msgCache = new TimedCache(60 * 1000);
    /**
     * The Gateway code mapper.
     */
    GatewayCodeMapper gatewayCodeMapper;
    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;
    /**
     * The Ding send utils.
     */
    DingSendUtils dingSendUtils;
    /**
     * The Config mapper.
     */
    ConfigMapper configMapper;
    NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getURI().toASCIIString();
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // header set
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
        }
        String message = ex.getMessage();
        R r = R.failed(Arrays.stream(systemError).filter(message::contains).findFirst().map(e -> "系统错误,请联系管理员." + IdGenerator.getIdStr(36)).orElse(message));
        if (ex instanceof BusinessException) {
            //转义
            try {
                GatewayCodePo gatewayCodePo = gatewayCodeMapper.selectOne(Wrappers.query(new GatewayCodePo().setMsg(message)));
                if (ObjectUtil.isNotEmpty(gatewayCodePo)) {
                    //如果有就替换，如果 没有就还是使用默认的
                    r.setCode(gatewayCodePo.getCode());
                } else if (((BusinessException) ex).getCode() != 1) {
                    r.setCode(((BusinessException) ex).getCode());
                }
            } catch (Exception e) {
                log.error("数据库查询错误", e);
            }
        }

        String body = JSONObject.toJSONString(r, JSONWriter.Feature.LargeObject, JSONWriter.Feature.NotWriteDefaultValue);
        //转义
        String s = StackTraceElementUtils.logThrowableToString(ex);
        String env = SpringContextUtil.getEnv();
        String msg = env + nacosDiscoveryProperties.getIp() + " _ " + nacosDiscoveryProperties.getPort() + "网关错误拦截:请求地址:" + path + "返回信息:" + body + ",异常信息为:" + s;
        log.error(msg);
        //发送消息
        try {
            //缓存缓存 2 小时的错误数据，如果存在错误就不发了
            boolean enableCache = (boolean) msgCache.get("enablecache", this::canSendDing);
            if (enableCache && !msgCache.containsKey(r.getMsg())) {
                msgCache.put(r.getMsg(), r.getMsg());
                DingRes dingRes = dingSendUtils.sendMessage(msg);
                if (ObjectNull.isNull(dingRes)) {
                    log.error("未配置钉钉异常信息通知");
                }
            }
        } catch (Exception e) {

        }
        return response
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    return bufferFactory.wrap(body.getBytes());
                }));
    }

    /**
     * 是否发送到钉钉
     *
     * @return true-发送，false-不发送
     */
    private boolean canSendDing() {
        ConfigsTypeEnum typeEnum = ConfigsTypeEnum.ERROR_MESSAGE_SEND_DING;
        SysConfigs config = configMapper.selectOne(Wrappers.<SysConfigs>lambdaQuery()
                .eq(SysConfigs::getType, typeEnum));
        if (ObjectNull.isNull()) {
            return false;
        }
        return BeanCopyUtil.copy(config.getContent(), ErrorMessageSendDingConfig.class).getEnable();
    }
}
