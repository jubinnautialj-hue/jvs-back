package cn.bctools.data.factory.source.data.mqtt;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.service.impl.JvsDataFactoryServiceImpl;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Setter
public class OnMessageCallback implements MqttCallback {
    /**
     * 缓存的key
     */
    private String redisKey;
    /**
     * 需要获取一条数据的时候 使用
     */
    @Getter
    private String dataValue;
    /**
     * 数据处理的最大条数
     * 最大1000 默认
     */
    private Integer maxNumber = 1000;
    /**
     * 数据集id
     */
    @Getter
    private String dataFactoryId;

    private static final Object lock = new Object();

    @Override
    public void connectionLost(Throwable cause) {
        TenantContextHolder.clear();
        //如果连接丢失需要停止客户端
        MqttClientFactory mqttClientFactory = SpringContextUtil.getBean(MqttClientFactory.class);
        mqttClientFactory.closeMonitor(this.dataFactoryId);
        //关闭数据集
        JvsDataFactoryServiceImpl jvsDataFactoryService = SpringContextUtil.getBean(JvsDataFactoryServiceImpl.class);
        jvsDataFactoryService.update(new UpdateWrapper<JvsDataFactory>().lambda().eq(JvsDataFactory::getId, this.dataFactoryId).set(JvsDataFactory::getEnable, Boolean.FALSE));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String value = new String(message.getPayload());
        //记录一次结果 用于获取字段
        if (StrUtil.isBlank(this.dataValue)) {
            this.dataValue = value;
        }
        //如果是获取字段 就不会存在写入缓存中
        if (StrUtil.isNotBlank(redisKey)) {
            RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
            synchronized (lock) {
                //如果已经到最大值 就不在写入 直接丢弃
                if (redisUtils.lGetListSize(redisKey) < maxNumber) {
                    //写入值
                    redisUtils.lSetList(redisKey, value);
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /**
     * 获取redis缓存中的值 获取完成后会进行清空
     */
    public List<String> getRedisValue() {
        RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
        List<String> list = new ArrayList<>();
        synchronized (lock) {
            if (redisUtils.hasKey(redisKey)) {
                Object object = redisUtils.getSetList(redisKey);
                String jsonString = JSONObject.toJSONString(object);
                list = JSONArray.parseArray(jsonString, String.class);
                redisUtils.del(redisKey);
            }
        }
        return list;
    }

    /**
     * 清理redis数据
     */
    public void clearRedisValue() {
        RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
        synchronized (lock) {
            if (redisUtils.hasKey(redisKey)) {
                redisUtils.del(redisKey);
            }
        }
    }
}
