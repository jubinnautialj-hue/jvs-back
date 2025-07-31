package cn.bctools.rule.data.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.data.kingbase.KingBaseDatasourceSelectedOption;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
import cn.bctools.rule.data.oracle.OracleSelectedOption;
import cn.bctools.rule.data.postgresql.PgsqlSelectOption;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.parser.ParserConfig;
import lombok.Data;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * DM客户端获取
 *
 * @author admin
 */
@Data
public class ClientConfig {


    private JdbcTemplate jdbcTemplate;

    static String KINGBASE_URL = "jdbc:kingbase8://{}:{}/{}?currentSchema={}&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true" +
                                 "&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&stringtype=unspecified";

    public static Function<KingBaseDatasourceSelectedOption, String> KINGBASE_FUNCTION_URL = datasourceSelectedOption -> {
        return StrUtil.format(KINGBASE_URL, datasourceSelectedOption.getIp(), datasourceSelectedOption.getPort(), datasourceSelectedOption.getDatabaseName(), datasourceSelectedOption.getSchema());
    };

    static String URL = "jdbc:dm://{}:{}?schema={}";

    static String MYSQL = "jdbc:mysql://{}:{}/{}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true";

    static String PGSQL = "jdbc:postgresql://{}:{}/{}?currentSchema={}&connectTimeout={}";

    static String SQLSERVER = "jdbc:sqlserver://{}:{};databaseName={};user={};password={};encrypt=false;loginTimeout={};";
    /**
     * 通过服务名链接
     */
    private static final String URL_SERVER_NAME = "jdbc:oracle:thin:@//{}:{}/{}";

    private static final String URL_SID = "jdbc:oracle:thin:@//{}:{}:{}";


    public static final Function<DatasourceSelectedOption, String> SQLSERVER_URL = sqlserverSelectOption -> {
        return StrUtil.format(SQLSERVER, sqlserverSelectOption.getIp(), sqlserverSelectOption.getPort(), sqlserverSelectOption.getDatabaseName(), sqlserverSelectOption.getUserName(), sqlserverSelectOption.getPassWord(), 5000);
    };

    public static Function<PgsqlSelectOption, String> PGSQL_URL = pgsqlSelectOption -> {
        return StrUtil.format(PGSQL, pgsqlSelectOption.getSourceHost(), pgsqlSelectOption.getSourcePort(), pgsqlSelectOption.getSourceLibraryName(), pgsqlSelectOption.getSchema(), 5000);
    };

    public static Function<DatasourceSelectedOption, String> MYSQL_URL = datasourceSelectedOption -> {
        return StrUtil.format(MYSQL, datasourceSelectedOption.getIp(), datasourceSelectedOption.getPort(), datasourceSelectedOption.getDatabaseName());
    };

    public static Function<OracleSelectedOption, String> ORACLE_URL = oracleConnectDto -> {
        String url;
        //如果是获取模式那么链接数据库时模式就不会传入
        if (StrUtil.isNotBlank(oracleConnectDto.getServerName())) {
            url = StrUtil.format(URL_SERVER_NAME, oracleConnectDto.getSourceHost(), oracleConnectDto.getSourcePort(), oracleConnectDto.getServerName());
        } else {
            url = StrUtil.format(URL_SID, oracleConnectDto.getSourceHost(), oracleConnectDto.getSourcePort(), oracleConnectDto.getSid());
        }
        return url;
    };

    public static Function<DatasourceSelectedOption, String> DM_URL = datasourceSelectedOption -> {
        return StrUtil.format(URL, datasourceSelectedOption.getIp(), datasourceSelectedOption.getPort(), datasourceSelectedOption.getDatabaseName());
    };

    private static final TimedCache<String, DriverManagerDataSource> local = CacheUtil.newTimedCache(1000 * 60 * 60);

    private static final TimedCache<String, PGSimpleDataSource> PGSQL_LOCAL = CacheUtil.newTimedCache(1000 * 60 * 60);
    private static final TimedCache<String, DriverManagerDataSource> ORACLE_LOCAL = CacheUtil.newTimedCache(1000 * 60 * 60);

    private Boolean status = Boolean.FALSE;

    /**
     * 构造方法
     */
    private ClientConfig() {
    }

    /**
     * 获取客户端
     */
    public static ClientConfig init(KingBaseDatasourceSelectedOption publicConnectDto, Function<KingBaseDatasourceSelectedOption, DriverManagerDataSource> function) throws SQLException {
        ClientConfig client = new ClientConfig();
        //重用dataSource
        String concat = StrUtil.concat(true, publicConnectDto.getIp(), StrUtil.toString(publicConnectDto.getPort()), publicConnectDto.getDatabaseName(), publicConnectDto.getUserName(),
                publicConnectDto.getPassWord());
        String key = SecureUtil.md5(concat);
        DriverManagerDataSource dataSource = local.get(key, () -> function.apply(publicConnectDto));
        client.setJdbcTemplate(new JdbcTemplate(dataSource));
        return client;
    }


    /**
     * 获取客户端
     */
    public static ClientConfig init(DatasourceSelectedOption publicConnectDto, Function<DatasourceSelectedOption, DriverManagerDataSource> function) throws SQLException {
        ClientConfig client = new ClientConfig();
        //重用dataSource
        String concat = StrUtil.concat(true, publicConnectDto.getIp(), StrUtil.toString(publicConnectDto.getPort()), publicConnectDto.getDatabaseName(), publicConnectDto.getUserName(),
                publicConnectDto.getPassWord());
        String key = SecureUtil.md5(concat);
        DriverManagerDataSource dataSource = local.get(key, () -> function.apply(publicConnectDto));
        client.setJdbcTemplate(new JdbcTemplate(dataSource));
        return client;
    }

    public static ClientConfig init(OracleSelectedOption option, Function<OracleSelectedOption, DriverManagerDataSource> function) {
        ClientConfig client = new ClientConfig();
        //重用dataSource
        String concat = StrUtil.concat(true, option.getSourceHost(), StrUtil.toString(option.getSourcePort()), option.getSchema(), option.getSourceUserName(),
                option.getSourcePwd());
        String key = SecureUtil.md5(concat);
        DriverManagerDataSource dataSource = ORACLE_LOCAL.get(key, () -> function.apply(option));
        client.setJdbcTemplate(new JdbcTemplate(dataSource));
        return client;
    }


    public static ClientConfig init(PgsqlSelectOption option, Function<PgsqlSelectOption, PGSimpleDataSource> function) {
        ClientConfig client = new ClientConfig();
        //重用dataSource
        String concat = StrUtil.concat(true, option.getSourceHost(), StrUtil.toString(option.getSourcePort()), option.getSchema(), option.getSourceUserName(),
                option.getSourcePwd());
        String key = SecureUtil.md5(concat);
        PGSimpleDataSource dataSource = PGSQL_LOCAL.get(key, () -> function.apply(option));
        client.setJdbcTemplate(new JdbcTemplate(dataSource));
        return client;
    }

    public Object queryData(String sql, String resultType) {
        if (StrUtil.equals(resultType, "OBJECT")) {
            return jdbcTemplate.queryForObject(sql, Object.class);
        } else {
            return jdbcTemplate.queryForList(sql);
        }

    }

    public Object updateData(String sql) {
        return this.execute(sql);
    }

    public Object insertData(String sql) {
        return this.execute(sql);
    }

    public Integer execute(String sql) {
        // 使用PreparedStatementCreator创建预处理SQL语句
        PreparedStatementCreator psc = con -> con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // 执行预处理SQL语句并获取执行结果（可选）
        try {
            Integer execute = jdbcTemplate.execute(psc, PreparedStatement::executeUpdate);
            this.status = Boolean.TRUE;
            return execute;
        } catch (DataAccessException e) {
            throw new BusinessException("sql【{}】执行异常", sql);
        }
    }


    public List<Map<String, Object>> queryData(String sql, Object[] args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public Long getTotalCount(String tableName) {
        String countSql = String.format("SELECT COUNT(*) FROM %s AS total", tableName);
        return jdbcTemplate.queryForObject(countSql, Long.class);
    }
}
