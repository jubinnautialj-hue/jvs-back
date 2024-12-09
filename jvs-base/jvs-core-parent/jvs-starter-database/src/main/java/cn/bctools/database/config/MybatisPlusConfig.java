package cn.bctools.database.config;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.database.getter.DefaultTableFieldGetter;
import cn.bctools.database.interceptor.other.CustomOthersInterceptor;
import cn.bctools.database.interceptor.tenant.JvsTenantHandler;
import cn.bctools.database.property.SqlProperties;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.ImadcnIdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.TableNameParser;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.imadcn.framework.idworker.common.SerializeStrategy;
import com.imadcn.framework.idworker.config.ApplicationConfiguration;
import com.imadcn.framework.idworker.config.ZookeeperConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author My_gj
 */
@Slf4j
@Configuration
@Lazy(false)
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@MapperScan(basePackages = {"cn.bctools.**.mapper"})
public class MybatisPlusConfig {

    @Value("${tenant.enable:true}")
    Boolean tenant = true;

    @Value("${mybatis-plus.zookeeper.serverLists:}")
    String zkServerLists;


    /**
     * 适配 zk id生成器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator identifierGenerator() {
        if (ObjectNull.isNotNull(zkServerLists)) {
            // 创建idworker实例
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            applicationConfiguration.setSerialize(SerializeStrategy.SERIALIZE_JSON_FASTJSON);
            ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration();
            zookeeperConfiguration.setServerLists(zkServerLists);
            ImadcnIdentifierGenerator imadcnIdentifierGenerator = new ImadcnIdentifierGenerator(zookeeperConfiguration, applicationConfiguration);
            IdWorker.setIdentifierGenerator(imadcnIdentifierGenerator);
            return imadcnIdentifierGenerator;
        } else {
            DefaultIdentifierGenerator defaultIdentifierGenerator = new DefaultIdentifierGenerator();
            IdWorker.setIdentifierGenerator(defaultIdentifierGenerator);
            return defaultIdentifierGenerator;
        }
    }


    @Bean
    @ConditionalOnMissingBean
    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor() {

            @Override
            protected String changeTable(String sql) {
                TableNameParser parser = new TableNameParser(sql);
                List<TableNameParser.SqlToken> names = new ArrayList<>();
                parser.accept(names::add);
                StringBuilder builder = new StringBuilder();
                int last = 0;
                for (TableNameParser.SqlToken name : names) {
                    int start = name.getStart();
                    if (start != last) {
                        builder.append(sql, last, start);
                        String value = name.getValue();
                        if (DefaultTableFieldGetter.getTABLELIST().contains(value)) {
                            builder.append(dynamicTableName(sql, value));
                        } else {
                            builder.append(value);
                        }
                    }
                    last = name.getEnd();
                }
                if (last != sql.length()) {
                    builder.append(sql.substring(last));
                }
                return builder.toString();
            }

            private String dynamicTableName(String sql, String tableName) {
                String tenantId = TenantContextHolder.getTenantId();
                if (StrUtil.isBlank(tenantId)) {
                    return tableName;
                }
                SqlProperties sqlProperties = SpringContextUtil.getBean(SqlProperties.class);
                if (!sqlProperties.isDynamicTenantDatabase()) {
                    return tableName;
                }
                return tableName;
            }
        };
        return dynamicTableNameInnerInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(JvsTenantHandler jvsTenantHandler) {
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor();
        tenantLineInnerInterceptor.setTenantLineHandler(jvsTenantHandler);
        return tenantLineInnerInterceptor;
    }

    /**
     * 预留插件扩展
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomOthersInterceptor otherInterceptor(TenantLineInnerInterceptor tenantLineInnerInterceptor, DynamicTableNameInnerInterceptor table) {
        return new CustomOthersInterceptor() {
            @Override
            public List<InnerInterceptor> gets() {
                return tenant ? init(tenantLineInnerInterceptor) : new ArrayList<>();
            }
        };
    }

    /**
     * 租户管理
     */
    @Bean
    @ConditionalOnMissingBean
    public JvsTenantHandler tenantHandler() {
        return new JvsTenantHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(CustomOthersInterceptor customOthersInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //添加自定义插件扩展预留
        List<InnerInterceptor> gets = customOthersInterceptor.gets();
        if (ObjectUtil.isNotEmpty(gets)) {
            for (InnerInterceptor get : gets) {
                interceptor.addInnerInterceptor(get);
            }
        }
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 不指定数据源类型，可兼容多数据源
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

}
