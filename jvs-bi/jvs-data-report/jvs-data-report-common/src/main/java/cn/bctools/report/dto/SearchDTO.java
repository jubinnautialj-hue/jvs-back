package cn.bctools.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("报表查询dto")
public class SearchDTO {

    @ApiModelProperty("报表id")
    @NotBlank(message = "未选择报表")
    private String reportId;

    @ApiModelProperty("查询条件")
    private List<FieldItem> searchFields = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class FieldItem{

        @ApiModelProperty("数据集id")
        @NotBlank(message = "查询条件异常，未传入数据集id")
        private String executeName;

        @ApiModelProperty("字段key")
        @NotBlank(message = "查询条件异常，字段key为空")
        private String fieldKey;

        @ApiModelProperty("值")
        private Object value;
    }
}
