package cn.bctools.data.factory.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.config.MqttProperties;
import cn.bctools.data.factory.source.data.mqtt.OnMessageCallback;
import cn.bctools.database.util.IdGenerator;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

@Slf4j
public class MqttUtil {
    /**
     * 客户端
     */
    @Setter
    private MqttClient mqttClient;

    /**
     * 回调类
     */
    @Setter
    @Getter
    private OnMessageCallback onMessageCallback;

    /**
     * 关闭当前对象的链接
     */
    public void close() {
        try {
            if (this.mqttClient != null) {
                this.mqttClient.disconnect();
                this.mqttClient.close();
            }
            if (onMessageCallback!=null) {
                onMessageCallback.clearRedisValue();
            }
        } catch (Exception exception) {
            log.info("关闭mqtt客户端失败", exception);
            throw new BusinessException("关闭mqtt客户端失败:" + exception.getMessage());
        }
    }


    /**
     * 创建mqtt客户端
     *
     * @param clientId       客户端id
     * @param mqttCallback   回调函数
     * @param mqttProperties 连接信息
     * @return
     */
    @SneakyThrows
    public MqttClient build(MqttProperties mqttProperties, String clientId, OnMessageCallback mqttCallback) {
        this.onMessageCallback = mqttCallback;
        //创建MQTT客户端对象
        String serverUri = "tcp://" + mqttProperties.getSourceHost() + ":" + mqttProperties.getSourcePort();
        MqttClient client = new MqttClient(serverUri, clientId, new MemoryPersistence());
        //连接设置
        MqttConnectOptions options = new MqttConnectOptions();
        //是否清空session，设置false表示服务器会保留客户端的连接记录（订阅主题，qos）,客户端重连之后能获取到服务器在客户端断开连接期间推送的消息
        //设置为true表示每次连接服务器都是以新的身份
        options.setCleanSession(Boolean.TRUE);
        if (StrUtil.isAllNotBlank(mqttProperties.getSourceUserName(), mqttProperties.getSourcePwd())) {
            //设置连接用户名
            options.setUserName(mqttProperties.getSourceUserName());
            //设置连接密码
            options.setPassword(mqttProperties.getSourcePwd().toCharArray());
        }
        //设置回调
        client.setCallback(mqttCallback);
        client.connect(options);
        // 订阅
        client.subscribe(mqttProperties.getSubTopic());
        this.mqttClient = client;
        return client;
    }

    /**
     * 创建mqtt客户端
     *
     * @param mqttProperties 连接信息
     * @return 连接是否成功
     */
    public static String check(MqttProperties mqttProperties) {
        try {
            String clientId = "test_check" + IdGenerator.getId();
            //创建MQTT客户端对象
            String serverUri = "tcp://" + mqttProperties.getSourceHost() + ":" + mqttProperties.getSourcePort();
            MqttClient client = new MqttClient(serverUri, clientId, new MemoryPersistence());
            //连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            //是否清空session，设置false表示服务器会保留客户端的连接记录（订阅主题，qos）,客户端重连之后能获取到服务器在客户端断开连接期间推送的消息
            //设置为true表示每次连接服务器都是以新的身份
            options.setCleanSession(Boolean.TRUE);
            if (StrUtil.isAllNotBlank(mqttProperties.getSourceUserName(), mqttProperties.getSourcePwd())) {
                //设置连接用户名
                options.setUserName(mqttProperties.getSourceUserName());
                //设置连接密码
                options.setPassword(mqttProperties.getSourcePwd().toCharArray());
            }
            //设置回调
            client.connect(options);
            client.disconnect();
            client.close();
            return null;
        } catch (Exception exception) {
            log.info("连接mqtt失败", exception);
            return exception.getMessage();
        }
    }
}
