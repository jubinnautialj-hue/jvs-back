package cn.bctools.report.model.univer.conf;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class URange {

    private Integer startRow;

    private Integer startAbsoluteRefType;

    private Integer endAbsoluteRefType;

    private Integer startColumn;

    private Integer rangeType = 0;

    private String sheetId;

    private String unitId;

    private Integer endRow;

    private Integer endColumn;
}
