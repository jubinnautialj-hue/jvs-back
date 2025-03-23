package cn.bctools.screen.filter;

import cn.bctools.web.config.JvsWebMvcConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * [Description]: 拦截器
 *
 * @author : admin
 * @date : 2020-11-19 19:44
 */
@Configuration
@RequiredArgsConstructor
public class DataAuthConfigurer extends JvsWebMvcConfigurer {

    private final CommonInterceptor commonInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(commonInterceptor);
    }


}

