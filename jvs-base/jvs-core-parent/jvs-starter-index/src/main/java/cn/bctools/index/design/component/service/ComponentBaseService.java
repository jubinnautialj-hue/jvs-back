package cn.bctools.index.design.component.service;

import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.enums.ComponentType;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.OptionsBase;
import cn.hutool.core.bean.BeanUtil;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 组件基础规范
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentBaseService<T extends ComponentBaseInfo, R extends OptionsBase, S extends FormQueryParamsBase> {

    /**
     * 生成卡片
     *
     * @param
     * @return t t
     */
    default T generate() {
        Class<? extends ComponentBaseInfo> aClass = ComponentType.serverClass((Class<? extends ComponentBaseService>) this.getClass().getInterfaces()[0]);
        ComponentBaseInfo bean = BeanUtil.toBean("", aClass);
        ComponentType componentType = ComponentType.bClass(aClass);
        bean.setType(componentType);
        bean.setName(componentType.getDesc());
        return (T) bean;
    }

    /**
     * 组件数据填充组装方法，s为声明的实体类对象信息，可直接进行强转
     *
     * @param body the body
     * @return r r
     */
    default R fillData(S body) {
        return null;
    }

    /**
     * 动态关联的属性选择值
     * 当前组件多个属性存在选择后动态渲染加一个属性值时，会触发此方法，需要在注解中编写此方法的关联规则(link)
     * 例：
     * 数据初始化的时候，也调用此方法进行数据第一次处理
     *
     * @param key          要获取的属性值
     * @param paramsDtoMap 全部的属性值
     * @return list list
     * @FormFormQuery(label = "aa", prop = "aa", type = FormAttributeTypeEnum.select, link = {"aa", "cc"})
     */
    default List<SelectedAttribute> fieldInitOrLink(String key, S paramsDtoMap) {
        return null;
    }

    /**
     * 用于临时存储实体类映射关系
     */
    Map<Class<? extends ComponentBaseService>, Class<? extends FormQueryParamsBase>> BASE_MAP = new HashMap<>();

    /**
     * 获取自定义属性类,
     * 此实体类通过注解描述参数类型和个数和获取方式,
     * 不需要实现
     *
     * @return the class
     */
    default Class<? extends FormQueryParamsBase> generateClass() {
        if (BASE_MAP.containsKey(this.getClass())) {
            return BASE_MAP.get(this.getClass());
        }
        Class<? extends FormQueryParamsBase> actualTypeArgument1 = null;
        try {
            Class<? extends ComponentBaseService> aClass = this.getClass();
            Type genericInterface = aClass.getGenericInterfaces()[0];
            Type[] actualTypeArguments = ((ParameterizedTypeImpl) genericInterface).getActualTypeArguments();
            Type actualTypeArgument = actualTypeArguments[0];
            actualTypeArgument1 = (Class<? extends FormQueryParamsBase>) actualTypeArgument;
            BASE_MAP.put(this.getClass(), actualTypeArgument1);
        } catch (Exception e) {
            BASE_MAP.put(this.getClass(), FormQueryParamsBase.class);
        }
        return actualTypeArgument1;
    }
}
