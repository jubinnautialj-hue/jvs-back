package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "ai工具的回调信息")
@Accessors(chain = true)
public class AiCallDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "请求参数")
    private Map<String, Object> body;
    @Schema(description = "工具名称，中文")
    private String name;
    @Schema(description = "Id，进行随机生成，当 ai聊天中可以使用或使用到这个工具时，会自动调用此工具。并传递 id值，请不要使用下画线，为保留字符")
    private String id;

}
