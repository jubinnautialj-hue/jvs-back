package cn.bctools.report.render;

import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
@ApiModel("渲染组")
public class RenderingGroup implements Comparable<RenderingGroup>{

    @ApiModelProperty("组渲染类型")
    private ERenderingType renderingType;

    @ApiModelProperty("基点单元格行号")
    private int baseR;

    @ApiModelProperty("基点单元格列号")
    private int baseC;

    @ApiModelProperty(value = "基点单元格值类型")
    private EValueType baseValueType;

    @ApiModelProperty(value = "行偏移",notes = "当前组的基准单元格相对前一个组的单元格的行偏移量")
    private int offsetR;

    @ApiModelProperty(value = "列偏移",notes = "当前组的基准单元格相对前一个组的单元格的列偏移量")
    private int offsetC;

    @ApiModelProperty("单元格设置")
    private List<UCell> cells = new ArrayList<>();

    public void addCells(List<UCell> cell) {
        this.cells.addAll(cell);
    }

    @Override
    public int compareTo(@NotNull RenderingGroup o) {
        if(this.baseR==o.getBaseR() && this.baseC==o.getBaseC()){
            return 0;
        }
        if(this.baseR==o.getBaseR() && this.baseC<o.getBaseC()){
            return -1;
        }
        if(this.baseR<o.getBaseR()){
            return -1;
        }
        return 1;
    }
}
