package cn.bctools.data.factory.source.data.config;

import cn.bctools.data.factory.source.dto.KingBaseConnectDto;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class KingBaseClientConfig {

    public static final String URL = "jdbc:kingbase8://{}:{}/{}?currentSchema={}&clientEncoding=UTF-8";
    private static final String GET_SCHEMA_URL = "jdbc:kingbase8://{}:{}/{}?clientEncoding=UTF-8";

    /**
     * 获取整个库的所有模式  会排查默认的模式
     * pg_toast 模式包含了 PostgreSQL 表中存储大对象的相关数据。
     * pg_catalog 模式包含了 PostgreSQL 系统目录中的元数据信息，例如系统表、系统视图和系统函数等。
     * information_schema 模式包含了关于数据库对象（表、列、视图等）的标准化元数据信息。
     */
    private static final String GET_SCHEMA = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT IN ('pg_toast', 'pg_catalog', 'information_schema');";

    /**
     * jdbc
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造方法
     */
    private KingBaseClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static KingBaseClientConfig init(KingBaseConnectDto connectDto) throws SQLException {
        KingBaseClientConfig clientConfig = new KingBaseClientConfig();
        String schema = Optional.of(connectDto).map(KingBaseConnectDto::getSchema).orElse("public");
        String url = StrUtil.format(URL, connectDto.getSourceHost(), connectDto.getSourcePort(), connectDto.getSourceLibraryName(),schema);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.kingbase8.Driver");
        dataSource.setUsername(connectDto.getSourceUserName());
        dataSource.setPassword(connectDto.getSourcePwd());
        clientConfig.setJdbcTemplate(new JdbcTemplate(dataSource));
        return clientConfig;
    }


    public List<Map<String, Object>> queryData(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryData(String sql,Object[] args) {
        return jdbcTemplate.queryForList(sql,args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(*) FROM (%s) AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }

    public static List<Map<String, Object>> getSchema(KingBaseConnectDto connectDto){
        String url = StrUtil.format(GET_SCHEMA_URL, connectDto.getSourceHost(), connectDto.getSourcePort(), connectDto.getSourceLibraryName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.kingbase8.Driver");
        dataSource.setUsername(connectDto.getSourceUserName());
        dataSource.setPassword(connectDto.getSourcePwd());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForList(GET_SCHEMA);
    }
}
