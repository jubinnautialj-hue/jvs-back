package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.Db2ConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Data
public class Db2ClientConfig {

    public static final String URL = "jdbc:db2://{}:{}/{}";
    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private Db2ClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static Db2ClientConfig init(Db2ConnectDto connectDto, int loginTimeout) throws SQLException {
        Db2ClientConfig clientConfig = new Db2ClientConfig();
        String url = StrUtil.format(URL, connectDto.getSourceHost(), connectDto.getSourcePort(), connectDto.getSourceLibraryName(), loginTimeout);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.ibm.db2.jcc.DB2Driver");
        dataSource.setUsername(connectDto.getSourceUserName());
        dataSource.setPassword(connectDto.getSourcePwd());
        clientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return clientConfig;
    }

    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public List<Map<String, Object>> queryDataFormat(String sql, Object... args) {
        return jdbcTemplate.queryForList(StrUtil.format(sql, args));
    }

    public Long getTotalCount(Db2ConnectDto connectDto,String tableName) {
        String countSql = StrUtil.format("SELECT COUNT(1) FROM {}.{} AS total", connectDto.getSchema(),tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
