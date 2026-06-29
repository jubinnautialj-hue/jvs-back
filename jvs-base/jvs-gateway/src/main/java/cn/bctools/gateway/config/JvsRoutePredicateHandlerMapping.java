package cn.bctools.gateway.config;

import cn.bctools.common.constant.SysConstant;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping.ManagementPortType.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;

/**
 * 重写路由选择,加速选择,避免每一次都去查询
 *
 * @author zxk
 */
@Component
@Slf4j
public class JvsRoutePredicateHandlerMapping extends RoutePredicateHandlerMapping {

    private final RouteLocator routeLocator;

    private final Integer managementPort;

    @Autowired
    public JvsRoutePredicateHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator,
                                           GlobalCorsProperties globalCorsProperties, Environment environment) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        this.routeLocator = routeLocator;
        this.managementPort = environment.getProperty("management.server." + "port", Integer.class);
        setOrder(environment.getProperty(GatewayProperties.PREFIX + ".handler-mapping.order", Integer.class, 1));
        setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
    }

    private ManagementPortType getManagementPortType(Environment environment) {
        Integer serverPort = environment.getProperty("server." + "port", Integer.class);
        if (this.managementPort != null && this.managementPort < 0) {
            return DISABLED;
        }
        return ((this.managementPort == null || (serverPort == null && this.managementPort.equals(8080))
                || (this.managementPort != 0 && this.managementPort.equals(serverPort))) ? SAME : DIFFERENT);
    }

    static Cache<String, Route> cache = CacheUtil.newFIFOCache(500, 60 * 60 * 5);

    @Override
    protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
        String s = exchange.getRequest().getPath().toString();
        if (s.startsWith(SysConstant.AUTH_PATH) || s.startsWith(SysConstant.MGR_PATH)) {
            if (cache.containsKey(s)) {
                return Mono.just(cache.get(s));
            }
        }
        return routeLocator.getRoutes()
                // individually filter routes so that filterWhen error delaying is not a
                // problem
                .concatMap(route -> Mono.just(route).filterWhen(r -> {
                    // add the current route we are testing
                    exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
                    return r.getPredicate().apply(exchange);
                })
                        // instead of immediately stopping main flux due to error, log and
                        // swallow it
                        .doOnError(e -> logger.error("Error applying predicate for route: " + route.getId(), e))
                        .onErrorResume(e -> Mono.empty()))
                // .defaultIfEmpty() put a static Route not found
                // or .switchIfEmpty()
                // .switchIfEmpty(Mono.<Route>empty().log("noroute"))
                .next()
                // TODO: error handling
                .map(route -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Route matched: " + route.getId());
                    }
                    cache.put(s, route);
                    validateRoute(route, exchange);
                    return route;
                });
    }

}
