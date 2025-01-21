package cn.bctools.word.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;

import java.util.List;

/**
 * excel 变量名
 */
@Data
@Accessors(chain = true)
public class ExcelVariable {

    /**
     * 变量名
     */
    String name;

    /**
     * 格式数据
     */
    CellStyle cellStyle;

    /**
     * 如果是 list 才会有的 key
     */
    String listKey;

    /**
     * 数据类型, 字符，时间，布尔，数组，如果是数组，需要向下遍历填充数据
     */
    ExcelType type;

    /**
     * 具体的数据值
     */
    Object value;

    /**
     * 第几个sheet,从 0 开始
     */
    int sheet;

    /**
     * 行号，从 0 开始
     */
    int row;

    /**
     * 列号
     */
    int column;

    /**
     * 类型为数组的时候的大小
     */
    int size;

    public int getSize() {
        if (ExcelType.List.equals(type) && value instanceof List) {
            return ((List<?>) value).size();
        }
        return 0;
    }


    enum ExcelType {

        String(CellType.STRING),
        Date(CellType.NUMERIC),
        Number(CellType.NUMERIC),

        Boolean(CellType.BOOLEAN),

        List(CellType._NONE),
        ;

        public final CellType type;

        ExcelType(CellType type) {
            this.type = type;
        }
    }
}
