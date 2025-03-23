package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.DMConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 达梦
 */
@Data
public class DMClientConfig {
    private static final String URL = "jdbc:dm://{}:{}?connectTimeout={}";
    private static final String SCHEMA_URL = "jdbc:dm://{}:{}/{}?connectTimeout={}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private DMClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static DMClientConfig init(DMConnectDto dmConnectDto, int loginTimeout) throws SQLException {
        DMClientConfig clientConfig = new DMClientConfig();
        String url;
        if (StrUtil.isNotBlank(dmConnectDto.getSchema())) {
            url = StrUtil.format(SCHEMA_URL, dmConnectDto.getSourceHost(), dmConnectDto.getSourcePort(), dmConnectDto.getSchema(), loginTimeout);
        } else {
            url = StrUtil.format(URL, dmConnectDto.getSourceHost(), dmConnectDto.getSourcePort(), loginTimeout);
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("dm.jdbc.driver.DmDriver");
        dataSource.setUsername(dmConnectDto.getSourceUserName());
        dataSource.setPassword(dmConnectDto.getSourcePwd());
        clientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return clientConfig;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryDataFormat(String sql, Object... args) {
        String format = StrUtil.format(sql, args);
        return jdbcTemplate.queryForList(format);
    }

    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(1) FROM %s AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }

}
