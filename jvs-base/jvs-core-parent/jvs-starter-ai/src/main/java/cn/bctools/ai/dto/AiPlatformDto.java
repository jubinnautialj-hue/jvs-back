package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 模型信息
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AiPlatformDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "平台名称")
    public String name;

    @Schema(description = "模型列表")
    public List<AiModelDto> models;

}
