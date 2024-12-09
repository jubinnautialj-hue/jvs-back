package cn.bctools.rule.utils.html;


import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class ResultDto {
    /**
     * 显示类型
     */
    InputType type;
    /**
     * 原始节点返回类型
     */
    ClassType classType;
    /**
     * 毫秒
     */
    Long time;
    /**
     * 具体的值
     */
    Object value;
    /**
     * 节点的错误消息
     */
    Object errorMessage;
    /**
     * 节点执行的名称
     */
    String functionName;
    /**
     * 输入变量
     */
    Map<String, Object> parameterIn;
    /**
     * 入参对象解释
     */
    Map<String, String> parameterInDesc;
    /**
     * 变量
     */
    Map<String, Object> parameter;
}
