package cn.bctools.data.factory.source.data.mqtt;

import cn.bctools.data.factory.config.MqttProperties;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.util.MqttUtil;
import cn.hutool.core.map.SafeConcurrentHashMap;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Administrator
 */
@Component
public class MqttClientFactory {
    /**
     * 所有客户端缓存
     * 数据池
     * key 为数据集id
     */
    private final Map<String, MqttUtil> mqttClientMap = new SafeConcurrentHashMap<>();


    /**
     * 开启监听
     *
     * @param mqttProperties 监听的入参
     * @param factoryId      数据集id
     */
    public void openMonitor(MqttProperties mqttProperties, String factoryId, Integer maxNumber) {
        OnMessageCallback onMessageCallback = new OnMessageCallback();
        String redisKey = RedisKey.getDataFactoryMqtt(factoryId);
        onMessageCallback.setRedisKey(redisKey);
        onMessageCallback.setMaxNumber(maxNumber);
        onMessageCallback.setDataFactoryId(factoryId);
        MqttUtil mqttUtil = new MqttUtil();
        mqttUtil.build(mqttProperties, factoryId, onMessageCallback);
        mqttClientMap.put(factoryId, mqttUtil);
    }

    /**
     * 关闭监听
     *
     * @param factoryId 数据集id
     */
    public void closeMonitor(String factoryId) {
        if (mqttClientMap.containsKey(factoryId)) {
            MqttUtil mqttUtil = mqttClientMap.get(factoryId);
            mqttUtil.close();
        }
    }

    /**
     * 获取客户端工具类
     *
     * @param factoryId 数据集id
     */
    public MqttUtil getMqttUtil(String factoryId) {
        return mqttClientMap.get(factoryId);
    }


}
