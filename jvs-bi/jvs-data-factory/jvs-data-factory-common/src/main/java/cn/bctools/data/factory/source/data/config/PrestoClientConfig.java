package cn.bctools.data.factory.source.data.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.dto.PrestoConnectDto;
import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * mysql客户端获取
 *
 * @author admin
 */
@Data
public class PrestoClientConfig {

    /**
     * jdbc:presto://host:port/catalog
     */
    private static final String BASE_URL = "jdbc:presto://{}:{}/{}";

    /**
     * jdbc:presto://host:port/catalog/schema
     */
    public static final String URL = "jdbc:presto://{}:{}/{}/{}";

    private static final String SCHEMA_QUERY = "show schemas";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private PrestoClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static PrestoClientConfig init(PrestoConnectDto prestoConnectDto, int loginTimeout) throws SQLException {
        PrestoClientConfig prestoClientConfig = new PrestoClientConfig();
        String url = StrUtil.format(URL, prestoConnectDto.getSourceHost(), prestoConnectDto.getSourcePort(),prestoConnectDto.getCatalog(),prestoConnectDto.getSchema());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.facebook.presto.jdbc.PrestoDriver");
        dataSource.setUsername(prestoConnectDto.getSourceUserName());
        dataSource.setPassword(prestoConnectDto.getSourcePwd());
        prestoClientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return prestoClientConfig;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(*) FROM %s AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }


    public static List<String> getSchema(PrestoConnectDto prestoConnectDto) {
        String url = StrUtil.format(BASE_URL, prestoConnectDto.getSourceHost(), prestoConnectDto.getSourcePort(),prestoConnectDto.getCatalog());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.facebook.presto.jdbc.PrestoDriver");
        dataSource.setUsername(prestoConnectDto.getSourceUserName());
        dataSource.setPassword(prestoConnectDto.getSourcePwd());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        return jdbcTemplate.queryForList(SCHEMA_QUERY).stream().map(e -> StrUtil.toString(e.get("Schema"))).collect(Collectors.toList());
    }

    public static void check(PrestoConnectDto prestoConnectDto) {
        String url = StrUtil.format(BASE_URL, prestoConnectDto.getSourceHost(), prestoConnectDto.getSourcePort(),prestoConnectDto.getCatalog());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.facebook.presto.jdbc.PrestoDriver");
        dataSource.setUsername(prestoConnectDto.getSourceUserName());
        dataSource.setPassword(prestoConnectDto.getSourcePwd());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "select 1";
        jdbcTemplate.queryForObject(sql,Integer.class);
    }
}
