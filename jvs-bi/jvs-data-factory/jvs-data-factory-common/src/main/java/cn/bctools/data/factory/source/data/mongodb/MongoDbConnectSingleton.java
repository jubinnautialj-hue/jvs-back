package cn.bctools.data.factory.source.data.mongodb;

import cn.bctools.data.factory.source.dto.MongoDbConnectDto;
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
     * @param publicConnectDto 连接信息
     */
    public static MongoDsClient getInstance(MongoDbConnectDto publicConnectDto, Integer connectTimeout, Integer readTimeout) {
        //创建客户端
        MongoDsClient mongoDsClient;
        if (StrUtil.isNotBlank(publicConnectDto.getSourceUserName()) && StrUtil.isNotBlank(publicConnectDto.getSourcePwd())) {
            mongoDsClient = new MongoDsClient(publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceUserName(), publicConnectDto.getSourcePwd(), publicConnectDto.getSourceLibraryName(), connectTimeout, readTimeout, publicConnectDto.getAuthenticationDatabase());
        } else {
            mongoDsClient = new MongoDsClient(publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), connectTimeout, readTimeout);
        }
        return mongoDsClient;
    }

    /**
     * 获取客户端
     *
     * @param connectTimeout   连接超时时间单位毫秒
     * @param readTimeout      读取超时时间单位毫秒
     * @param publicConnectDto 连接信息
     */
    public static MongoClient getClient(MongoDbConnectDto publicConnectDto, Integer connectTimeout, Integer readTimeout) {
        return getInstance(publicConnectDto, connectTimeout, readTimeout).getClient();
    }

}
