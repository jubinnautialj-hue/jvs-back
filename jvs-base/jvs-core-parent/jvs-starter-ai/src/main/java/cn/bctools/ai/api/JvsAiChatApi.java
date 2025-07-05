package cn.bctools.ai.api;

import cn.bctools.ai.dto.AiChatMessage;
import cn.bctools.ai.dto.AiPlatformDto;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 */
@FeignClient(value = "jvs-ai", contextId = "default-model")
public interface JvsAiChatApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/aichat/api/ai/model";

    /**
     * 获取模型列表信息
     *
     * @return
     */
    @GetMapping(PREFIX + "/models")
    R<List<AiPlatformDto>> models();

    /**
     * 通过模型直接调用
     * 由用户自己定义提示词和系统提示词，但不具备上下文信息。模型也可以自行选择
     * 可以根据用户提示词和提取字段。直接转结构化数据
     *
     * @param chatMessage 模型对象
     */
    @PostMapping(PREFIX + "/generate")
    R<Object> generate(@RequestBody AiChatMessage chatMessage);

}
