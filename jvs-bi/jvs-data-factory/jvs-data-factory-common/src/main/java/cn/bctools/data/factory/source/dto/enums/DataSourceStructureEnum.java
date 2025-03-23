package cn.bctools.data.factory.source.dto.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * 数据类型
 * @author admin
 */
@Getter
@AllArgsConstructor
public enum DataSourceStructureEnum {

    STRING("STRING", "文本", String.class),
    NUMBER("NUMBER", "数字", BigDecimal.class),
    BOOLEAN("BOOLEAN", "布尔", Boolean.class),
    LIST("LIST", "数组", ArrayList.class),
    OBJECT("OBJECT", "未知", Object.class),
    MAP("MAP", "map", Map.class),
    DATETIME("DATETIME", "时间(时分秒)", LocalDateTime.class),
    DATE("DATE","时间", LocalDate.class);

    @EnumValue
    public final String value;
    public final String desc;
    public final Class<?> javaClass;
}
