package cn.bctools.rule.utils.html;


import cn.bctools.rule.entity.enums.ClassType;
import lombok.Data;

/**
 * @author guojing
 */
@Data
public class HtmlFunctionInfo {
    /**方法的名称*/
    private String functionName;
    /**方法返回的类型*/
    private ClassType returnType;
    /**方法的使用介绍*/
    private String content;
}
