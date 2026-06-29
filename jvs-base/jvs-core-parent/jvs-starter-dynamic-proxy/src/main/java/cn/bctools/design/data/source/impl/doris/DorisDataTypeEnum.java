package cn.bctools.design.data.source.impl.doris;

import cn.bctools.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * The enum Doris data type enum.
 *
 * @Author: ZhuXiaoKang
 * @Description: Java类型映射Doris数据库数据类型
 */
@Getter
@AllArgsConstructor
public enum DorisDataTypeEnum {
    /**
     * String doris data type enum.
     */
    STRING(String.class, "STRING"),
    /**
     * List doris data type enum.
     */
    LIST(Collection.class, "JSONB"),
    /**
     * Number doris data type enum.
     */
    NUMBER(Number.class, "STRING"),
    /**
     * Boolean doris data type enum.
     */
    BOOLEAN(Boolean.class, "BOOLEAN"),
    /**
     * Date doris data type enum.
     */
    DATE(Date.class, "DATETIME"),
    /**
     * Map doris data type enum.
     */
    MAP(Map.class, "JSONB"),
    ;

    private Class<?> javaType;
    private String dorisType;

    /**
     * Gets type.
     *
     * @param javaType the java type
     * @return the type
     */
    @SneakyThrows
    public static String getType(Class<?> javaType) {
        for (DorisDataTypeEnum value : DorisDataTypeEnum.values()) {
            if (value.getJavaType().isAssignableFrom(javaType)) {
                return value.getDorisType();
            }
        }
        throw new BusinessException("未知的数据类型");
    }
}
