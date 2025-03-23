package cn.bctools.bi.dto;

import cn.bctools.bi.entity.enums.TaskTypeEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@ApiModel("导出数据")
@Data
@Accessors(chain = true)
public class DownIdTypeDto {
    @ApiModelProperty("需要下载的数据id集合")
    private List<String> id;
    @ApiModelProperty("下载类型")
    private TaskTypeEnums type;
}
