package cn.bctools.database.config;

import cn.bctools.database.getter.DefaultDataSourceGetter;
import cn.bctools.database.getter.DefaultTableFieldGetter;
import cn.bctools.database.getter.IDataSourceGetter;
import cn.bctools.database.getter.ITableFieldGetter;
import cn.bctools.database.init.DataSourceInit;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.Properties;

/**
 * 数据源信息获取配置
 * <p>
 * 用于各个MyBatis拦截器
 *
 * @Author: GuoZi
 */
@Slf4j
public class DatabaseConfig {

    /**
     * 默认的表字段获取类
     */
    @Bean
    @ConditionalOnMissingBean
    public ITableFieldGetter tableFieldGetter() {
        log.info("[mysql-data] 使用默认的表字段获取类: {}", DefaultTableFieldGetter.class.getName());
        return new DefaultTableFieldGetter();
    }

    /**
     * 默认的数据源信息获取类
     * <p>
     * 该默认实现类优先级均低于 jvs-starter-dynamic模块
     * 详情见{@link cn.bctools.dynamic.config}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("cn.bctools.dynamic.config.DynamicDataConfig")
    public IDataSourceGetter dataSourceGetter() {
        log.info("[mysql-data] 使用默认的数据源信息获取类: {}", DefaultDataSourceGetter.class.getName());
        return new DefaultDataSourceGetter();
    }

    /**
     * 默认的数据源信息获取类
     * <p>
     * 该默认实现类优先级均低于 jvs-starter-dynamic模块
     * 详情见{@link cn.bctools.dynamic.config}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("cn.bctools.dynamic.config.DynamicDataConfig")
    @ConditionalOnProperty(value = "tenant.enable", havingValue = "true", matchIfMissing = true)
    @Lazy(false)
    public DataSourceInit dataSourceInit() {
        log.info("[mysql-data] 加载单数据源初始化类: {}", DataSourceInit.class.getName());
        return new DataSourceInit();
    }


    @Bean
    @ConditionalOnMissingBean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.put("MySQL", DbType.MYSQL.getDb());
        properties.put("KingbaseES", DbType.KINGBASE_ES.getDb());
        properties.put("PostgreSQL", DbType.POSTGRE_SQL.getDb());
        properties.put("DM DBMS", DbType.DM.getDb());
        provider.setProperties(properties);
        return provider;
    }

//   需要升级mybatis plus 3.5.3.2
//    @Bean
//    public Cache cache() {
//        Cache<String, byte[]> build = Caffeine.newBuilder()
//                // 设置最后一次写入或访问后经过固定时间过期
//                .expireAfterWrite(60, TimeUnit.SECONDS)
//                // 初始的缓存空间大小
//                .initialCapacity(100)
//                // 缓存的最大条数
//                .maximumSize(1000)
//                .build();
//        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(build));
//        return build;
//    }

}
