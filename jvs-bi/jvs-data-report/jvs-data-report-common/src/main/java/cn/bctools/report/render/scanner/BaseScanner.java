package cn.bctools.report.render.scanner;

import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.render.RenderingGroup;

import java.util.List;
import java.util.Map;

/**
 * 扫描器
 * @author wl
 */
public interface BaseScanner {

    /**
     * 扫描
     * @param oneDimensional 将所有的单元格平铺以后的数据 根据行列排序 从小到大
     * @param cellData 设计的单元格对象 Map<rowNum,Map<columnNum,UCell>>
     * @return 渲染组
     */
    List<RenderingGroup> scanner(List<UCell> oneDimensional,Map<Integer, Map<Integer, UCell>> cellData);

}
