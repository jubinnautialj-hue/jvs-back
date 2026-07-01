package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.enums.DataQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
public class PageSearchHtml {

    @ApiModelProperty("查询类型")
    DataQueryType enabledQueryTypes;
    @ApiModelProperty("是否启用")
    Boolean disabled;
    @ApiModelProperty("查询类型")
    List<Object> dataFileterList;
    @ApiModelProperty("当前列表字段")
    String value;
    @ApiModelProperty("目标列表字段")
    String fieldKey;

}
