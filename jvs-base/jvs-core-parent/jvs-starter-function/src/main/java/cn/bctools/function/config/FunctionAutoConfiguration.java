package cn.bctools.function.config;

import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.function.component.ExpressionComponent;
import cn.bctools.function.controller.ExpressionController;
import cn.bctools.function.handler.ExpressionAfterHandler;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.handler.impl.BaseFunctionImpl;
import cn.bctools.function.handler.impl.SysParamImpl;
import cn.bctools.function.handler.impl.VariableParamImpl;
import cn.bctools.function.mapper.SysFunctionMapper;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.function.service.SysFunctionService;
import cn.bctools.redis.JvsMessageListenerAdapter;
import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;

/**
 * aws 自动配置类
 *
 * @author Administrator
 */
@Slf4j
@AllArgsConstructor
@EnableConfigurationProperties
public class FunctionAutoConfiguration {

    /**
     * 基础函数
     */
    @Bean
    @ConditionalOnMissingBean(BaseFunctionImpl.class)
    public BaseFunctionImpl baseFunctionImpl(SysFunctionMapper sysFunctionMapper) {
        return new BaseFunctionImpl(sysFunctionMapper);
    }

    /**
     * 当前用户函数
     */
    @Bean
    @ConditionalOnMissingBean(SysParamImpl.class)
    public SysParamImpl sysParamImpl(UserExtensionServiceApi extensionServiceApi) {
        return new SysParamImpl(extensionServiceApi);
    }

    /**
     * 函数执行完后的操作，对参数做结构化处理
     */
    @Bean
    @ConditionalOnMissingBean(ExpressionAfterHandler.class)
    public ExpressionAfterHandler expressionAfterHandler() {
        //默认不处理
        return (designId, init, body) -> body.getParams();
    }

    /**
     * 环境变量的处理实现场景
     */
    @Bean("jvsEnvironment")
    @ConditionalOnMissingBean(VariableParamImpl.class)
    public VariableParamImpl variableParamImpl(EnvironmentVariableApi variableApi) {
        return new VariableParamImpl(variableApi);
    }

    /**
     * 所有的函数操作入口 controller
     */
    @Bean
    @ConditionalOnMissingBean
    public ExpressionController commonFunctionController(ExpressionComponent expressionComponent, ExpressionHandler expressionHandler, FunctionBusinessService functionBusinessMapper,
                                                         ExpressionAfterHandler expressionAfterHandler, SysFunctionService sysFunctionService, RedisUtils redisUtils) {
        return new ExpressionController(expressionHandler, expressionComponent, functionBusinessMapper, expressionAfterHandler, sysFunctionService, redisUtils);
    }

    /**
     * 函数执行组件
     */
    @Bean
    @ConditionalOnMissingBean
    public ExpressionComponent expressionComponent(ExpressionHandler expressionHandler, FunctionBusinessService functionBusinessMapper) {
        return new ExpressionComponent(expressionHandler, functionBusinessMapper);
    }

    @Bean("functionCleanCache")
    JvsMessageListenerAdapter functionCleanCache(BaseFunctionImpl baseFunction) {
        return new JvsMessageListenerAdapter() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                baseFunction.clean();
            }
        };
    }
}
