package cn.bctools.rule.data.sqlserver;

import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.function.Function;

/**
 * @author jvs
 */
public class SqlServerBase {

    public Function<DatasourceSelectedOption, DriverManagerDataSource> options(DatasourceSelectedOption option) {
        Function<DatasourceSelectedOption, DriverManagerDataSource> sqlserver_templateFunction = e -> {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            String url = ClientConfig.SQLSERVER_URL.apply(option);
            dataSource.setUrl(url);
            dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource.setUsername(option.getUserName());
            dataSource.setPassword(option.getPassWord());
            return dataSource;
        };
        return sqlserver_templateFunction;
    }
}
