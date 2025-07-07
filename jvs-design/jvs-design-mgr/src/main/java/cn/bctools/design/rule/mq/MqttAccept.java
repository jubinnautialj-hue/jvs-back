//package cn.bctools.design.rule.mq;
//
//import cn.bctools.database.util.IdGenerator;
//import cn.bctools.common.utils.ObjectNull;
//import cn.bctools.design.rule.RuleRunService;
//import cn.bctools.design.rule.entity.MqttDto;
//import cn.bctools.design.rule.entity.RuleDesignPo;
//import cn.bctools.design.rule.entity.RuleType;
//import cn.bctools.design.rule.service.RuleDesignService;
//import cn.hutool.core.lang.Dict;
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.paho.client.mqttv3.*;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
///**
// * @author jvs
// * 通过mqtt数据处理
// */
//@Slf4j
//@Lazy(value = false)
//@Component
//public class MqttAccept {
//
//    /**
//     * The Rule design service.
//     */
//    @Autowired
//    RuleDesignService ruleDesignService;
//    /**
//     * The Rule run service.
//     */
//    @Autowired
//    RuleRunService ruleRunService;
//
//
//    /**
//     * 自动开启已经发布的逻辑的监听服务
//     */
//    @PostConstruct
//    public void get() {
//        ruleDesignService.list(new LambdaQueryWrapper<RuleDesignPo>()
//                        .eq(RuleDesignPo::getReqType, RuleType.Listening_logic)
//                        .eq(RuleDesignPo::getEnable, true)
//                        .isNotNull(RuleDesignPo::getMqttDto))
//                .forEach(e -> {
//                    try {
//                        connect(e.getMqttDto(), e.getSecret());
//                    } catch (Exception ex) {
//                        //可能导致无法正常启动
//                        log.error("启动初始化历史监听失败", ex);
//                    }
//                });
//    }
//
//    /**
//     * 客户端连接
//     *
//     * @param dto     the dto
//     * @param ruleKey 逻辑引擎执行的Key
//     */
//    @SneakyThrows
//    public void connect(MqttDto dto, String ruleKey) {
//        if (ObjectNull.isNull(dto.getServerURI())) {
//            return;
//        }
//        String clientId = IdGenerator.getIdStr();
//        MqttClient client = new MqttClient(dto.getServerURI(), clientId, new MemoryPersistence());
//        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName(dto.getUserName());
//        if (ObjectNull.isNotNull(dto.getPassword())) {
//            options.setPassword(dto.getPassword().toCharArray());
//        }
//        client.setCallback(new MqttCallbackExtended() {
//
//            @SneakyThrows
//            @Override
//            public void connectionLost(Throwable cause) {
//                log.info("连接断开，可以做重连");
//                if (client == null || !client.isConnected()) {
//                    log.info("emqx重新连接....................................................");
//                    client.connect();
//                }
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                String s = new String(message.getPayload());
//                log.info("接收消息主题 : " + topic);
//                log.info("接收消息Qos : " + message.getQos());
//                log.info("接收消息内容 : " + s);
//                try {
//                    //测试执行
//                    if (JSON.isValidArray(s)) {
//                        //支持数组
//                        JSONArray objects = JSON.parseArray(s);
//                        Dict data = Dict.create().set("data", objects);
//                        ruleRunService.run(ruleKey, data);
//                    } else {
//                        JSONObject dataMap = JSON.parseObject(s);
//                        ruleRunService.run(ruleKey, dataMap);
//                    }
//                } catch (Exception e) {
//                    log.error("启动mqtt失败", e);
//                }
//            }
//
//            @SneakyThrows
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//                String[] topics = token.getTopics();
//                for (String topic : topics) {
//                    log.info("向主题：" + topic + "发送消息成功！");
//                }
//                MqttMessage message = token.getMessage();
//                byte[] payload = message.getPayload();
//                String s = new String(payload, "UTF-8");
//                log.info("消息的内容是：" + s);
//            }
//
//            @SneakyThrows
//            @Override
//            public void connectComplete(boolean reconnect, String serverUri) {
//                log.info("--------------------ClientId:" + client.getClientId() + "客户端连接成功！--------------------");
//                // 以/#结尾表示订阅所有以test开头的主题
//                // 订阅所有机构主题
//                client.subscribe(dto.getTopic(), dto.getQos());
//            }
//        });
//        client.connect();
//    }
//
//}
