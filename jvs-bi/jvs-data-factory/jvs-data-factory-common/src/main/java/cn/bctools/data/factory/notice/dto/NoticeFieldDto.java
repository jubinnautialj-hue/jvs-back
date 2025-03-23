package cn.bctools.data.factory.notice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("可选字段")
public class NoticeFieldDto {

    @ApiModelProperty("字段名称")
    private String name;

    @ApiModelProperty("字段key")
    private String key;
}
