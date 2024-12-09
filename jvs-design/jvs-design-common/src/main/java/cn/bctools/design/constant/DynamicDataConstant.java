package cn.bctools.design.constant;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 动态数据通用常量
 */
public class DynamicDataConstant {
    /**
     * 置空
     */
    public static final String DATA_EMPTY = "empty";

    /**
     * 数据置空
     *
     * @param javaClass
     * @return
     */
    public static Object getEmpty(Class<?> javaClass) {
        if (CharSequence.class.isAssignableFrom(javaClass)) {
            return DATA_EMPTY;
        }
        if (Collection.class.isAssignableFrom(javaClass)) {
            return Collections.EMPTY_LIST;
        }
        if (Map.class.isAssignableFrom(javaClass)) {
            return Collections.EMPTY_MAP;
        }
        if (Date.class.isAssignableFrom(javaClass)) {
            return null;
        }
        //其它类型设置为空时给定占位
        return DATA_EMPTY;
    }
}
