package cn.bctools.rule.tools.groovy;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author st
 */
@Inspect
@Accessors(chain = true)
@Data
public class GroovyDto {

    @ParameterValue(info = "脚本", type = InputType.groovy, explain = "示例: def groovy(... x){  //函数体 return 1;  }; ", defaultValue = "def groovy(... x){ return x[0]+x[1]; }; ")
    @NotNull(message = "脚本不能为空")
    public String content;

    @ParameterValue(info = "参数", necessity = false,type = InputType.list)
    @NotNull(message = "参数不能为空")
    public List<Object> parameters = new ArrayList<>();

    @ParameterValue(info = "参数是否为数组", necessity = false, type = InputType.map, explain = "参数类型,参数名:false/true,true表示参数为数组,若不设置系统会自行判断当前函数体参数类型，可能会导致脚本执行失败，请与参数个数保持一致")
    public Map<String,Boolean> parameterTypes = new HashMap<>();



}
