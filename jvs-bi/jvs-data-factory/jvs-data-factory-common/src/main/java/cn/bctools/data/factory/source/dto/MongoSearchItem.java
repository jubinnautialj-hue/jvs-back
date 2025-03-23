package cn.bctools.data.factory.source.dto;

import cn.bctools.design.use.api.enums.DataModelQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MongoSearchItem {

    String key;

    Object value;

    String type;

    DataModelQueryType queryType;

    @ApiModelProperty("true and false or")
    Boolean andOr;
}
