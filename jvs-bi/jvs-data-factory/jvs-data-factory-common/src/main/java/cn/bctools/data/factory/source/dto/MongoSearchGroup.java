package cn.bctools.data.factory.source.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MongoSearchGroup {

    List<MongoSearchItem> items;

    @ApiModelProperty("true and false or")
    Boolean andOr;

}
