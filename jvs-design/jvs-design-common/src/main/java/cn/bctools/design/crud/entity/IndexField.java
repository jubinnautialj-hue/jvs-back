package cn.bctools.design.crud.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class IndexField implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("字段名")
    String key;
    @ApiModelProperty("字段排序")
    SortEnum sort;
}
