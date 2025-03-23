package cn.bctools.document.po;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@ApiModel("源文件下载-富文本附件类型")
@Accessors(chain = true)
public class SourceFileDownTypePo {
    /**
     * 文件类型
     * 0 视频或者音频
     * 1 图片
     */
    private Integer type;
    /**
     * 文件url
     */
    private String url;
}
