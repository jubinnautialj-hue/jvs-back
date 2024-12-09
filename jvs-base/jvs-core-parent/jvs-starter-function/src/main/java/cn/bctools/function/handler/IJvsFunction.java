package cn.bctools.function.handler;

import cn.bctools.function.entity.vo.ElementVo;

import javax.validation.constraints.NotNull;

/**
 * Jvs表达式-自定义函数列表
 * <p>
 * 实现类需要添加注解, 否则也不会生效{@link cn.bctools.function.handler.JvsExpression}
 *
 * @Author: GuoZi
 */
public interface IJvsFunction<T extends ElementVo> extends IJvsExpressionElement<T> {

    String NAME = "函数列表";

    /**
     * 函数计算
     *
     * @param functionName 函数名
     * @param data         参数
     * @return 计算结果
     */
    Object calculate(String functionName, @NotNull Object... data);

}
