package cn.bctools.design.crud.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class IndexFields implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("索引名称")
    String name;
    @ApiModelProperty("是否允许重复")
    Boolean repetitionAllowed;
    @ApiModelProperty("索引字段")
    List<IndexField> fields;

}
