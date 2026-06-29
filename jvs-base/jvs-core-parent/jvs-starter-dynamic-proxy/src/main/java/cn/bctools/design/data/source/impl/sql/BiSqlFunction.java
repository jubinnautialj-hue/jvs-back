package cn.bctools.design.data.source.impl.sql;

/**
 * The interface Bi sql function.
 *
 * @param <T> 函数的第一个输入类型
 * @param <U> 函数的第二个输入类型
 * @param <E> 函数的第三个输入类型
 * @param <R> 函数的结果类型
 * @Author: ZhuXiaoKang
 * @Description: 接收3个参数并生成结果的函数
 */
@FunctionalInterface
public interface BiSqlFunction<T, U, E, R> {

    /**
     * Apply r.
     *
     * @param t the t
     * @param u the u
     * @param e the e
     * @return the r
     */
    R apply(T t, U u, E e);
}
