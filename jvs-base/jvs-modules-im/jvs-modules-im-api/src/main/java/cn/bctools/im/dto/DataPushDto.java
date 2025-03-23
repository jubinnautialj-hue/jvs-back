package cn.bctools.im.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据推送dto
 */
@Data
@Accessors(chain = true)
@ApiModel("数据推送dto")
public class DataPushDto {

    @ApiModelProperty(value = "接收推送的通道标识（用户id）", required = true)
    @NotEmpty(message = "接收推送的通道标识（用户id）不能为空")
    private List<String> mark;

    @ApiModelProperty(value = "数据类型(格式：服务名_业务名)", required = true)
    @NotBlank(message = "数据类型不能为空")
    private String type;

    @ApiModelProperty(value = "数据(JSON)", required = true)
    @NotNull(message = "数据不能为空")
    private JSONObject content;
}
