package cn.bctools.ai.api;

import cn.bctools.ai.dto.AiModelDto;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * The interface Jvs ai api.
 *
 * @author jvs
 */
@FeignClient(value = "jvs-ai", contextId = "default-model")
public interface JvsAiModelApi {

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
    R<Map<String, String>> models();

    /**
     * 通过模型直接调用
     * 由用户自己定义提示词和系统提示词，但不具备上下文信息。模型也可以自行选择
     * 可以根据用户提示词和提取字段。直接转结构化数据
     *
     * @param aiModelDto      模型对象
     * @param applicationName 应用名称
     */
    @PostMapping(PREFIX + "/generate")
    R<Object> generate(@RequestBody AiModelDto aiModelDto, @RequestHeader(SysConstant.APPLICATION_NAME) String applicationName);

}
