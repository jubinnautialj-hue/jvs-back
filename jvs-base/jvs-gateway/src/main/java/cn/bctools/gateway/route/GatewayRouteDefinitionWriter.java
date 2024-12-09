package cn.bctools.gateway.route;

import cn.bctools.gateway.entity.GatewayRoutePo;
import cn.bctools.gateway.mapper.GatewayRouteMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

/**
 * The type Gateway route definition writer.
 * 网关路由转发规则，默认加载的数据库表 gatewayRoute 表的数据
 *
 * @author gj
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayRouteDefinitionWriter implements RouteDefinitionRepository {

    /**
     * The Gateway route mapper.
     */
    @Resource
    GatewayRouteMapper gatewayRouteMapper;
    /**
     * 根据事件确定是否重新加载
     */
    private static Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());

    /**
     * Clear route.
     */
    public static void clearRoute() {
        routes.clear();
    }

    private Map<String, RouteDefinition> getDbRoute(List<GatewayRoutePo> gatewayRoutePos) {
        return gatewayRoutePos
                .stream()
                .map(e -> {
                    try {
                        RouteDefinition routeDefinition = new RouteDefinition();
                        routeDefinition.setId(e.getId());
                        routeDefinition.setUri(new URI(e.getUri()));
                        Map<String, String> predicateParams = new HashMap<>(1);
                        PredicateDefinition predicate = new PredicateDefinition();
                        predicate.setName("Path");
                        predicateParams.put("pattern", e.getPath());
                        predicateParams.put("_genkey_0", e.getPath());
                        predicate.setArgs(predicateParams);
                        routeDefinition.setPredicates(Collections.singletonList(predicate));
                        //数据库配置优先于自动注册
                        return routeDefinition;
                    } catch (Exception uriSyntaxException) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RouteDefinition::getId, Function.identity()));
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        if (routes.isEmpty()) {
            routes = getDbRoute(gatewayRouteMapper.selectList(Wrappers.query()));
        }
        log.info("重新加载路由信息");
        //查询数据库
        Map<String, RouteDefinition> routesSafeCopy = new LinkedHashMap<>(routes);
        return Flux.fromIterable(routesSafeCopy.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            if (ObjectUtils.isEmpty(r.getId())) {
                return Mono.error(new IllegalArgumentException("id may not be empty"));
            }
            routes.put(r.getId(), r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (routes.containsKey(id)) {
                routes.remove(id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
        });
    }

}
