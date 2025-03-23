package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

@Data
public class ClickHouseClientConfig {

    public static final String URL = "jdbc:clickhouse://{}:{}/{}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private ClickHouseClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static ClickHouseClientConfig init(PublicConnectDto publicConnectDto, int loginTimeout) {
        ClickHouseClientConfig clientConfig = new ClickHouseClientConfig();
        String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
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
