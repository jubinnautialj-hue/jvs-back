package cn.bctools.report.render.scanner;

import cn.bctools.report.enums.EDataAggTech;
import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.render.ERenderingType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.render.ScannerOrder;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 交叉组合扫描器
 *                       UCell(横向拓展 分组)
 *  UCell(向下拓展 分组)  UCell(向下拓展 列表 动态属性)
 */
@Component
@Slf4j
@Order(ScannerOrder.CROSS_TAB)
public class CrossTabScanner implements BaseScanner{

    /**
     * 交叉报表扫描器
     * 扫描规则：
     *  1. 确定作为基点的动态属性单元格 （动态属性：根据表头和目录进行交叉拓展的单元格）
     *  2. 扫描表头
     *      2.1 以当前基点为中心，向上查找 值类型为 固定值或数据集的连续单元格设置
     *      2.2 以当前基点为中心，向右查找子基点（子基点：也是动态属性单元格，但是只作为交叉拓展数据作用）
     *      2.3 查找子基点上的值类型为固定值的单元格
 *      3。扫描目录
     *      3.1 以当前基点作为中心，向左查找值类型为 数据集的连续单元格设置
 *      4. 扫描其他设置
     *      4.1 其他设置的扫描区块范围为 表头最小行号<=r<=基点行号 && 目录最小列号<=c<=基点列号
     *      4.2 单元格值类型 只为固定值
     * @param oneDimensional
     * @param cellData
     * @return
     */
    @Override
    public List<RenderingGroup> scanner(List<UCell> oneDimensional, Map<Integer, Map<Integer, UCell>> cellData) {
        //先扫描有动态属性的单元格
        List<UCell> dynamicList = oneDimensional.stream().filter(e -> !e.getCustom().getFlagged()).filter(e -> EDataGrowthPlan.CROSS_TAB.equals(e.getCustom().getDataGrowthPlan())).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(dynamicList)){
            return Collections.emptyList();
        }
        return dynamicList.stream().map(e -> {
            if (e.getCustom().getFlagged()) {
                return null;
            }
            //由当前基准进行拓展查询
            RenderingGroup renderingGroup = new RenderingGroup().setBaseValueType(EValueType.数据集).setRenderingType(ERenderingType.CROSS_TAB);

            //顶部分类
            List<UCell> tops = new ArrayList<>();
            findHead(e, cellData, tops);
            if (CollectionUtil.isEmpty(tops)) {
                return null;
            }
            renderingGroup.addCells(tops);
            //左侧分类
            List<UCell> lefts = new ArrayList<>();
            findLeft(e, cellData, lefts);
            if (CollectionUtil.isEmpty(lefts)) {
                return null;
            }
            renderingGroup.addCells(lefts);
            //动态属性
            List<UCell> crossTabs = new ArrayList<>();
            crossTabs.add(e);
            findCrossTabs(e, cellData, crossTabs);
            renderingGroup.addCells(crossTabs);
            ignoreEmptyCell(cellData,crossTabs,tops);
            //打标记
            renderingGroup.getCells().forEach(UCell::flag);
            UCell uCell = renderingGroup.getCells().stream().min(UCell::compareTo).orElse(null);
            if(uCell!=null){
                renderingGroup.setBaseR(uCell.getR()).setBaseC(uCell.getC());
            }else{
                renderingGroup.setBaseR(e.getR()).setBaseC(e.getC());
            }
            return renderingGroup;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     *
     * @param current
     * @param cellData
     * @param selected
     */
    private void findHead(UCell current,Map<Integer, Map<Integer, UCell>> cellData,List<UCell> selected){
        searchUpwards(current, EDataGrowthPlan.HORIZONTAL,cellData, selected,EValueType.空,EValueType.公式);
    }

    /**
     *
     * @param current
     * @param cellData
     * @param selected
     */
    private void findLeft(UCell current,Map<Integer, Map<Integer, UCell>> cellData,List<UCell> selected){
        Integer c = current.getC();
        Integer r = current.getR();

        Integer lastC = c-1;
        Map<Integer, UCell> columnMap = cellData.get(r);

        while (columnMap.containsKey(lastC)){
            UCell uCell = columnMap.get(lastC);
            if(uCell.getCustom().getFlagged()){
                break;
            }
            UCellExpand custom = uCell.getCustom();
            if(!EDataGrowthPlan.VERTICAL.equals(custom.getDataGrowthPlan())){
                break;
            }
            if(!EDataAggTech.GROUP.equals(custom.getDataAggTech())){
                break;
            }
            EValueType valueType = uCell.getCustom().getValueType();
            if(!EValueType.数据集.equals(valueType)){
                break;
            }
            selected.add(uCell);
            lastC = uCell.getC()-1;
        }
    }

    /**
     * 向上查找
     * @param current 当前单元格
     * @param dataGrowthPlan 限制拓展方向
     * @param cellData 单元格设置Map
     * @param selected 符合条件的单元格的容器
     * @param ignores 忽略的值类型
     */
    private void searchUpwards(UCell current,EDataGrowthPlan dataGrowthPlan,Map<Integer, Map<Integer, UCell>> cellData,List<UCell> selected,EValueType... ignores){
        Integer c = current.getC();
        Integer r = current.getR();
        Integer nextR = r-1;
        while(cellData.containsKey(nextR)){
            Map<Integer, UCell> columnConf = cellData.get(nextR);
            if(columnConf.containsKey(c)){
                UCell uCell = columnConf.get(c);
                if(uCell.getCustom().getFlagged()){
                    break;
                }
                EValueType valueType = uCell.getCustom().getValueType();
                if(Arrays.asList(ignores).contains(valueType)){
                    break;
                }
                if(valueType.equals(EValueType.数据集) && !dataGrowthPlan.equals(uCell.getCustom().getDataGrowthPlan())){
                    break;
                }
                selected.add(uCell);
                nextR = uCell.getR()-1;
            }else{
                break;
            }
        }
    }

    /**
     * 查询交叉拓展单元格 (动态单元格)
     * @param current 当前单元格
     * @param cellData 单元格map
     * @param selected
     */
    private void findCrossTabs(UCell current,Map<Integer, Map<Integer, UCell>> cellData,List<UCell> selected){
        Integer r = current.getR();
        Map<Integer, UCell> rowCellMap = cellData.get(r);
        Map<Integer, UCell> headRowMap = cellData.get(r - 1);
        boolean notEmpty = MapUtil.isNotEmpty(headRowMap);
        Integer nextC = current.getC()+1;
        while (rowCellMap.containsKey(nextC) && rowCellMap.get(nextC).getCustom().isCrossTable()){
            UCell uCell = rowCellMap.get(nextC);
            if(uCell.getCustom().getFlagged()){
                break;
            }
            EValueType valueType = uCell.getCustom().getValueType();
            if(!EValueType.数据集.equals(valueType) && !EValueType.公式.equals(valueType)){
                return;
            }
            //查询表头
            if(notEmpty && headRowMap.containsKey(nextC)){
                UCell head = headRowMap.get(nextC);
                selected.add(head);
            }
            selected.add(uCell);
            nextC = uCell.getC()+1;
        }
    }

    /**
     * 查询静态交叉表头
     * @param cellData 所有的单元格设置
     * @param minR 最小行
     * @param minC 最小列
     * @param maxR 最大行
     * @param maxC 最大列
     * @return 交叉表头
     */
    public List<UCell> findCrossTabMenus(Map<Integer, Map<Integer, UCell>> cellData,int minR,int minC,int maxR,int maxC){
        int[] rRange = NumberUtil.range(minR, maxR);
        int[] cRange = NumberUtil.range(minC, maxC);
        List<UCell> crossTabMenus = new ArrayList<>();
        for (int r : rRange) {
            if(!cellData.containsKey(r)){
                continue;
            }
            Map<Integer, UCell> colmunMap = cellData.get(r);
            for (int c : cRange) {
                if(!colmunMap.containsKey(c)){
                    continue;
                }
                UCell uCell = colmunMap.get(c);
                if(EValueType.数据集.equals(uCell.getCustom().getValueType()) ||
                 EValueType.公式.equals(uCell.getCustom().getValueType())){
                    continue;
                }
                crossTabMenus.add(uCell);
            }
        }
        return crossTabMenus;
    }

    /**
     * 忽略空的单元格
     * @param cellData
     * @param crossTabs
     * @param heads
     */
    private void ignoreEmptyCell( Map<Integer, Map<Integer, UCell>> cellData,List<UCell> crossTabs,List<UCell> heads){
        if(CollectionUtil.isEmpty(crossTabs) || CollectionUtil.isEmpty(heads)){
            return;
        }
        if(crossTabs.size()<=1){
            return;
        }
        List<Integer> cList = crossTabs.stream().map(UCell::getC).collect(Collectors.toList());

        Integer minC = CollectionUtil.min(cList);
        Integer maxC= CollectionUtil.max(cList);

        Integer minR = heads.stream().map(UCell::getR).min(Integer::compareTo).orElse(0);
        Integer maxR = CollectionUtil.getFirst(crossTabs).getR();

        for (int i = minR; i <=maxR; i++) {
            if(!cellData.containsKey(i)){
                continue;
            }
            for (int j = minC; j <=maxC; j++) {
                if(!cellData.get(i).containsKey(j)){
                    continue;
                }
                cellData.get(i).get(j).flag();
            }
        }
    }

}
