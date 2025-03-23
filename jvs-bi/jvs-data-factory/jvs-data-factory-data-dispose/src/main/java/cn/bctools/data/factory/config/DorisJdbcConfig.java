package cn.bctools.data.factory.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author xiaohui
 */
@EnableConfigurationProperties({DorisConfig.class})
public class DorisJdbcConfig {

    private static final String URL = "jdbc:mysql://{}:{}/{}";

    @Bean
    public DorisJdbcTemplate jdbcTemplate(DorisConfig dorisConfig) {
        String url = StrUtil.format(URL, dorisConfig.getIp(), dorisConfig.getQueryPort(), dorisConfig.getLibraryName());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(dorisConfig.getUserName());
        dataSource.setPassword(dorisConfig.getPassWord());
        return new DorisJdbcTemplate(dataSource);
    }

}
