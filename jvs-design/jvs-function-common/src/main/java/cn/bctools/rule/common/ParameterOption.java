package cn.bctools.rule.common;

import cn.bctools.rule.cons.SHEnum;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class ParameterOption<T> {
    /**
     * 选项显示的值
     */
    String label;
    /**
     * 类型,前端组件的类型
     */
    String type;
    /**
     * 显示效果
     */
    SHEnum show;
    /**
     * 选项解释
     */
    T value;
    /**
     * 扩展
     */
    Object extend;
    /**
     * 文本描述框
     */
    String describeText;
    /**
     * 子项
     */
    List<ParameterOption> childList;

    public ParameterOption() {

    }

    public ParameterOption(String label, T value) {
        this.label = label;
        this.value = value;
    }
}
