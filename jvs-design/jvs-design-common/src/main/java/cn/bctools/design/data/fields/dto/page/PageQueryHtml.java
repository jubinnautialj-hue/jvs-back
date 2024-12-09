package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
public class PageQueryHtml {

    @ApiModelProperty("查询类型")
    String enabledQueryTypes;
    String fieldKey;
    String value;

}
