package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
public class DataUtil {

    private DataUtil() {
    }

    /**
     * 解析数据，得到数据字段
     *
     * @see DataColumnDto
     *
     * @param dataObj 数据
     * @return
     */
    public static <T> List<DataColumnDto> getDataColumns(T dataObj) {
        if (ObjectNull.isNull(dataObj)) {
            return Collections.emptyList();
        }
        if (dataObj instanceof Collection) {
            Collection<?> dataArray = (Collection<?>) dataObj;
            return dataArray.stream()
                    .flatMap(data -> dataObjToColumn(data).stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return dataObjToColumn(dataObj);
    }

    /**
     * 数据转DataColumn
     *
     * @param obj 数据对象
     * @return
     */
    private static <T> List<DataColumnDto> dataObjToColumn(T obj) {
        if (obj instanceof DynamicUpdate) {
            return dataObjToColumn(((DynamicUpdate) obj).getUpdateData());
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>)obj;
            return map.entrySet().stream()
                    .map(entry -> new DataColumnDto()
                            .setColumnName(entry.getKey().toString())
                            // 可能存在null字段，默认设置该字段类型为String
                            .setJavaType(entry.getValue() != null ?  entry.getValue().getClass() : String.class))
                    .collect(Collectors.toList());
        }
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(declaredField -> Boolean.FALSE.equals(Modifier.isStatic(declaredField.getModifiers())))
                .map(declaredField ->  new DataColumnDto()
                        .setColumnName(declaredField.getName())
                        .setJavaType(declaredField.getType()))
                .collect(Collectors.toList());
    }


    /**
     * 将数据处理成sql可以执行的类型
     *
     * @param dataObj
     * @return
     */
    public static <T> Map<String, Object> data(T dataObj) {
        if (ObjectNull.isNull(dataObj)) {
            return Collections.emptyMap();
        }
        if (dataObj instanceof Collection) {
            Collection<?> dataList = (Collection<?>) dataObj;
            Collection<Map<String, Object>> dataMaps = dataList.stream().map(DataUtil::convertObject).collect(Collectors.toList());
            Map<String, Object> data = new HashMap<>(1);
            data.put(Constant.DATA, dataMaps);
            return data;
        }
        return convertObject(dataObj);
    }

    private static <T> Map<String, Object> convertObject(T obj) {
        if (obj instanceof DynamicUpdate) {
            return convertObject(((DynamicUpdate) obj).getUpdateData());
        }

        if (obj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>)obj;
            data.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && (entry.getValue() instanceof Collection || entry.getValue() instanceof Map))
                    .forEach(entry -> entry.setValue(JSON.toJSONString(entry.getValue())));
            return data;
        }

        Map<String, Object> data = new HashMap<>(obj.getClass().getDeclaredFields().length);
        Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(declaredField -> Boolean.FALSE.equals(Modifier.isStatic(declaredField.getModifiers())))
                .forEach(declaredField -> {
                    Object val;
                    boolean accessible = declaredField.isAccessible();
                    if (!accessible) {
                        declaredField.setAccessible(true);
                    }
                    try {
                       val = declaredField.get(obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("获取值异常：" + e);
                    } finally {
                        declaredField.setAccessible(accessible);
                    }
                    if (Collection.class.isAssignableFrom(declaredField.getType()) || Map.class.isAssignableFrom(declaredField.getType())) {
                        data.put(declaredField.getName(), JSON.toJSONString(val));
                    } else {
                        data.put(declaredField.getName(), val);
                    }
                });
        return data;
    }

}
