package cn.bctools.common.utils;

import java.util.List;
import java.util.Map;

/**
 * 将对象转成map工具类，可以使用此方法，也可以使用其它工具类，不做强制要求
 *
 * @author administer
 */
public class BeanToMapUtils {

    /**
     * 将对象装换为map
     *
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        return BeanCopyUtil.beanToMap(bean);
    }

    /**
     * 将List<T>转换为List<Map<String, Object>>
     *
     * @param objList
     * @return
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
        return BeanCopyUtil.objectsToMaps(objList);
    }

}
