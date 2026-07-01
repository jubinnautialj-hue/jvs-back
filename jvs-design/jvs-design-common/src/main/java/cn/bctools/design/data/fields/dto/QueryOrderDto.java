package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Sort;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("自定义排序")
public class QueryOrderDto {

    @ApiModelProperty("字段id")
    private String fieldKey;
    @ApiModelProperty("排序类型")
    private Sort.Direction direction;

}
