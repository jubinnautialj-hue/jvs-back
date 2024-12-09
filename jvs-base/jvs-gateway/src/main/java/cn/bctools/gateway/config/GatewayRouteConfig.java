package cn.bctools.gateway.config;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.gateway.route.GatewayRouteDefinitionWriter;
import cn.bctools.redis.JvsMessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author xh
 */
@Order(100)
@Component
public class GatewayRouteConfig extends SpringContextUtil implements ApplicationContextAware {


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        applicationContext.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 刷新路由
     *
     * @return
     */
    @Bean
    JvsMessageListenerAdapter gatewayRoute() {
        return new JvsMessageListenerAdapter() {
            @Override
            public void onMessage(String s) {
                GatewayRouteDefinitionWriter.clearRoute();
            }
        };
    }

}
