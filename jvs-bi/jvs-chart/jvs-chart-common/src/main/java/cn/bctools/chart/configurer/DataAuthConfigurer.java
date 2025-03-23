package cn.bctools.chart.configurer;

import cn.bctools.web.config.JvsWebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * [Description]: 拦截器
 *
 * @author : admin
 * @date : 2020-11-19 19:44
 */
@Configuration
public class DataAuthConfigurer extends JvsWebMvcConfigurer {

    @Lazy
    @Autowired
    CommonInterceptor commonInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(commonInterceptor);
    }


}

