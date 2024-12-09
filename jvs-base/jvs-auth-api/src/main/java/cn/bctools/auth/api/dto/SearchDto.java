package cn.bctools.auth.api.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@ApiModel("搜索对象")
public class SearchDto {
    @ApiModelProperty("搜索的类型")
    SearchType type;
    @ApiModelProperty("指定范围搜索，只有在低代码中使用")
    DeptRangTypeDto rangType;
    @ApiModelProperty("指定范围搜索，只有在低代码中使用，传递的具体的范围值,真实的值只有一个")
    List<String> rangIds;
    @ApiModelProperty("搜索的值")
    String value;
    @ApiModelProperty("页大小")
    long size;
    @ApiModelProperty("页码数")
    long current = 1;
}
