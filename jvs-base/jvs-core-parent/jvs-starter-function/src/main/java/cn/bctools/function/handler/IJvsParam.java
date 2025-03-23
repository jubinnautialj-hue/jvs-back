package cn.bctools.function.handler;

import cn.bctools.function.entity.vo.ElementVo;

import java.util.Map;

/**
 * Jvs表达式-自定义参数列表
 * <p>
 * 实现类需要添加注解, 否则也不会生效{@link JvsExpression}
 *
 * @Author: GuoZi
 */
public interface IJvsParam<T extends ElementVo> extends IJvsExpressionElement<T> {

    String NAME = "参数列表";

    /**
     * 变量值获取
     *
     * @param paramName 变量名称
     * @param data      当前环境参数
     * @return 变量值
     */
    Object get(String paramName, Map<String, Object> data);

}
