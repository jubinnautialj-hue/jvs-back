package cn.bctools.rule.data.mongo.mongodb;

import cn.hutool.core.util.StrUtil;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MongoDsClient {
    /**
     * MongoDB单点连接信息
     */
    private ServerAddress serverAddress;
    /**
     * MongoDB客户端对象
     */
    private MongoClient mongo;
    /**
     * 用户认证信息 不是所有都需要
     */

    private MongoCredential credential;


    /**
     * 客户端初始化
     *
     * @param readTimeout    读取超时时间毫秒
     * @param connectTimeout 连接超时时间毫秒
     * @param host           链接地址
     * @param port           端口
     */
    public MongoDsClient(String host, int port, Integer connectTimeout, Integer readTimeout) {
        this.serverAddress = new ServerAddress(host, port);
        this.mongo = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(serverAddress)))
                .applyToSocketSettings(e -> {
                    e.connectTimeout(Optional.ofNullable(connectTimeout).orElse(10000), TimeUnit.MILLISECONDS);
                    e.readTimeout(Optional.ofNullable(readTimeout).orElse(10000), TimeUnit.MILLISECONDS);
                })
                .build());
    }

    /**
     * 客户端初始化
     *
     * @param readTimeout    读取超时时间毫秒
     * @param connectTimeout 连接超时时间毫秒
     * @param host           链接地址
     * @param port           端口
     * @param password       密码
     * @param userName       用户名称
     * @param database       用户所在的数据库
     */
    public MongoDsClient(String host, int port, String userName, String password, String database, Integer connectTimeout, Integer readTimeout, String authenticationDatabase) {
        if (StrUtil.isNotBlank(authenticationDatabase)) {
            database = authenticationDatabase;
        }
        this.serverAddress = new ServerAddress(host, port);
        this.credential = MongoCredential.createCredential(userName, database, password.toCharArray());
        this.mongo = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(serverAddress)))
                .credential(credential)
                .applyToSocketSettings(e -> {
                    e.connectTimeout(Optional.ofNullable(connectTimeout).orElse(10000), TimeUnit.MILLISECONDS);
                    e.readTimeout(Optional.ofNullable(readTimeout).orElse(10000), TimeUnit.MILLISECONDS);
                })
                .build());
    }

    /**
     * 获取客户端
     */
    public MongoClient getClient() {
        return this.mongo;
    }


    /**
     * 关闭
     */
    public void close() {
        this.mongo.close();
    }

}
