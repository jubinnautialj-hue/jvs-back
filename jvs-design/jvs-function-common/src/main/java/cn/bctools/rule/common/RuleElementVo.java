package cn.bctools.rule.common;

import cn.bctools.function.enums.JvsParamType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RuleElementVo {

    @ApiModelProperty("名称")
    public String name;

    @ApiModelProperty("详细描述")
    private String info;

    @ApiModelProperty("返回值类型")
    private JvsParamType jvsParamType;

    @ApiModelProperty("下级结构")
    List<RuleElementVo> children;

    @ApiModelProperty("返回值类型")
    private String jvsParamTypeName;

    public RuleElementVo(String name, String info, JvsParamType jvsParamType) {
        this.name = name;
        this.info = info;
        this.jvsParamType = jvsParamType;
    }
}
