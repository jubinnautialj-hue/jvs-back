package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;


/**
 * 知识库
 *
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "知识库")
@Accessors(chain = true)
public class AiDatasetDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;


    /**
     * 是否公开
     */
    @Schema(description = "是否公开，默认 false 不公开（需要动态校验权限），true 公开。")
    private Boolean publicFlag;

    /**
     * 文档列表
     */
    @Schema(description = "文档列表,只有权限时才返回此字段")
    private List<AiDocumentDto> documentDtos;

}
