package cn.bctools.design;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import cn.bctools.rule.entity.enums.InputTypeTransformInterface;
import cn.bctools.rule.utils.RuleDesignUtils;
import com.xxl.job.core.config.annotation.EnableXxlJobExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Indexed;

/**
 * @author Administrator
 */
@Indexed
@EnableDiscoveryClient
@SpringBootApplication
@EnableXxlJobExecutor
@ComponentScan(basePackages = {"cn.bctools.design", "cn.bctools.rule", "cn.bctools.function"})
@EnableJvsMgrResourceServer
public class JvsDesignApplication {

    public static void main(String[] args) {
        //逻辑引擎需要初始化类型结构解析器 初始化自定义结构
        SpringApplication.run(JvsDesignApplication.class, args)
                .getBeansOfType(InputTypeTransformInterface.class)
                .values()
                .forEach(e -> RuleDesignUtils.inputTypeTransformInterfaceMap.put(e.name(), e));
    }

}
