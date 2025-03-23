package cn.bctools.document.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.poi.hpsf.Thumbnail;

import java.util.Map;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文件转换")
public class OnlyOfficeFileTransitionVo {
    @ApiModelProperty("是否为异步 前端不传后端会直接覆盖")
    private Boolean async;
    @ApiModelProperty("文件类型 后缀名 后端直接获取")
    private String filetype;
    @ApiModelProperty("唯一key 后端生成")
    private String key;
    @ApiModelProperty("输出类型 后缀名")
    private String outputtype;
    @ApiModelProperty("输出名称 可以不用传")
    private String title;
    @ApiModelProperty("文件url")
    private String url;

    private Thumbnail thumbnail;

    @Data
    @Accessors(chain = true)
    public static class Thumbnail{

        private Boolean first = Boolean.TRUE;

        private Integer aspect;

        private Integer height;

        private Integer width;
    }
}
