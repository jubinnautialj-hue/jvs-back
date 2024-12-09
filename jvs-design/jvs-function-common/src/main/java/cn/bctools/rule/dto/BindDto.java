package cn.bctools.rule.dto;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 需要在节点执行完成后做变量复制时，集成此类即可。
 * 变量赋值在编程中是一种基本操作，用于将某个值存储到一个标识符（变量）中。这种操作在不同的编程场景中都有其独特的用途和重要性
 * 示例： 条件语句 执行完成后，循环执行完成后，或临时存储数据，或实现 i=i+1 等功能。
 * 提示消息的同时将入参值变更为其它数据
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class BindDto {

    @ParameterValue(info = "变量绑定赋值", necessity = false, explain = "根据条件动态修改当前节点连线前所有变量的数据值。</br>支持公式、字段、判断条件等等", type = InputType.bind)
    public List<BindBaseDto> bind;

    /**
     * 根据条件设置多个数据值
     */
    @ParameterValue(info = "变量赋值", necessity = false, type = InputType.assignment)
    public List<BindBaseDto> assignmentBind;

}
