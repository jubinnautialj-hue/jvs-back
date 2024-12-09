package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Sort;

/**
 * @author zhuxiaokang
 * 列表数据排序设计
 */
@Data
@Accessors(chain = true)
public class PageSortDesignHtml {
    @ApiModelProperty("字段名")
    private String fieldName;
    @ApiModelProperty("字段key")
    private String fieldKey;
    @ApiModelProperty("排序方式")
    private Sort.Direction direction;
}
