package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataQueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataConditionTypeDto {

    @ApiModelProperty("字段集")
    FieldBasicsHtml fieldDto;
    @ApiModelProperty("支持的条件")
    List<DataQueryType> dataQueryTypes;
    @ApiModelProperty("选择的值，如果为空，则为空值则")
    List<Values> values;

    @Data
    @Accessors(chain = true)
    public static class Values {
        String name;
        String value;
    }

    public DataConditionTypeDto setTypes(DataQueryType... queryTypes) {
        this.dataQueryTypes = Arrays.asList(queryTypes.clone());
        return this;
    }

}
