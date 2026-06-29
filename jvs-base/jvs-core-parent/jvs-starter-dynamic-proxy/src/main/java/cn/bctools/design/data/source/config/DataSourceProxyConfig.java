package cn.bctools.design.data.source.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author bcotols.cn
 * 数据源初始化代理对象
 */
@ConfigurationProperties(prefix = "dynamic")
public class DataSourceProxyConfig {

    DataSourceEnum type;

}
