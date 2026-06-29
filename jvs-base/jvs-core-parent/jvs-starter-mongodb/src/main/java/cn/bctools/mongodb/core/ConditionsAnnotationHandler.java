package cn.bctools.mongodb.core;

import org.springframework.data.mongodb.core.query.Criteria;

/**
 * 对注解在字段上的MongoDbAnnotation进行处理
 *
 * @author xh
 * @see
 */
@FunctionalInterface
public interface ConditionsAnnotationHandler {
    /**
     * 对注解在字段上的MongoDbAnnotation进行处理
     *
     * @param criteria 提供的Criteria
     * @param value    给Criteria加上条件的value 具体的Criteria调用方法交给子类实现
     * @return
     */
    Criteria handler(Criteria criteria, Object value);
}
