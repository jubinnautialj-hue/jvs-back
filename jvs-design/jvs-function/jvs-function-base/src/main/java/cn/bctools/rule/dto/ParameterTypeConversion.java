package cn.bctools.rule.dto;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;

import java.util.Arrays;
import java.util.Map;

/**
 * [Description]: 参数类型转换
 *
 * @author : xh
 */
public class ParameterTypeConversion {
    private static final String LIST_STR = "List";
    private static final String MAP_STR = "Map";

    public Object getVariable() {
        return variable;
    }

    public void setVariable(Object variable) {
        this.variable = variable;
    }

    private Object variable;

    public ParameterTypeConversion(Object variable, Object value, String name) {
        //如果为空直接返回
        if (value == null) {
            this.variable = value;
            return;
        }
        //需要把整个实体对象传过来，因为对象属性默认为null会造成空指针异常
        Arrays.stream(variable.getClass().getFields()).forEach(e -> {
            if (e.getName().equals(name)) {
                //两个类型相同直接赋值
                if (e.getType().getName().equals(e.getClass().getName())) {
                    this.variable = value;
                } else {
                    //如果类型不同
                    if (e.getType().getName().equals(String.class.getName())) {
                        this.variable = value.toString();
                    } else if (e.getType().getName().equals(Long.class.getName())) {
                        this.variable = Long.parseLong(value.toString());
                    } else if (e.getType().getName().equals(Float.class.getName())) {
                        this.variable = Float.parseFloat(value.toString());
                    } else if (e.getType().getName().equals(Integer.class.getName())) {
                        this.variable = Integer.parseInt(value.toString());
                    } else if (e.getType().getName().contains(MAP_STR)) {
                        String s;
                        if (value instanceof String) {
                            s = (String) value;
                        } else {
                            s = JSONUtil.toJsonStr(value);
                        }
                        this.variable = JSON.parseObject(s, Map.class);
                    } else if (e.getType().getName().contains(LIST_STR)) {
                        String s;
                        if (value instanceof String) {
                            s = (String) value;
                        } else {
                            s = JSON.toJSONString(value);
                        }
                        this.variable = JSON.parseArray(s, Object.class);
                    } else {
                        this.variable = value;
                    }
                }
            }
        });


    }
}
