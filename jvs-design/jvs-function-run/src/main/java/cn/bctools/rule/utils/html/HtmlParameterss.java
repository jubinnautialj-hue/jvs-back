package cn.bctools.rule.utils.html;


import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;

/**
 * @author guojing
 */
@Data
public class HtmlParameterss {
    /**
     * 常量名|变量名
     */
    private String key;
    /**
     * 是否是必填写
     */
    private Boolean necessity;
    /**
     * 输入框类型
     */
    private InputType inputType;
    /**
     * 数据解释
     */
    private String info;

}
