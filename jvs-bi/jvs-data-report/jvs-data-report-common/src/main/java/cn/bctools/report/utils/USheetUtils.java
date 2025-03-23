package cn.bctools.report.utils;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.report.dto.GroupRecord;
import cn.bctools.report.enums.EDataAggTech;
import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.ERangeType;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.UCellType;
import cn.bctools.report.model.univer.conf.UColumnData;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.model.univer.conf.URowData;
import cn.bctools.report.render.Occupied;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.render.stats.StatsFactory;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * sheet 工具
 * @author wl
 */
@Slf4j
public class USheetUtils {

    /**
     * 统计
     * 计算小计 合计
     * @param conf
     * @param cells
     */
    public static void stats(RenderingGroup conf,List<UCell> cells){
        List<UCell> mergeConf = conf
                .getCells()
                .stream()
                .filter(e -> EDataAggTech.GROUP.equals(e.getCustom().getDataAggTech()))
                .sorted(UCell::compareToOrigin)
                .collect(Collectors.toList());
        if(mergeConf.stream().noneMatch(e -> e.getCustom().isStats())){
            return;
        }
        /*
         * 拆分记录
         */
        //纵向拓展拆分记录
        List<GroupRecord> vRecords = new ArrayList<>();
        //横向拓展拆分记录
        List<GroupRecord> hRecords = new ArrayList<>();

        //循环分组设置
        for (UCell uCell : mergeConf) {
            EDataGrowthPlan dataGrowthPlan = uCell.getCustom().getDataGrowthPlan();
            List<UCell> collect = cells
                    .stream()
                    .filter(e -> uCell.getR().equals(e.getCustom().getOR()) && uCell.getC().equals(e.getCustom().getOC()))
                    .collect(Collectors.toList());

            List<Integer> prefix = getPrefix(dataGrowthPlan, vRecords, hRecords);

            //按前置分组记录拆分数据
            List<List<UCell>> partition = ListUtils.partition(collect, prefix);
            //统计出需要合并记录
            List<Integer> statistic = partition.stream().map(ListUtils::statistic).flatMap(Collection::stream).collect(Collectors.toList());

            UCell start;
            GroupRecord groupRecords;
            switch (dataGrowthPlan){
                case VERTICAL:
                    start = collect.stream().min(Comparator.comparing(UCell::getR)).orElse(uCell);
                    groupRecords = new GroupRecord().setDataGrowthPlan(dataGrowthPlan).setR(start.getR()).setC(start.getC()).setGroupList(statistic);
                    vRecords.add(groupRecords);
                    break;
                case HORIZONTAL:
                    start = collect.stream().min(Comparator.comparing(UCell::getC)).orElse(uCell);
                    groupRecords = new GroupRecord().setDataGrowthPlan(dataGrowthPlan).setR(start.getR()).setC(start.getC()).setGroupList(statistic);
                    hRecords.add(groupRecords);
            }
        }

        cells.sort(UCell::compareTo);

        List<Integer> rowNos = cells.stream().map(UCell::getR).distinct().sorted(Integer::compareTo).collect(Collectors.toList());
        List<Integer> colNos = cells.stream().map(UCell::getC).distinct().sorted(Integer::compareTo).collect(Collectors.toList());

        /*
         * 设计块生成的单元格改为二维链表
         * 主要用于小计 合计的时候插入单元格
         * 并且后续设置行列的时候 由于都是指向的同一个对象，可以利用这个特性设置单元格的行列
         */
        Map<Integer, Map<Integer, UCell>> blockCellMap = cells.stream()
                .collect(Collectors.groupingBy(UCell::getR, Collectors.toMap(UCell::getC, Function.identity(), (v1, v2) -> v2)));

        Map<Integer, Integer> rMapping = cells.stream().collect(Collectors.toMap(UCell::getR, UCell::getOR, (v1, v2) -> v1));
        Map<Integer, Integer> cMapping = cells.stream().collect(Collectors.toMap(UCell::getC, UCell::getOC, (v1, v2) -> v1));

        /*
        组成链表结构
         */
        List<List<UCell>> lists = new ArrayList<>();
        for (Integer rowNo : rowNos) {
            List<UCell> rowData = new ArrayList<>();
            Map<Integer, UCell> blockRowMap = blockCellMap.get(rowNo);
            for (Integer colNo : colNos) {
                UCell cell;
                if(blockRowMap.containsKey(colNo)){
                    cell = blockRowMap.get(colNo);
                }else {
                    cell = new UCell();
                    cell.setR(rowNo);
                    cell.setC(colNo);
                    cell.getCustom().setOR(rMapping.get(rowNo)).setOC(cMapping.get(colNo));
                }
                rowData.add(cell);
            }
            lists.add(rowData);
        }
        Map<String, GroupRecord> groupRecordMap = Stream.of(vRecords, hRecords).filter(CollectionUtil::isNotEmpty).flatMap(Collection::stream).collect(Collectors.toMap(e -> e.getR() + "_" + e.getC(), Function.identity()));


        List<UCell> allGenerateUCell = SpringContextUtil.getBean(StatsFactory.class).doCalculation(lists, cells, mergeConf, groupRecordMap, rowNos, colNos);

        cells.addAll(allGenerateUCell);
        reset(lists);
    }

    /**
     * 计算合并项
     * @param conf 分组设置
     * @param cells 分组计算出来的数据
     * @return 合并数据
     */
    public static List<URange> merge(RenderingGroup conf,List<UCell> cells){
        List<URange> mergeData = new ArrayList<>();

        List<UCell> mergeConf = conf
                .getCells()
                .stream()
                .filter(e -> EDataAggTech.GROUP.equals(e.getCustom().getDataAggTech()))
                .sorted(UCell::compareToOrigin)
                .collect(Collectors.toList());

        /*
         * 设计块生成的单元格改为二维链表
         * 主要用于小计 合计的时候插入单元格
         * 并且后续设置行列的时候 由于都是指向的同一个对象，可以利用这个特性设置单元格的行列
         */
        Map<Integer, List<UCell>> collect1 = cells.stream().sorted(Comparator.comparing(UCell::getR)).collect(Collectors.groupingBy(UCell::getR));
        //链表结构
        List<List<UCell>> lists = new ArrayList<>(collect1.values());

        //纵向分组记录 从左到右
        List<List<Integer>> vRecords = new ArrayList<>();
        //纵向分组 起始行 行号
        Integer vStartR = null;
        //横向分组记录 从上到下
        List<List<Integer>> hRecords = new ArrayList<>();
        //横向分组 起始列 列号
        Integer hStartC = null;

        for (UCell group : mergeConf) {
            EDataGrowthPlan dataGrowthPlan = group.getCustom().getDataGrowthPlan();
            //纵向拓展
            if(EDataGrowthPlan.VERTICAL.equals(dataGrowthPlan)){
                //得到这一列的数据
                Map<Integer, List<UCell>> collect = getColOrRow(group, lists,UCell::getC);

                for (Map.Entry<Integer, List<UCell>> entry : collect.entrySet()) {
                    List<UCell> currentColumns = entry.getValue();
                    ListUtil.sort(currentColumns,Comparator.comparing(UCell::getR));

                    //按前置分组记录拆分数据
                    List<Integer> last = CollectionUtil.getLast(vRecords);
                    if (vStartR!=null) {
                        Integer finalVStartR = vStartR;
                        currentColumns = currentColumns.stream().filter(e -> e.getR() >= finalVStartR).collect(Collectors.toList());
                    }
                    UCell first = CollectionUtil.getFirst(currentColumns);
                    vStartR = first.getR();

                    List<Integer> statistic;
                    if (currentColumns.stream().noneMatch(e -> e.getCustom().getIgnoreGroupChain())) {
                        List<List<UCell>> partition = ListUtils.partition(currentColumns, last);
                        //统计出需要合并记录
                        statistic = partition.stream().map(ListUtils::statistic).flatMap(Collection::stream).collect(Collectors.toList());
                        vRecords.add(statistic);
                    }else{
                        List<List<UCell>> partition = ListUtils.partition(currentColumns, null);
                        //统计出需要合并记录
                        statistic = partition.stream().map(ListUtils::statisticBySign).flatMap(Collection::stream).collect(Collectors.toList());
                    }
                    List<URange> verticalMerges = getVerticalMerges(statistic, first.getR(), first.getC());
                    mergeData.addAll(verticalMerges);
                }
            }
            //横向拓展
            if(EDataGrowthPlan.HORIZONTAL.equals(dataGrowthPlan)){
                Map<Integer, List<UCell>> collect = getColOrRow(group, lists,UCell::getR);
                for (Map.Entry<Integer, List<UCell>> entry : collect.entrySet()) {
                    List<UCell> currentRows = entry.getValue();
                    ListUtil.sort(currentRows,Comparator.comparing(UCell::getC));
                    //按前置分组记录拆分数据
                    List<Integer> last = CollectionUtil.getLast(hRecords);
                    if (hStartC!=null) {
                        Integer finalHStartC = hStartC;
                        currentRows = currentRows.stream().filter(e -> e.getC() >= finalHStartC).collect(Collectors.toList());
                    }
                    UCell first = CollectionUtil.getFirst(currentRows);
                    hStartC = first.getC();
                    List<Integer> statistic;
                    if (currentRows.stream().noneMatch(e -> e.getCustom().getIgnoreGroupChain())) {
                        List<List<UCell>> partition = ListUtils.partition(currentRows, last);
                        //统计出需要合并记录
                        statistic = partition.stream().map(ListUtils::statistic).flatMap(Collection::stream).collect(Collectors.toList());
                        hRecords.add(statistic);
                    } else {
                        List<List<UCell>> partition = ListUtils.partition(currentRows, null);
                        //统计出需要合并记录
                        statistic = partition.stream().map(ListUtils::statisticBySign).flatMap(Collection::stream).collect(Collectors.toList());
                    }
                    List<URange> horizontalMerges = getHorizontalMerges(statistic, first.getR(), first.getC());
                    mergeData.addAll(horizontalMerges);
                }
            }
        }
        return mergeData;
    }

    /**
     * 按行或按列分组
     * @param group
     * @param lists
     * @param groupFunc
     * @return
     */
    private static @NotNull Map<Integer, List<UCell>> getColOrRow(UCell group, List<List<UCell>> lists,Function<UCell, Integer> groupFunc) {
        Map<Integer, List<UCell>> collect = lists.stream()
                .map(e -> e.stream().filter(v -> group.getC().equals(v.getCustom().getOC()) && group.getR().equals(v.getCustom().getOR())).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .filter(e -> !e.getCustom().getMergeIgnore())
                .collect(Collectors.groupingBy(groupFunc));
        return collect;
    }

    private static void reset(List<List<UCell>> lists){
        Integer r = CollectionUtil.getFirst(CollectionUtil.getFirst(lists)).getR();
        AtomicInteger startR = new AtomicInteger(r);
        lists.forEach(e -> {
            Integer startC = CollectionUtil.getFirst(e).getC();
            AtomicInteger cIndex = new AtomicInteger(startC);
            int currentR = startR.getAndAdd(1);
            e.forEach(v -> {
                v.setR(currentR);
                v.setC(cIndex.getAndAdd(1));
            });
        });
    };

    /**
     * 获取横向合并数据
     * @return 合并范围记录
     */
    private static List<URange> getHorizontalMerges(List<Integer> groupList,int r,int c) {
        AtomicInteger startColumn = new AtomicInteger(c-1);
        return groupList.stream().map(e -> new URange()
                        .setRangeType(ERangeType.NORMAL.getType())
                        .setStartRow(r)
                        .setEndRow(r)
                        .setStartColumn(startColumn.addAndGet(1))
                        .setEndColumn(startColumn.addAndGet(e-1)))
                .collect(Collectors.toList());
    }

    /**
     * 获取纵向合并数据
     * @return 合并范围记录
     */
    private static List<URange> getVerticalMerges(List<Integer> groupList,int r,int c) {
        AtomicInteger startRow = new AtomicInteger(r-1);
        return groupList.stream().map(e -> new URange()
                        .setRangeType(ERangeType.NORMAL.getType())
                        .setStartRow(startRow.addAndGet(1))
                        .setEndRow(startRow.addAndGet(e-1))
                        .setStartColumn(c)
                        .setEndColumn(c))
                .collect(Collectors.toList());
    }

    /**
     * 获取前置分组
     * @param dataGrowthPlan 数据拓展方向
     * @param vRecords 纵向拓展分组记录
     * @param hRecords 横向拓展分组记录
     * @return 前置分组记录
     */
    private static List<Integer> getPrefix(EDataGrowthPlan dataGrowthPlan, List<GroupRecord> vRecords, List<GroupRecord> hRecords) {
        List<Integer> prefix = new ArrayList<>();
        switch (dataGrowthPlan){
            case VERTICAL:
                if(CollectionUtil.isNotEmpty(vRecords)){
                    prefix = CollectionUtil.getLast(vRecords).getGroupList();
                }
                break;
            case HORIZONTAL:
                if(CollectionUtil.isNotEmpty(hRecords)){
                    prefix = CollectionUtil.getLast(hRecords).getGroupList();
                }
        }
        return prefix;
    }

    /**
     * 比较值类型 是否包含
     * @param uCell 单元格
     * @param types 类型列表
     * @return true 类型列表中包含单元格的值类型
     */
    public static boolean equalsValueType(UCell uCell, EValueType... types){
        if(types==null || types.length==0){
            return false;
        }
        return Arrays.stream(types).anyMatch(e -> e.equals(uCell.getCustom().getValueType()));
    }

    /**
     * 判断单元格是否已被标记
     * @param uCell 单元格
     * @return 是否被标记
     */
    public static boolean isFlagged(UCell uCell){
        return Optional.ofNullable(uCell).map(UCell::getCustom).map(UCellExpand::getFlagged).orElse(false);
    }

    /**
     * 坐标转为引用
     * @param uCell
     * @return
     */
    public static String getRef(UCell uCell){
        Integer c = uCell.getC();
        String s = ExcelUtil.indexToColName(c);
        //行号需要加1 excel的引用地址行号以1开始
        return s+(uCell.getR()+1);
    }

    /**
     * 计算sheet 列宽
     * @param uCellExpands 单元格的拓展属性
     * @param original 原始列宽设置
     * @return 新的列宽设置
     */
    public static Map<Integer, UColumnData> calculateColumnData(List<UCellExpand> uCellExpands,Map<Integer, UColumnData> original){
        if(CollectionUtil.isEmpty(uCellExpands) || MapUtil.isEmpty(original)){
            return original;
        }
        Map<Integer, List<Integer>> collect = uCellExpands.stream().filter(e -> ObjectUtil.isNotNull(e.getOC())).collect(Collectors.groupingBy(UCellExpand::getOC, Collectors.mapping(UCellExpand::getC, Collectors.toList())));
        return collect.entrySet()
                .stream()
                .filter(e -> original.containsKey(e.getKey()))
                .map(USheetUtils::splitAndBuild).flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, e -> original.get(e.getValue()), (v1, v2) -> v1));
    }

    /**
     * 计算sheet 行高
     * @param uCellExpands 单元格的拓展属性
     * @param original 原始行高设置
     * @return 新的行高设置
     */
    public static Map<Integer, URowData> calculateRowData(List<UCellExpand> uCellExpands,Map<Integer, URowData> original){
        if(CollectionUtil.isEmpty(uCellExpands) || MapUtil.isEmpty(original)){
            return original;
        }
        Map<Integer, List<Integer>> collect = uCellExpands.stream().collect(Collectors.groupingBy(UCellExpand::getOR, Collectors.mapping(UCellExpand::getR, Collectors.toList())));
        return collect.entrySet()
                .parallelStream()
                .filter(e -> original.containsKey(e.getKey()))
                .map(USheetUtils::splitAndBuild).flatMap(Collection::parallelStream).collect(Collectors.toMap(Map.Entry::getKey, e -> original.get(e.getValue()), (v1, v2) -> v1));
    }

    /**
     * 计算sheet 行列设置 拆分和重建
     * @param entry 行 或则 列
     * @return 新的entry数据
     */
    private static Set<Map.Entry<Integer,Integer>> splitAndBuild(Map.Entry<Integer, List<Integer>> entry){
        Integer key = entry.getKey();
        List<Integer> value = entry.getValue();
        Map<Integer, Integer> map = new HashMap<>();
        value.stream().distinct().forEach(v -> map.put(v, key));
        return map.entrySet();
    }

    /**
     * 是否被占用
     * @param uCell 单元格
     * @param occupiedMap 占用记录 map
     * @return true单元格已被占用
     */
    public static boolean isOccupied(UCell uCell,Map<Integer, Map<Integer, Occupied>> occupiedMap){
        return occupiedMap.containsKey(uCell.getR()) && occupiedMap.get(uCell.getR()).containsKey(uCell.getC());
    }

    /**
     * 检查是否被占用
     * @param cells
     * @return
     */
    public static boolean isOccupied(List<UCell> cells){
        //获取占用对象
        Map<Integer, Map<Integer, Occupied>> occupiedMap = USheetContext.getOccupied();
        if(MapUtil.isEmpty(occupiedMap)){
            return false;
        }
        return cells.parallelStream().anyMatch(e -> isOccupied(e, occupiedMap));
    }

    /**
     * 单元格偏移
     * @param cells 设计块生成的单元格
     */
    public static void offset(RenderingGroup group,List<UCell> cells){
        Map<Integer, Map<Integer, Occupied>> occupied = USheetContext.getOccupied();

        //如果未被占用 则直接停止偏移逻辑
        if(isOccupied(cells)){
            //占用的最大行
            List<Integer> columnNums= cells.stream().map(UCell::getC).distinct().collect(Collectors.toList());
            Integer occupiedMaxR = occupied.entrySet()
                    .stream()
                    .filter(entry -> columnNums.parallelStream().anyMatch(columnNum -> entry.getValue().containsKey(columnNum)))
                    .max(Map.Entry.comparingByKey())
                    .map(Map.Entry::getKey).orElse(0);

            Integer minR = cells.stream().min(Comparator.comparing(UCell::getR)).map(UCell::getR).orElse(0);
            int offset = occupiedMaxR-minR+1;
            if(offset>0){
                cells.forEach(e -> e.setR(e.getR()+ offset));
            }
        }
    }

    /**
     * 获取单元格的fieldKey
     * 如果不为数据集 返回 null
     * @param uCell 单元格
     * @return fieldKey
     */
    public static String getUCellFieldKey(UCell uCell){
        if(EValueType.数据集.equals(uCell.getCustom().getValueType())){
            return uCell.getCustom().getField().getFieldKey();
        }
        return null;
    }

    /**
     * 空值替换
     * @return 替换后的值
     */
    public static Object nullReplacement(UCell uCell,Object value){
        Object nullReplaceValue = uCell.getCustom().getNullReplaceValue();
        /*
        如果空替换值不为null且不为空字符串
        并且 值为null或空字符串时
        会将 值改为空替换值
         */
        if(nullReplaceValue!=null && StrUtil.isNotBlank(nullReplaceValue.toString()) && (value==null || StrUtil.isBlank(value.toString()))){
            return nullReplaceValue;
        }
        return value;
    }

    /**
     * 字段类型转univer单元格类型
     * @param uCell
     * @return
     */
    public static Integer fieldType2CellType(UCell uCell){
        if(uCell==null){
            return UCellType.FORCE;
        }
        return Optional.of(uCell).map(UCell::getCustom).map(UCellExpand::getField).map(UField::getUCellT).orElse(uCell.getT());
    }

}
