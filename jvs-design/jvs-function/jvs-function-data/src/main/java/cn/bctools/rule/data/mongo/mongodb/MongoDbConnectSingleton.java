package cn.bctools.rule.data.mongo.mongodb;

import cn.bctools.rule.data.mongo.MongoDBOption;
import cn.hutool.core.util.StrUtil;
import com.mongodb.client.MongoClient;

/**
 * @author admin
 */
public class MongoDbConnectSingleton {

    /**
     * 数据源池
     */

    private MongoDbConnectSingleton() {

    }

    /**
     * 初始化
     *
     * @param connectTimeout   连接超时时间单位毫秒
     * @param readTimeout      读取超时时间单位毫秒
     */
    public static MongoDsClient getInstance(MongoDBOption option, Integer connectTimeout, Integer readTimeout) {
        //创建客户端
        MongoDsClient mongoDsClient;
        if (StrUtil.isNotBlank(option.getUserName()) && StrUtil.isNotBlank(option.getPassword())) {
            mongoDsClient = new MongoDsClient(option.getHost(), option.getPort(), option.getUserName(), option.getPassword(), option.getLibraryName(), connectTimeout, readTimeout, option.getAuthorization());
        } else {
            mongoDsClient = new MongoDsClient(option.getHost(), option.getPort(), connectTimeout, readTimeout);
        }
        return mongoDsClient;
    }

    /**
     * 获取客户端
     *
     * @param connectTimeout   连接超时时间单位毫秒
     * @param readTimeout      读取超时时间单位毫秒
     */
    public static MongoClient getClient(MongoDBOption option, Integer connectTimeout, Integer readTimeout) {
        return getInstance(option, connectTimeout, readTimeout).getClient();
    }

}
