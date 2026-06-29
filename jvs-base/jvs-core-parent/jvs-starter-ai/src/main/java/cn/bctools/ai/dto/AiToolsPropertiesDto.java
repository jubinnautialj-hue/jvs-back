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
public class AiToolsPropertiesDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "类型,这里只支持基础类型 String,Integer，不支持复杂类型")
    String type;
    @Schema(description = "key")
    String key;
    @Schema(description = "描述")
    String description;
    @Schema(description = "值")
    Object value;
    @Schema(description = "是否必填写")
    Boolean required;

}
