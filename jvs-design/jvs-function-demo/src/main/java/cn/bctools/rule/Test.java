package cn.bctools.rule;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("测试")
public class Test {
    @ApiModelProperty("姓名")
    String name;
}
