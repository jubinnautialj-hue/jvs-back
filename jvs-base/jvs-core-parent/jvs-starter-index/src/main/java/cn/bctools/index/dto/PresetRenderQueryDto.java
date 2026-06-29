package cn.bctools.index.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author jvs The type Preset render query dto.
 */
@Data
@Accessors(chain = true)
@ApiModel("设计")
public class PresetRenderQueryDto {

    @ApiModelProperty("组件类型")
    private String type;

    @ApiModelProperty("组件表单数据")
    private List<FormQueryParamsDto> queryParams;

    @ApiModelProperty("组件元数据")
    private Map<String,Object> componentMetaData;
}
