package cn.bctools.design.rule.impl.rule.app.rule;

import cn.bctools.design.rule.impl.datamodel.DataModelLinkSelected;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AppRuleDto {

    @ParameterValue(info = "选择逻辑引擎", type = InputType.selected, explain = "选择当前应用下的其它逻辑引擎，不支持相互调用,可能会存在系统错误。", cls = ApiRuleSelected.class)
    @NotNull(message = "选择逻辑引擎不能为空")
    public String key;

    @ParameterValue(info = "请求业务数据", explain = "请在调用前校验具体参数是否完整，或在被调用逻辑中添加参数校验规则。",type = InputType.map)
    @NotNull(message = "请求业务数据不能为空")
    public Map<String, Object> body;
}
