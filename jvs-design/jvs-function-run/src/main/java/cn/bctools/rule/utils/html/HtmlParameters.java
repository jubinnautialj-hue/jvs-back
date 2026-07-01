package cn.bctools.rule.utils.html;


import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.RuleFunctionDtoParameter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HtmlParameters extends RuleFunctionDtoParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("defaultValue")
    private String defaultvalue;
    /**
     * 公式的id值
     */
    private String formula;
    /**
     * 公式的内容如果公式为空,则直接获取值，如果公式不为空，直接执行公式
     */
    private String formulaContent;
    @ApiModelProperty("属性结构定义")
    private List<RuleElementVo> customStructureBody;
}
