package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;


/**
 * @author jvs
 * The type Set the theme configuration of our enterprises embedded page dto.
 */
@Data
@Accessors(chain = true)
public class SetTheThemeConfigurationOfOurEnterprisesEmbeddedPageDto {

    /**
     * The Options.
     */
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    /**
     * The Operator.
     */
    @ParameterValue(info = "执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

}
