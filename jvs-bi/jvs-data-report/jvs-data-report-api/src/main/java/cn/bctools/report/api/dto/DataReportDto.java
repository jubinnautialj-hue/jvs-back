package cn.bctools.report.api.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class DataReportDto {

    @ApiModelProperty("id")
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("权限")
    private List<JSONObject> role;
}
