package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * mysql客户端获取
 *
 * @author admin
 */
@Data
public class MysqlClientConfig {
    private static final String URL = "jdbc:mysql://{}:{}/{}?connectTimeout={}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private MysqlClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static MysqlClientConfig init(PublicConnectDto publicConnectDto, int loginTimeout) throws SQLException {
        MysqlClientConfig mysqlClientConfig = new MysqlClientConfig();
        String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName(), loginTimeout);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(publicConnectDto.getSourceUserName());
        dataSource.setPassword(publicConnectDto.getSourcePwd());
        mysqlClientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return mysqlClientConfig;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }


    public Long execTotal(String sql) {
        String countSql = String.format("SELECT COUNT(*) FROM ( %s ) AS total", sql);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }

    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(*) FROM %s AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
