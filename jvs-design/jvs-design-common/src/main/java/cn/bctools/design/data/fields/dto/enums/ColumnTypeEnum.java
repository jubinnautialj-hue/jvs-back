package cn.bctools.design.data.fields.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <字段类型>
 *
 * @author auto
 **/
@Getter
@AllArgsConstructor
public enum ColumnTypeEnum implements Serializable {

    /**
     * tinyint
     **/
    TINYINT("tinyint", Integer.class),
    SMALLINT("smallint", Integer.class),
    INT("int", Integer.class),
    DOUBLE("double", Double.class),
    FLOAT("float", Float.class),
    BIGINT("bigint", Long.class),
    DECIMAL("decimal", BigDecimal.class),
    DATE("date", LocalDate.class),
    TIME("time", LocalTime.class),
    DATETIME("datetime", LocalDateTime.class),
    TIMESTAMP("timestamp", LocalDateTime.class),
    ENUM("enum", String.class),
    CHAR("char", String.class),
    TINYBLOB("tinyblob", String.class),
    VARCHAR("varchar", String.class),
    TEXT("text", String.class),
    MEDIUMTEXT("mediumtext", String.class),
    LONGTEXT("longtext", String.class),
    LONGBLOB("longblob", String.class),
    JSON("json", String.class),
    ;
    /**
     * 数据类型
     */
    private final String dataType;
    /**
     * Java数据类型
     */
    private final Class<?> javaType;
}
