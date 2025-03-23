package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.SqlServerConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

@Data
public class SqlServerClientConfig {

    public static final String URL = "jdbc:sqlserver://{}:{};databaseName={};sslEnabled=false;trustServerCertificate=true;";

    public static final String NO_DATA_BASE_URL = "jdbc:sqlserver://{}:{};sslEnabled=false;trustServerCertificate=true;";

    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private SqlServerClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static SqlServerClientConfig init(SqlServerConnectDto publicConnectDto) {
        SqlServerClientConfig clientConfig = new SqlServerClientConfig();
        String url;
        if (StrUtil.isNotBlank(publicConnectDto.getSourceLibraryName())) {
            url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
        } else {
            url = StrUtil.format(NO_DATA_BASE_URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort());
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
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

    public Long getTotalCount(SqlServerConnectDto publicConnectDto, String tableName) {
        String countSql = StrUtil.format("SELECT COUNT(*) FROM {}.{}.{} AS total", publicConnectDto.getSourceLibraryName(), publicConnectDto.getSchema(), tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
