package cn.bctools.im.config;

import cn.bctools.im.command.*;
import cn.hutool.core.map.MapUtil;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.im.client.ImClientChannelContext;
import cn.bctools.im.client.JimClient;
import cn.bctools.im.helper.DbMessageHelper;
import cn.bctools.im.listener.ImGroupListener;
import cn.bctools.im.listener.ImUserListener;
import cn.bctools.im.processor.JvsWsHandshakeProcessor;
import cn.bctools.im.processor.LoginServiceProcessor;
import cn.bctools.im.processor.MessageProcessor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.config.ImConfig;
import org.jim.core.http.HttpConfig;
import org.jim.core.packets.Command;
import org.jim.core.utils.PropUtil;
import org.jim.server.JimServer;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.HandshakeReqHandler;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.config.PropertyImServerConfigBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.tio.core.ssl.SslConfig;

import java.util.*;
import java.util.stream.Collectors;

//import com.basics.utils.SpringContextUtil;

/**
 * @Author: ZhuXiaoKang
 * @Description: JIM 服务配置
 */
public class JimConfig {

    private JimConfig() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * JIM配置前缀
     */
    private static final String JIM_PROPERTIES_PREFIX = "jim.";

    /**
     * 公用配置文件名
     */
    private static final String APPLICATION_PROPERTIES_NAME = "application";

    /**
     * redis配置前缀
     */
    private static final String REDIS_PROPERTIES_PREFIX = "spring.redis";

    /**
     * 客户端上下文
     */
    public static ImClientChannelContext imClientChannelContext = null;

    /**
     * 本地服务端配置
     */
    public static ImServerConfig globalImServerConfig = null;


    /**
     * 启动JIM服务
     *
     * @param environment       spring运行时环境
     * @param nacosRegistration nacos注册信息
     */
    @SneakyThrows
    public static void start(ConfigurableEnvironment environment, NacosRegistration nacosRegistration) {
        // jim配置
        JimServer jimServer = buildJimServer(environment, nacosRegistration);
        // 以下处理器根据业务需要自行添加与扩展，每个Command都可以添加扩展
        extendHandler();
        // 启动JIM服务
        jimServer.start();
        // 启动JIM客户端（用以系统发送信息）
        imClientChannelContext = new JimClient().connect(globalImServerConfig.getBindPort());
        ImConfig.Global.set(globalImServerConfig);
    }


    /**
     * 构建JimServer
     *
     * @param environment       spring运行时环境
     * @param nacosRegistration nacos注册信息
     */
    @SneakyThrows
    private static JimServer buildJimServer(ConfigurableEnvironment environment, NacosRegistration nacosRegistration) {
        // 得到JIM配置
        Map<String, String> jimConfigMap = getJimConfig(environment, nacosRegistration);
        // 构建JIM配置
        PropertyImServerConfigBuilder propertyImServerConfigBuilder = new PropertyImServerConfigBuilder(jimConfigMap);
//        Random random = new Random();
//        String serverPort = String.valueOf(random.nextInt(12000) % (12000 - 10000 + 1) + 10000);
        // websocket端口固定 见{@url https://www.kdocs.cn/l/chuWDzUN56lp}的"开发环境各服务端口"
        String serverPort = "30015";
        PropUtil.getProp().getProperties().setProperty("jim.port", serverPort);

        HttpConfig build = HttpConfig.newBuilder().build();
        build.setBindPort(Integer.valueOf(serverPort));
        propertyImServerConfigBuilder.configHttp(build);

        ImServerConfig imServerConfig = propertyImServerConfigBuilder.build();
        imServerConfig.setBindPort(Integer.valueOf(serverPort));

        // 初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
        initSsl(imServerConfig);
        // 设置群组监听器，非必须，根据需要自己选择性实现
        imServerConfig.setImGroupListener(new ImGroupListener());
        // 设置绑定用户监听器，非必须，根据需要自己选择性实现
        imServerConfig.setImUserListener(new ImUserListener());
        // 设置持久化帮助类
        DbMessageHelper dbMessageHelper = SpringContextUtil.getBean(DbMessageHelper.class);
        imServerConfig.setMessageHelper(dbMessageHelper);

        globalImServerConfig = imServerConfig;
        return new JimServer(imServerConfig);
    }

    /**
     * 得到JIM Service配置，转为Map
     *
     * @param environment       spring运行时环境
     * @param nacosRegistration nacos注册信息
     */
    private static Map<String, String> getJimConfig(ConfigurableEnvironment environment, NacosRegistration nacosRegistration) {
        Map<String, String> config = new HashMap<>(16);

        String serviceName = nacosRegistration.getNacosDiscoveryProperties().getService();
        MutablePropertySources propertySources = environment.getPropertySources();
        Iterator var2 = propertySources.iterator();
        while (var2.hasNext()) {
            PropertySource p = (PropertySource) var2.next();
            // 解析项目名匹配的配置
            if (p.getSource() instanceof LinkedHashMap) {
                LinkedHashMap<String, String> sourceMaps = (LinkedHashMap<String, String>) p.getSource();
                // 得到属性包含"jim."的配置信息
                Map<String, String> jimPropertySource = sourceMaps.entrySet().stream()
                        .filter(k -> k.getKey().contains(JIM_PROPERTIES_PREFIX) || k.getKey().contains(REDIS_PROPERTIES_PREFIX))
                        .collect(Collectors.toMap(k -> k.getKey(), k -> String.valueOf(k.getValue())));
                if (MapUtil.isNotEmpty(jimPropertySource)) {
                    config.putAll(jimPropertySource);
                }
            }
            // 获取redis配置
            if (p.getName().contains(APPLICATION_PROPERTIES_NAME) && p.getSource() instanceof LinkedHashMap) {
                LinkedHashMap<String, String> sourceMaps = (LinkedHashMap<String, String>) p.getSource();
                config.put("jim.redis.host", sourceMaps.get("spring.redis.host"));
                config.put("jim.redis.port", String.valueOf(sourceMaps.get("spring.redis.port")));
                config.put("jim.redis.database", String.valueOf(sourceMaps.get("spring.redis.database")));
                config.put("jim.redis.timeout", String.valueOf(sourceMaps.get("spring.redis.timeout")));
            }

        }

        return config;
    }


    /**
     * 开启SSL之前，你要保证你有SSL证书哦！
     *
     * @param imServerConfig
     * @throws Exception
     */
    private static void initSsl(ImServerConfig imServerConfig) throws Exception {
        // 开启SSL
        if (ImServerConfig.ON.equals(imServerConfig.getIsSSL())) {
            String keyStorePath = PropUtil.get("jim.key.store.path");
            String keyStoreFile = keyStorePath;
            String trustStoreFile = keyStorePath;
            String keyStorePwd = PropUtil.get("jim.key.store.pwd");
            if (StringUtils.isNotBlank(keyStoreFile) && StringUtils.isNotBlank(trustStoreFile)) {
                SslConfig sslConfig = SslConfig.forServer(keyStoreFile, trustStoreFile, keyStorePwd);
                imServerConfig.setSslConfig(sslConfig);
            }
        }
    }

    /**
     * Im处理器扩展,此类，最多只参修改此方法，其它方法不要修改
     */
    private static void extendHandler() {
        // 添加自定义握手处理器
        HandshakeReqHandler handshakeReqHandler = CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
        handshakeReqHandler.addMultiProtocolProcessor(new JvsWsHandshakeProcessor());

        // 添加登录业务处理器
        LoginReqHandler loginReqHandler = CommandManager.getCommand(Command.COMMAND_LOGIN_REQ, LoginReqHandler.class);
        loginReqHandler.setSingleProcessor(new LoginServiceProcessor());

        // 添加用户业务聊天记录处理器
        ChatReqHandler chatReqHandler = CommandManager.getCommand(Command.COMMAND_CHAT_REQ, ChatReqHandler.class);
        chatReqHandler.setSingleProcessor(new MessageProcessor());

        // 添加通知记录处理器
        NotifyReqHandler notifyReqHandler = CommandManager.getCommand(Command.COMMAND_NOTIFY_REQ, NotifyReqHandler.class);
        notifyReqHandler.setSingleProcessor(new MessageProcessor());

        // 添加业务自定义消息处理器
        BusinessReqHandler businessReqHandler = CommandManager.getCommand(Command.COMMAND_BUSINESS_REQ, BusinessReqHandler.class);
        businessReqHandler.setSingleProcessor(new MessageProcessor());

        // 变更用户信息
        GroupUserUpdateReqHandler groupUserUpdateReqHandler = CommandManager.getCommand(Command.COMMAND_GROUP_USER_UPDATE_REQ, GroupUserUpdateReqHandler.class);
        groupUserUpdateReqHandler.setSingleProcessor(new MessageProcessor());

        // 退出群组
        QuitGroupReqHandler quitGroupReqHandler = CommandManager.getCommand(Command.COMMAND_QUIT_GROUP_REQ, QuitGroupReqHandler.class);
        quitGroupReqHandler.setSingleProcessor(new MessageProcessor());

        // 获取组用户信息
        GetGroupUserReqHandler getGroupUserReqHandler = CommandManager.getCommand(Command.COMMAND_GET_GROUP_USER_REQ, GetGroupUserReqHandler.class);
        getGroupUserReqHandler.setSingleProcessor(new MessageProcessor());
    }
}
