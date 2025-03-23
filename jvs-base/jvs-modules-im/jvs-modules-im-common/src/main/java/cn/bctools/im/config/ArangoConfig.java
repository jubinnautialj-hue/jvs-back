package cn.bctools.im.config;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author: ZhuXiaoKang
 * @Description: ArangoDB配置
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
@Configuration
@EnableArangoRepositories(basePackages = {"cn.bctools.im"})
public class ArangoConfig implements ArangoConfiguration {

    @Resource
    private ArangoProperties arangoProperties;

    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder()
                .host(arangoProperties.getHost(), arangoProperties.getPort())
                .user(arangoProperties.getUserName())
                .password(arangoProperties.getPassword());
    }

    @Override
    public String database() {
        return arangoProperties.getDbName();
    }
}
