package cn.bctools.data.factory.enums;

import cn.bctools.common.exception.BusinessException;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字段类型，数据模型的字段类型
 *
 * @author guojing
 */
@Getter
@AllArgsConstructor
public enum DataFieldTypeEnum {
    /**
     * 类型分类
     */
    BOOLEAN("布尔(BOOLEAN)", "BOOL, BOOLEAN与TINYINT一样，0代表false，1代表true", DataFieldTypeClassifyEnum.布尔, "BOOLEAN",null),
    TINYINT("数字(TINYINT)", "1字节有符号整数，范围[-128, 127]", DataFieldTypeClassifyEnum.数字, "TINYINT",null),
    SMALLINT("数字(SMALLINT)", "2字节有符号整数，范围[-32768, 32767]", DataFieldTypeClassifyEnum.数字, "SMALLINT",null),
    INT("数字(INT)", "4字节有符号整数，范围[-2147483648, 2147483647]", DataFieldTypeClassifyEnum.数字, "INT",null),
    BIGINT("数字(BIGINT)", "8字节有符号整数，范围[-9223372036854775808, 9223372036854775807]", DataFieldTypeClassifyEnum.数字, "BIGINT",null),
    LARGEINT("数字(LARGEINT)", "16字节有符号整数，范围[-2^127 + 1 ~ 2^127 - 1]", DataFieldTypeClassifyEnum.数字, "LARGEINT",null),
    FLOAT("数字(FLOAT)", "4字节浮点数", DataFieldTypeClassifyEnum.数字, "FLOAT",null),
    JSON("JSON(JSON)", "二进制 JSON 类型", DataFieldTypeClassifyEnum.Json, "JSON",null),
    DOUBLE("数字(DOUBLE)", "8字节浮点数", DataFieldTypeClassifyEnum.数字, "DOUBLE",null),
    DECIMAL("数字(DECIMAL)", "DECIMAL(M[,D])高精度定点数，M 代表一共有多少个有效数字(precision)，D 代表小数位有多少数字(scale)，有效数字 M 的范围是 [1, 38]，小数位数字数量 D 的范围是 [0, precision]. 默认值为 DECIMAL(9, 0)", DataFieldTypeClassifyEnum.数字, "DECIMAL(%s,%s)",null),
    DATE("时间(DATE)", "日期类型，目前的取值范围是['0000-01-01', '9999-12-31'], 默认的打印形式是'yyyy-MM-dd'", DataFieldTypeClassifyEnum.时间, "DATE","yyyy-MM-dd"),
    DATETIME("时间(DATETIME)", "DATETIME([P]) 日期时间类型，可选参数P表示时间精度，取值范围是[0, 6]，即最多支持6位小数（微秒）。不设置时为0。 取值范围是['0000-01-01 00:00:00[.000000]', '9999-12-31 23:59:59[.999999]']. 打印的形式是'yyyy-MM-dd HH:mm:ss.SSSSSS'", DataFieldTypeClassifyEnum.时间, "DATETIME(%s)","yyyy-MM-dd HH:mm:ss"),
    CHAR("字符串(CHAR)", "CHAR(M)定长字符串，M代表的是定长字符串的字节长度。M的范围是1-255", DataFieldTypeClassifyEnum.字符串, "CHAR(%s)",null),
    VARCHAR("字符串(VARCHAR)", "VARCHAR(M) 变长字符串，M代表的是变长字符串的字节长度。M的范围是1-65533。注意：变长字符串是以UTF-8编码存储的，因此通常英文字符占1个字节，中文字符占3个字节。", DataFieldTypeClassifyEnum.字符串, "VARCHAR(%s)",null),
    STRING("字符串(STRING)", "STRING 变长字符串，默认支持1048576 字节（1MB），可调大到 2147483643 字节（2G）", DataFieldTypeClassifyEnum.字符串, "STRING",null),
    DATE_YEAR_MONTH("日期年月(INT)", "时间年月的封装-格式只能是yyyyMM", DataFieldTypeClassifyEnum.时间, "INT","yyyyMM"),
    DATE_YEAR("日期年(INT)", "时间年的封装yyyy", DataFieldTypeClassifyEnum.时间, "INT","yyyy"),
    DATE_MONTH("日期月(INT)", "时间月的封装MM", DataFieldTypeClassifyEnum.时间, "INT","MM");
    @EnumValue
    private final String value;
    private final String desc;
    private final DataFieldTypeClassifyEnum classifyEnum;
    /**
     * 建表语法
     */
    private final String createTable;
    private final String format;

    public static DataFieldTypeEnum value(String name) {
        for (DataFieldTypeEnum value : DataFieldTypeEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new BusinessException("未知类型");
    }

    /**
     * 是否为正常的时间  date与dateTime
     */
    public static boolean isNormalDate(DataFieldTypeEnum dataFieldTypeEnum) {
        return dataFieldTypeEnum.equals(DATE) || dataFieldTypeEnum.equals(DATETIME);
    }
}
