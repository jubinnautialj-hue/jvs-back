package cn.bctools.design.data.fields.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据模型
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class DataModelDto implements Serializable {

    @ApiModelProperty("数据模型id")
    private String id;

    @ApiModelProperty("应用id")
    private String appId;

    @ApiModelProperty("应用id")
    private String name;

    @ApiModelProperty("数据集名")
    private String collectionName;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    @ApiModelProperty("字段列表")
    private List<FieldBasicsHtml> fieldList;

}
