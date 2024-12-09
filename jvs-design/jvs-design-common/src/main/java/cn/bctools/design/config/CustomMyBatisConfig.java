package cn.bctools.design.config;

import cn.bctools.database.getter.IDataSourceGetter;
import cn.bctools.database.getter.ITableFieldGetter;
import cn.bctools.database.interceptor.other.CustomOthersInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hoal
 */
@Configuration
public class CustomMyBatisConfig {


    @Bean
    @ConditionalOnMissingBean
    public JvsAppVersionInnerInterceptor jvsAppVersionInnerInterceptor(ITableFieldGetter tableFieldGetter, IDataSourceGetter dataSourceGetter) {
        JvsAppVersionInnerInterceptor jvsAppVersionInnerInterceptor = new JvsAppVersionInnerInterceptor();
        jvsAppVersionInnerInterceptor.setTableFieldGetter(tableFieldGetter);
        jvsAppVersionInnerInterceptor.setDataSourceGetter(dataSourceGetter);
        return jvsAppVersionInnerInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomOthersInterceptor otherInterceptor(TenantLineInnerInterceptor tenantLineInnerInterceptor, JvsAppVersionInnerInterceptor jvsAppVersionInnerInterceptor) {
        return () -> {
            List<InnerInterceptor> objects = new ArrayList<>();
            objects.add(tenantLineInnerInterceptor);
            objects.add(jvsAppVersionInnerInterceptor);
            return objects;
        };
    }
}
