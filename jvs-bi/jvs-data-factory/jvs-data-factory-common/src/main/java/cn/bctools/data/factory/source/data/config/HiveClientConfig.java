package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Data
public class HiveClientConfig {

    public static final String URL = "jdbc:hive2://{}:{}/{}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private HiveClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static HiveClientConfig init(PublicConnectDto publicConnectDto) throws SQLException {
        HiveClientConfig clientConfig = new HiveClientConfig();
        String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
        dataSource.setUsername(publicConnectDto.getSourceUserName());
        dataSource.setPassword(publicConnectDto.getSourcePwd());
        clientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return clientConfig;
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
}
