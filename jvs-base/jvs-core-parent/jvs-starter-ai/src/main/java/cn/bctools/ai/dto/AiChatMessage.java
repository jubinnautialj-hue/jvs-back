package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "平台 id，判断是否删除，如果删除")
    private String platformId;

    @Schema(description = "模型")
    public String model;

    @Schema(description = "场景,如果传递这个后，就使用固定的提示词")
    public String scene;

    @Schema(description = "用户提示词参数")
    public Map<String, Object> param;

    @Schema(description = "系统提示词")
    public String systemPrompt;

    @Schema(description = "用户提示词")
    public String userPrompt;

    @Schema(description = "需要提取的字段，中文")
    List<String> field;

    @Schema(description = "图片地址")
    String base64;

    @Schema(description = "只提取字段")
    public Boolean extract;


}
