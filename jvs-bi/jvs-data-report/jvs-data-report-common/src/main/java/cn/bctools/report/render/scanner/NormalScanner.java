package cn.bctools.report.render.scanner;

import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.render.ERenderingType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.render.ScannerOrder;
import cn.bctools.report.utils.USheetUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基础扫描器
 * @author wl
 */
@Component
@Order(ScannerOrder.NORMAL)
public class NormalScanner implements BaseScanner{

    /**
     * 扫描相同拓展方向的单元格设置
     * 扫描后为单元格打上标记，下一次扫描会跳过这些被打了标记的单元格
     * 纵向拓展扫描规则：
     *  1. 拓展方向一致
     *  2、相同行且列号大于当前单元格的列号
     * 横向拓展扫描规则：
     *  1. 拓展方向一致
     *  2. 相同列且行号大于当前单元格的行号
     * @param oneDimensional 单元格设置 一维数组
     * @param cellData 单元格设置 map （方便查找）
     * @return 分组
     */
    @Override
    public List<RenderingGroup> scanner(List<UCell> oneDimensional, Map<Integer, Map<Integer, UCell>> cellData) {
        List<RenderingGroup> renderingGroups = new ArrayList<>();

        //过滤 值类型只为数据集或固定值
        List<UCell> finalAndFactory = oneDimensional.stream().filter(e -> USheetUtils.equalsValueType(e, EValueType.固定值, EValueType.空,EValueType.数据集)).collect(Collectors.toList());

        //循环过滤后的数据集
        while (finalAndFactory.stream().anyMatch(e -> !e.getCustom().getFlagged())) {
            UCell base = finalAndFactory
                    .stream()
                    .filter(e -> !e.getCustom().getFlagged())
                    .findFirst()
                    .orElse(null);
            //如果没有其他的基点则跳出循环
            if(base == null){
                break;
            }
            RenderingGroup renderingGroup = new RenderingGroup();
            renderingGroup.setBaseC(base.getC()).setBaseR(base.getR()).setBaseValueType(base.getCustom().getValueType()).setRenderingType(ERenderingType.NORMAL);
            //获取基点的数据拓展方向
            EDataGrowthPlan dataGrowthPlan = base.getCustom().getDataGrowthPlan();
            EValueType valueType = base.getCustom().getValueType();
            List<UCell> data = new ArrayList<>();
            data.add(base);
            //需要根据基点的数据拓展方向，查找子点
            switch (dataGrowthPlan){
                case VERTICAL:
                    Map<Integer, UCell> columnMap = cellData.get(base.getR());
                    int nextC = base.getC()+1;
                    while (columnMap.containsKey(nextC)){
                        UCell uCell = columnMap.get(nextC);
                        if(breakAny(uCell,valueType)){
                            break;
                        }
                        data.add(uCell);
                        nextC = uCell.getC()+1;
                    }
                    break;
                case HORIZONTAL:
                    int nextR = base.getR()+1;
                    while (cellData.containsKey(nextR)){
                        Map<Integer, UCell> rowMap = cellData.get(nextR);
                        if(!rowMap.containsKey(base.getC())){
                            break;
                        }
                        UCell uCell = rowMap.get(base.getC());
                        if(breakAny(uCell,valueType)){
                            break;
                        }
                        data.add(uCell);
                        nextR = uCell.getR()+1;
                    }
                    break;
            }
            //打标
            data.forEach(UCell::flag);
            renderingGroup.setCells(data);
            renderingGroups.add(renderingGroup);
        }
        return renderingGroups;
    }

    /**
     * 如果当前单元格已被标记 或 当前单元格的值类型与基点不一致，则跳出循环
     * @param uCell 当前单元格
     * @param valueType 基点单元格值类型
     * @return true 跳出循环 false下一次循环
     */
    private boolean breakAny(UCell uCell,EValueType valueType){
        return USheetUtils.isFlagged(uCell) || !USheetUtils.equalsValueType(uCell, valueType);
    }

    /**
     * 匹配数据集
     * @param base
     * @param reference
     * @return
     */
    private boolean matchingExecuteName(UCell base,UCell reference){
        if(!Optional.ofNullable(base).map(UCell::getCustom).map(UCellExpand::getField).isPresent()){
            return Optional.ofNullable(reference).map(UCell::getCustom).map(UCellExpand::getField).isPresent();
        }
        return !StrUtil.equals(base.getCustom().getField().getExecuteName(), reference.getCustom().getField().getExecuteName());
    }
}
