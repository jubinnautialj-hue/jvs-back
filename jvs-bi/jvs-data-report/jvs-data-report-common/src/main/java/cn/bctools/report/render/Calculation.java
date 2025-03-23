package cn.bctools.report.render;

import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.URange;

import java.util.List;

/**
 * @author wl
 */
public interface Calculation {

    boolean check(RenderingGroup conf);

    /**
     * 填充sheet数据
     */
    List<UCell> data(RenderingGroup conf);

    /**
     * 填充合并单元格数据
     */
    List<URange> merge(RenderingGroup conf, List<UCell> cells);


    default void stats(RenderingGroup conf, List<UCell> cells){};

}
