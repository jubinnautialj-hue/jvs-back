package cn.bctools.ai.config;

import cn.bctools.ai.dto.AiDocumentDto;
import cn.bctools.ai.interfaces.JvsAiInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * @author jvs
 */
@Slf4j
@Configuration
public class AiConfig {

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

}
