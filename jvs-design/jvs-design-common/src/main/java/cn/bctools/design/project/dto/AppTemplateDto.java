package cn.bctools.design.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 应用模板
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("应用模板")
@Accessors(chain = true)
public class AppTemplateDto {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("应用名称")
    private String name;

    @ApiModelProperty("分类")
    private String type;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("版本应用的版本,前端传递  如果不一样的版本号，可能导致无法使用")
    private String version;

    @ApiModelProperty("LOGO")
    private String logo;

    @ApiModelProperty("描述图片")
    private List<String> imgs;

    @ApiModelProperty("[仅详情接口会返回该字段]应用数据")
    private String data;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("平台名称")
    private String platformName;
    @ApiModelProperty("平台Id")
    private String platformId;

    @ApiModelProperty("类型，设计里面每新增一个应用就保存一个")
    private Set<String> types;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
