package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AiModelDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "模型id")
    String id;
    @Schema(description = "模型名称")
    String name;
    @Schema(description = "模型描述")
    String description;
    @Schema(description = "模型类型")
    TypeEnum type;

}
