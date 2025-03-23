package cn.bctools.screen.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@ApiModel("前端获取数据联动时返回的数据格式")
@Data
@Accessors(chain = true)
public class LinkDataGetDto {
    @ApiModelProperty("被绑定的 图表id")
    private String linkChatId;
    @ApiModelProperty("当前图表id")
    private String chatId;
    @ApiModelProperty("字段绑定的关系")
    private LinkDataDto linkDataDto;
}
