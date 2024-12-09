package cn.bctools.database.init;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.annotation.JvsCacheLinkClear;
import cn.bctools.database.entity.DatabaseInfo;
import cn.bctools.database.entity.TableInfo;
import cn.bctools.database.getter.DefaultDataSourceGetter;
import cn.bctools.database.getter.DefaultTableFieldGetter;
import cn.bctools.database.getter.IDataSourceGetter;
import cn.bctools.database.getter.ITableFieldGetter;
import cn.bctools.database.mapper.TableInfoMapper;
import cn.bctools.database.util.DatabaseUtils;
import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.PageUtil;
import com.alibaba.excel.util.ClassUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源信息初始化
 * <p>
 * 会尝试调用默认实现类:
 * {@link DefaultTableFieldGetter}
 * {@link DefaultDataSourceGetter}
 *
 * @Author: GuoZi
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Lazy(false)
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true", matchIfMissing = true)
public class DataSourceInit extends SpringContextUtil {

    @Resource
    private TableInfoMapper tableInfoMapper;
    /**
     * 启动时扫描二级缓存的关联清除问题
     */
    public static Map<String, Set<String>> MYBATIS_PLUS_CACHE_LINK = new HashMap<>();
    @Getter
    private static Boolean isTenantId = false;
    private static final String TENANTID = "tenant_id";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        ClassScanner.scanAllPackageByAnnotation("cn.bctools", JvsCacheLinkClear.class).forEach(e -> {
            MYBATIS_PLUS_CACHE_LINK.put(e.getName(), Arrays.stream(e.getAnnotation(JvsCacheLinkClear.class).value()).map(s -> s.getName()).collect(Collectors.toSet()));
        });

        log.info("[mysql-data] 加载单个数据源");
        //初始化时不需要数据权限和租户权限，所以不用清除
        ITableFieldGetter tableFieldGetter = SpringContextUtil.getBean(ITableFieldGetter.class);
        IDataSourceGetter dataSourceGetter = SpringContextUtil.getBean(IDataSourceGetter.class);
        boolean defaultField = tableFieldGetter instanceof DefaultTableFieldGetter;
        boolean defaultSource = dataSourceGetter instanceof DefaultDataSourceGetter;
        if (!defaultField && !defaultSource) {
            return;
        }
        log.info("数据源信息初始化 >>>> ");
        DataSource dataSource = SpringContextUtil.getBean(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            DatabaseInfo info = DatabaseUtils.parseUrl(url);
            if (defaultField) {
                List<TableInfo> tableInfos = tableInfoMapper.tableInfo(info.getDatabaseName());
                for (TableInfo tableInfo : tableInfos) {
                    tableInfo.setIp(info.getIp());
                    tableInfo.setPort(info.getPort());
                    tableInfo.setDatabaseName(info.getDatabaseName());
                }
                isTenantId = tableInfos.stream().filter(e -> TENANTID.equals(e.getFieldName())).findAny().isPresent();
                ((DefaultTableFieldGetter) tableFieldGetter).saveCache(tableInfos);
                log.info(">>>> 数据表加载完毕, 共{}个表字段", tableInfos.size());
            }
            if (defaultSource) {
                log.info(">>>> 数据源加载完毕, 共1个数据源");
                ((DefaultDataSourceGetter) dataSourceGetter).init(info);
            }
        } catch (Exception e) {
            log.error(">>>> 数据源信息初始化异常, 部分功能可能无法正常使用,退出启动", e);
            System.exit(-1);
        }
    }

}
