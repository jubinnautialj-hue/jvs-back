package cn.bctools.rule.tools.groovy;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author st
 */
@Inspect
@Accessors(chain = true)
@Data
public class GroovyDto {

    @ParameterValue(info = "脚本", type = InputType.groovy, explain = " def groovy(... x){  //函数体 return 1;  }; ", defaultValue = "def groovy(... x){  //函数体\n\n\n\n\n\n\n\n\n\n\n\n   return x[0]+x[1]; }; ")
    @NotNull(message = "脚本不能为空")
    public String content;

    @ParameterValue(info = "参数", type = InputType.list)
    @NotNull(message = "脚本不能为空")
    public List<Object> parameters;


}
