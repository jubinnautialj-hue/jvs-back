package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识文档
 * 一个知识库，有多个文档存在，并可以删除某一个文档
 *
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "知识文档")
@Accessors(chain = true)
public class AiDocumentDto implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "文档 id ， 新增时可以直接由客户端定义")
    private String id;

    /**
     * 知识库ID
     */
    @Schema(description = "向量库ID")
    private String datasetId;

    @Schema(description = "向量库名称")
    private String datasetName;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * 文件链接
     */
    @Schema(description = "文件链接")
    private String fileUrl;

    /**
     * 文档内容
     */
    @Schema(description = "以markdown 或文本，只有上传时传递此参数 为数据的内容")
    private String content;

}
