package cn.bctools.rule.utils.html;

import lombok.Data;

import java.util.Map;

/**
 * @author guojing
 * @describe 具体的方法节点执行操作
 */
@Data
public class HtmlFunction {

    /**
     * 方法的信息
     */
    private HtmlFunctionInfo functionInfo;

    /**
     * 请求参数
     */
    private Map<String, Object> body;
}
