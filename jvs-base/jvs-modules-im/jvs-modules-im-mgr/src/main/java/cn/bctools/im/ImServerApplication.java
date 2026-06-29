package cn.bctools.im;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.feign.annotation.EnableJvsFeignClients;
import cn.bctools.im.config.JimConfig;
import cn.bctools.im.entity.ServerInfo;
import cn.bctools.im.rabbit.SyncMessageConsumer;
import cn.bctools.im.service.ImServerInfoService;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.cluster.ImClusterVO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * IM服务端启动类
 *
 * @author WChao
 */
@Slf4j
@EnableAsync
@RefreshScope
@EnableDiscoveryClient
@EnableJvsFeignClients
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"cn.bctools.*", "org.jim.*"})
public class ImServerApplication {

    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ImServerApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        NacosRegistration nacosRegistration = run.getBean(NacosRegistration.class);
        nacosRegistration.setPort(Integer.valueOf(environment.getProperty("server.port")));
        AbstractAutoServiceRegistration bean = run.getBean(AbstractAutoServiceRegistration.class);
        bean.start();
        // 启动JIM服务
        try {
            JimConfig.start(environment, nacosRegistration);
            // im socket服务注册到nacos
            registerImSocketServer(nacosRegistration);
            // im服务启动成功，保存服务信息到数据库
            saveImServer();
            SpringContextUtil.getBean(SyncMessageConsumer.class);
        } catch (Exception e) {
            log.error("IM服务启动失败。exception: {}", e);
            exit(run);
        }
        log.info("IM服务启动完成");
    }


    private static void registerImSocketServer(NacosRegistration nacosRegistration) throws NacosException {
        NacosDiscoveryProperties nacosDiscoveryProperties = nacosRegistration.getNacosDiscoveryProperties();
        Properties imNacosProperties = new Properties();
        imNacosProperties.setProperty("serverAddr", nacosDiscoveryProperties.getServerAddr());
        imNacosProperties.setProperty("namespace", nacosDiscoveryProperties.getNamespace());
        imNacosProperties.setProperty("secretKey", nacosDiscoveryProperties.getSecretKey());
        imNacosProperties.setProperty("username", nacosDiscoveryProperties.getUsername());
        imNacosProperties.setProperty("namingLoadCacheAtStart", nacosDiscoveryProperties.getNamingLoadCacheAtStart());
        imNacosProperties.setProperty("clusterName", nacosDiscoveryProperties.getClusterName());
        imNacosProperties.setProperty("password", nacosDiscoveryProperties.getPassword());
        imNacosProperties.setProperty("accessKey", nacosDiscoveryProperties.getAccessKey());
        imNacosProperties.setProperty("endpoint", nacosDiscoveryProperties.getEndpoint());
        NamingService namingService = NamingFactory.createNamingService(imNacosProperties);
        Instance instance = new Instance();
        instance.setIp(nacosRegistration.getHost());
        instance.setPort(JimConfig.globalImServerConfig.getBindPort());
        instance.setWeight(1.0D);
        instance.setClusterName(nacosDiscoveryProperties.getClusterName());
        instance.getMetadata().putAll(nacosDiscoveryProperties.getMetadata());
        namingService.registerInstance("im", nacosDiscoveryProperties.getGroup(), instance);
    }

    /**
     * 保存服务信息到数据库
     */
    private static void saveImServer() {
        ImServerInfoService imServerInfoService = SpringContextUtil.getBean(ImServerInfoService.class);
        ServerInfo serverInfo = new ServerInfo().setServerId(ImClusterVO.CLIENT_ID).setUpdateTime(LocalDateTime.now()).setHeartbeatTime(LocalDateTime.now());
        imServerInfoService.save(serverInfo);
    }

    private static void exit(ConfigurableApplicationContext context) {
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

}
