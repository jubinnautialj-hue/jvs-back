package cn.bctools.report.model.univer.conf;

import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 由于xSplit ySplit 名称不规范 需要手动创建setter getter
 */
@Accessors(chain = true)
public class UFreeze implements Serializable {

    private static final long serialVersionUID = 2752113805474423848L;

    private Integer xSplit;

    private Integer ySplit;

    private Integer startRow;

    private Integer startColumn;

    public Integer getxSplit() {
        return xSplit;
    }

    public void setxSplit(Integer xSplit) {
        this.xSplit = xSplit;
    }

    public Integer getySplit() {
        return ySplit;
    }

    public void setySplit(Integer ySplit) {
        this.ySplit = ySplit;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public Integer getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(Integer startColumn) {
        this.startColumn = startColumn;
    }


}
