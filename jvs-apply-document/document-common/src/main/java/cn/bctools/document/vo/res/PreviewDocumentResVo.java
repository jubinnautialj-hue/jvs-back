package cn.bctools.document.vo.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库预览文档返回
 */
@Data
@ApiModel("知识库预览文档")
@Accessors(chain = true)
public class PreviewDocumentResVo {

    @ApiModelProperty(value = "是否已收藏")
    private Boolean collected;

    @ApiModelProperty(value = "只读或可编辑")
    private String type;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "前端用于存储其他字段(例如:脑图需要一个type类型存储）")
    private String otherJson;

    @ApiModelProperty(value = "作者姓名")
    private String author;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
