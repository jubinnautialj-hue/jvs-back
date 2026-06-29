package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;

/**
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "搜索")
@Accessors(chain = true)
public class AiSearchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "知识库")
    private List<String> datasetIds;
    @Schema(description = "搜索内容")
    private String search;
    /**
     * 最大返回条数
     */
    @Schema(description = "最大返回条数")
    private Integer maxResults = 4;
    /**
     * 相似度 0.00~1.00
     */
    @Schema(description = "相似度")
    private double minScore = 0.8;


}
