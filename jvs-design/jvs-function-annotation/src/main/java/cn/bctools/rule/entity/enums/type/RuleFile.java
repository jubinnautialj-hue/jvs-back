package cn.bctools.rule.entity.enums.type;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class RuleFile {

    /**
     * 桶
     */
    @ApiModelProperty("桶")
    private String bucketName;
    /**
     * 文件名
     */
    @ApiModelProperty("文件名")
    private String fileName;
    private String name;
    /**
     * 原始文件名
     */
    @ApiModelProperty("原始文件名")
    private String originalName;
    /**
     * 文件格式
     */
    private String fileType;
    /**
     * 是否是预览，默认都是预览 true 为预览，false 为 下载
     */
    private OutputType outputType = OutputType.preview;
    /**
     * 模块
     */
    private String module;
    private String url;
    private String previewUrl;
    private String uid;
    private String status;
    /**
     * 文件大小
     */
    private Long size;

}
