package cn.bctools.ai.config;

import cn.bctools.ai.dto.AiDocumentDto;
import cn.bctools.ai.interfaces.JvsAiInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@Configuration
public class AiConfig {

    public static final String ai_dynamic_tools ="ai_dynamic_tools";
    public static final String ai_dynamic_dataset ="ai_dynamic_tools";

    /**
     * 根据 token 获取用户有哪些知识库的权限或文档的权限,这里现在只有文档的权限
     *
     * @return
     */
    @ConditionalOnMissingBean
    @Bean
    public JvsAiInterface jvsAiInterface() {
        return new JvsAiInterface() {
            @Override
            public ArrayList<AiDocumentDto> documents(String token) {
                return new ArrayList<>();
            }
        };
    }

    /**
     * 添加元数据，自定义元数据
     */
    @Autowired
    public void metaData(ServiceInstance instance) {
        //标记有动态工具
        Map<String, String> metadata = instance.getMetadata();
        //标记动态工具
        metadata.put(ai_dynamic_tools, "true");
        //标记动态知识库
        metadata.put(ai_dynamic_dataset, "true");
    }

}
