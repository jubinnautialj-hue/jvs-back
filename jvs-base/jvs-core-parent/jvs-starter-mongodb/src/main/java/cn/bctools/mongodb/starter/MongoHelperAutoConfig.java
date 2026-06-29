package cn.bctools.mongodb.starter;

import cn.bctools.mongodb.core.*;
import cn.bctools.mongodb.core.annotation.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author xh
 */
@Lazy(value = false)
@Configurable
public class MongoHelperAutoConfig implements InitializingBean {

    private static Map<Class<? extends Annotation>, ConditionsAnnotationHandler> defaultConditions = new HashMap<>(8);
    private static Map<Class<? extends Annotation>, OperatorAnnotationHandler> defaultOperator = new HashMap<>(8);

    static {
        defaultConditions.put(Eq.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::is));
        defaultConditions.put(Gt.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::gt));
        defaultConditions.put(Gte.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::gte));
        defaultConditions.put(Lt.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::lt));
        defaultConditions.put(Lte.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::lte));
        defaultConditions.put(Ne.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::ne));
        defaultConditions.put(In.class, (criteria, value) -> skipNullHandler(criteria, value, Criteria::in));
        defaultConditions.put(Regex.class, (criteria, value) -> {
            if (value == null) {
                return criteria;
            }
            String strValue = value.toString();
            return skipNullHandler(criteria, strValue, Criteria::regex);
        });

    }

    static {
        defaultOperator.put(AndOperator.class, ((criteriaLeft, criteriaRight) -> skipNullHandler(criteriaLeft, criteriaRight, Criteria::andOperator)));
        defaultOperator.put(OrOperator.class, ((criteriaLeft, criteriaRight) -> skipNullHandler(criteriaLeft, criteriaRight, Criteria::orOperator)));
        defaultOperator.put(NorOperator.class, ((criteriaLeft, criteriaRight) -> skipNullHandler(criteriaLeft, criteriaRight, Criteria::norOperator)));
    }

    @Autowired
    ConditionsAnnotationHandlerRegister conditionsAnnotationHandlerRegister;
    @Autowired
    OperatorAnnotationHandlerRegister operatorAnnotationHandlerRegister;

    private static <T> Criteria skipNullHandler(Criteria criteria, T value, BiFunction<Criteria, T, Criteria> biFunction) {
        if (value == null) {
            return criteria;
        }
        if (value instanceof String) {
            String s = (String) value;
            if (!StringUtils.hasText(s)) {
                return criteria;
            }
        }
        if (value instanceof Collection) {
            Collection c = (Collection) value;
            if (c.isEmpty()) {
                return criteria;
            }
        }
        return biFunction.apply(criteria, value);
    }

    @ConditionalOnMissingBean
    @Bean
    public ConditionsAnnotationHandlerRegister conditionsAnnotationHandlerRegister() {
        return new DefaultConditionsAnnotationHandlerRegister();
    }

    @ConditionalOnMissingBean
    @Bean
    public OperatorAnnotationHandlerRegister operatorAnnotationHandlerRegister() {
        return new DefaultOperatorAnnotationHandlerRegister();
    }

    @ConditionalOnMissingBean
    @Bean
    public MongoHelper mongoDbHelper() {
        return new MongoHelper();
    }


    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) throws Exception {
        return new MongoTemplate(factory, mappingMongoConverter(factory));
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> list = new ArrayList<>();
        //自定义的类型转换器
        list.add(new BigDecimalToDecimal128Converter());
        //自定义的类型转换器
        list.add(new Decimal128ToBigDecimalConverter());
        return new MongoCustomConversions(list);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory) {
        DefaultDbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext());
        converter.setCustomConversions(customConversions());
        converter.afterPropertiesSet();
        return converter;
    }

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        return mappingContext;
    }


//
//    /**
//     * 开启mongodb单节点副本集的实现
//     *
//     * @param factory
//     * @return
//     */
//    @Bean
//    MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
//        return new MongoTransactionManager(factory);
//    }

    @Override
    public void afterPropertiesSet() {
        initConditionsAnnotationHandlerRegister(conditionsAnnotationHandlerRegister);
        initOperatorAnnotationHandlerRegister(operatorAnnotationHandlerRegister);
    }

    private void initOperatorAnnotationHandlerRegister(OperatorAnnotationHandlerRegister register) {
        defaultOperator.forEach((annotation, handler) -> {
            if (register.getHandler(annotation) == null) {
                register.registerHandler(annotation, handler);
            }
        });
    }

    private void initConditionsAnnotationHandlerRegister(ConditionsAnnotationHandlerRegister register) {
        defaultConditions.forEach((annotation, handler) -> {
            if (register.getHandler(annotation) == null) {
                register.registerHandler(annotation, handler);
            }
        });
    }

}
