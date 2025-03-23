package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.OracleConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * mysql客户端获取
 *
 * @author admin
 */
@Data
public class OracleClientConfig {
    /**
     * 通过服务名链接
     */
    private static final String URL_SERVER_NAME = "jdbc:oracle:thin:@//{}:{}/{}";

    private static final String URL_SID = "jdbc:oracle:thin:@//{}:{}:{}";

    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private OracleClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static OracleClientConfig init(OracleConnectDto oracleConnectDto, int loginTimeout) throws SQLException {
        OracleClientConfig mysqlClientConfig = new OracleClientConfig();
        String url;
        //如果是获取模式那么链接数据库时模式就不会传入
        if (StrUtil.isNotBlank(oracleConnectDto.getServerName())) {
            url = StrUtil.format(URL_SERVER_NAME, oracleConnectDto.getSourceHost(), oracleConnectDto.getSourcePort(), oracleConnectDto.getServerName());
        } else {
            url = StrUtil.format(URL_SID, oracleConnectDto.getSourceHost(), oracleConnectDto.getSourcePort(), oracleConnectDto.getSid());
        }
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl(url);
        driverManagerDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        //链接的属性
        Properties properties = new Properties();
        //设置超时时间
        properties.setProperty("connectTimeout", String.valueOf(loginTimeout));
        driverManagerDataSource.setConnectionProperties(properties);
        driverManagerDataSource.setUsername(oracleConnectDto.getSourceUserName());
        driverManagerDataSource.setPassword(oracleConnectDto.getSourcePwd());
        mysqlClientConfig.setJdbcTemplate(new JdbcTemplate(driverManagerDataSource));
        return mysqlClientConfig;
    }


    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public Long execTotal(String sql) {
        String countSql = String.format("SELECT COUNT(1) AS total FROM ( %s )", sql);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }

    public Long getTotalCount(String tableName, String schema) {
        String countSql = StrUtil.format("SELECT COUNT(*) AS total FROM \"{}\".\"{}\" ", schema, tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
