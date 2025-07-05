package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "Ai 工具")
@Accessors(chain = true)
public class AiToolsDto {
    @Schema(description = "属性")
    private List<AiToolsPropertiesDto> properties;
    @Schema(description = "工具名称，中文")
    private String name;
    @Schema(description = "此字段可以忽略")
    private String url;
    @Schema(description = "工具描述，需要包含大概的返回字段内容，如返回手机号、身份证号等")
    private String description;
    @Schema(description = "Id，进行随机生成，当 ai聊天中可以使用或使用到这个工具时，会自动调用此工具。并传递 id值，请不要使用下画线，为保留字符")
    private String id;
}
