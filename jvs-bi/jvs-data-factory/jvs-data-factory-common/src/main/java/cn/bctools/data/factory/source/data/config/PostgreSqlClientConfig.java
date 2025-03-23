package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.PostgreSqlConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * mysql客户端获取
 *
 * @author admin
 */
@Data
public class PostgreSqlClientConfig {
    /**
     * 存在模式
     */
    private static final String URL_CURRENT_SCHEMA = "jdbc:postgresql://{}:{}/{}?currentSchema={}&connectTimeout={}";
    /**
     * 不存在模式
     */
    private static final String URL = "jdbc:postgresql://{}:{}/{}?connectTimeout={}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private PostgreSqlClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static PostgreSqlClientConfig init(PostgreSqlConnectDto postgreSqlConnectDto, int loginTimeout) throws SQLException {
        PostgreSqlClientConfig mysqlClientConfig = new PostgreSqlClientConfig();
        String url;
        //如果是获取模式那么链接数据库时模式就不会传入
        if (StrUtil.isBlank(postgreSqlConnectDto.getSchema())) {
            url = StrUtil.format(URL, postgreSqlConnectDto.getSourceHost(), postgreSqlConnectDto.getSourcePort(), postgreSqlConnectDto.getSourceLibraryName(), loginTimeout);
        } else {
            url = StrUtil.format(URL_CURRENT_SCHEMA, postgreSqlConnectDto.getSourceHost(), postgreSqlConnectDto.getSourcePort(), postgreSqlConnectDto.getSourceLibraryName(), postgreSqlConnectDto.getSchema(), loginTimeout);
        }
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setUrl(url);
        pgSimpleDataSource.setUser(postgreSqlConnectDto.getSourceUserName());
        pgSimpleDataSource.setPassword(postgreSqlConnectDto.getSourcePwd());
        mysqlClientConfig.setJdbcTemplate(new JdbcTemplate(pgSimpleDataSource));
        return mysqlClientConfig;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(*) FROM (%s) AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
