package cn.bctools.rule.function;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.utils.TaskLogUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The interface Base custom function interface.
 *
 * @param <T> the type parameter
 * @author guojing
 * @describe 如果需要定义方法 ，必须实现此接口  所有的数据自动加载到数据库中。如果(此处理类的名字)有重复，会自动提示
 */
public interface BaseCustomFunctionInterface<T> {

    /**
     * The constant log.
     */
    TaskLogUtil LOG = new TaskLogUtil(BaseCustomFunctionInterface.class);

    /**
     * 根据请求参数获取参数值
     * 入参类型在实现类中进行定义
     * 执行这个方法前，会保证参数完整性，请在实体类上面加上完整性注解
     *
     * @param t      请求参数，根据类型匹配
     * @param params the params
     * @return 返回数据对象 object
     * @throws Exception 处理后的异常对象
     */
    Object execute(T t, Map<String, Object> params);

    /**
     * 异常执行操作回滚操作
     *
     * @param t the t
     */
    default void exceptionCallBack(T t) {
        //默认异常操作执行为空
    }

    /**
     * 校验参数是否正常
     * 此方法必须与注解 {@linkplain cn.bctools.rule.annotations.Inspect 同时使用}
     *
     * @param o the o
     */
    default void inspect(T o) {
    }

    /**
     * 1、节点是否允许自定义结构
     * 2、根据节点获取的参数，自动识别结构
     * 3、节点允许自定义结构，但不能自动识别结构的。
     * 4、不需要根据参数，直接返回固定结构
     *
     * @param o 能用查询条件
     * @return 返回这个数据结构的对象信息 list
     */
    default List<RuleElementVo> structureType(T o) {
        //如果是空，则默认返回的数据为当前节点，，返回为未知
        return null;
    }

    /**
     * 默认清空 value值为空的查询条件
     *
     * @param body the body
     */
    default void removeKey(Map<String, Object> body) {
        //删除多余字段
        Iterator<String> iterator = body.keySet().iterator();
        while (iterator.hasNext()) {
            Object o = body.get(iterator.next());
            if (ObjectNull.isNull(o)) {
                //如果是数组则不删除
                if (!(o instanceof List)) {
                    iterator.remove();
                }
            }
        }
    }

}
