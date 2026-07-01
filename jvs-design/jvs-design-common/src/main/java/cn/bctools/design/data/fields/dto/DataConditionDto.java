package cn.bctools.design.data.fields.dto;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DataQueryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 筛选条件
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("筛选条件")
public class DataConditionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("字段标识")
    private String key;

    @ApiModelProperty("运算符")
    private DataQueryType operator;

    @ApiModelProperty("值")
    private Object value;

    public List<String> getValue() {
        if (ObjectNull.isNull(value)) {
            return Collections.emptyList();
        }
        if (value instanceof Collection) {
            return (List)value;
        }
        return Collections.singletonList(value.toString());
    }


}
