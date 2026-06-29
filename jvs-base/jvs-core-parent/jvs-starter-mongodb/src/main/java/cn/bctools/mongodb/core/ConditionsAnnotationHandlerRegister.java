package cn.bctools.mongodb.core;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 条件相关注解的处理器注册
 *
 * @author xh
 */
public interface ConditionsAnnotationHandlerRegister {
    /**
     * Annotation 必须标准ConditionsAnnotation注解
     *
     * @param annotation
     * @param conditionsAnnotationHandler
     */
    void registerHandler(Class<? extends Annotation> annotation, ConditionsAnnotationHandler conditionsAnnotationHandler);

    /**
     * 返回的是一个copy,不是原注册Map
     *
     * @return
     */
    Map<Class<? extends Annotation>, ConditionsAnnotationHandler> getAllRegisteredHandler();

    /**
     * 对注解在字段上的MongoDbAnnotation进行处理
     *
     * @param annotation
     * @return
     */
    ConditionsAnnotationHandler getHandler(Class<? extends Annotation> annotation);

}
