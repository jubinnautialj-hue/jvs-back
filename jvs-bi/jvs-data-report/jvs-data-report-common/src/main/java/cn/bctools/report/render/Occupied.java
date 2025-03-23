package cn.bctools.report.render;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 占用对象
 */
@Data
@Accessors(chain = true)
public class Occupied {

    /**
     * 基点 行号
     */
    private Integer baseR;

    /**
     * 基点 列号
     */
    private Integer baseC;

    /**
     * 原始单元格行号
     */
    private Integer oR;

    /**
     * 原始单元格列号
     */
    private Integer oC;

    /**
     * 偏移后的行号
     */
    private Integer r;

    /**
     * 偏移后的列号
     */
    private Integer c;


}
