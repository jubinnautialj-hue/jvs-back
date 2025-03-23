package cn.bctools.report.render.stats;

import cn.bctools.database.util.IdGenerator;
import cn.bctools.report.dto.GroupRecord;
import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.EStatsType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.UCellType;
import cn.bctools.report.model.univer.conf.stats.StatsField;
import cn.bctools.report.model.univer.conf.stats.StatsSetup;
import cn.bctools.report.model.univer.conf.stats.TotalSetup;
import cn.bctools.report.render.functions.FuncFactory;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(10)
@Component
@RequiredArgsConstructor
public class RowTotalStats implements BStats{

    private final FuncFactory funcFactory;

    @Override
    public void sort(List<UCell> groups) {
        ListUtil.sort(groups, UCell::compareTo);
        ListUtil.reverse(groups);
    }

    @Override
    public boolean status(UCell group) {
        return Optional.ofNullable(group).map(UCell::getCustom).map(UCellExpand::getRowTotalStatsSetup).map(StatsSetup::getEnabled).orElse(Boolean.FALSE);
    }

    @Override
    public List<UCell> calculation(UCell groupCell,final List<UCell> cells, List<List<UCell>> gridLinkedList, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos,List<Integer> copyRNos,List<Integer> copyCNos,List<Integer> newRowIndex,List<Integer> newColIndex) {
        StatsSetup rowTotalStatsSetup = groupCell.getCustom().getRowTotalStatsSetup();
        List<TotalSetup> setupList = rowTotalStatsSetup.getSetupList();
        String alias = rowTotalStatsSetup.getAlias();
        String newStyleKey = buildStyle(groupCell,rowTotalStatsSetup);

        EDataGrowthPlan dataGrowthPlan = groupCell.getCustom().getDataGrowthPlan();
        List<UCell> generateUCells = new ArrayList<>();
        Integer groupR = groupCell.getR();
        Integer groupC = groupCell.getC();
        switch (dataGrowthPlan){
            case HORIZONTAL:
                horizontal( gridLinkedList, groupRecords,rowNos, colNos, copyCNos, setupList, groupR, groupC, newStyleKey, alias, generateUCells,newRowIndex,newColIndex);
                break;
            case VERTICAL:
                vertical( gridLinkedList, groupRecords, rowNos, colNos, copyRNos, groupR, groupC, setupList, newStyleKey, alias, generateUCells,newRowIndex,newColIndex);
                break;
        }
        return generateUCells;
    }

    /**
     * 横向拓展 行总计
     * @param gridLinkedList 设计块生成的单元格组成的二维链表
     * @param groupRecords 合并项
     * @param rowNos 行号列表
     * @param colNos 列号列表
     * @param copyCNos 复制的列的列号列表
     * @param newStyleKey 新的样式的id
     * @param alias 别称
     * @param generateUCells 生成的单元格
     * @param newRowIndex 生成的行的下标 列表
     * @param newColIndex 生成的列的下标 列表
     */
    private void horizontal(
                            List<List<UCell>> gridLinkedList,
                            Map<String, GroupRecord> groupRecords,
                            List<Integer> rowNos,
                            List<Integer> colNos,
                            List<Integer> copyCNos,
                            List<TotalSetup> setupList,
                            Integer groupR,
                            Integer groupC,
                            String newStyleKey,
                            String alias,
                            List<UCell> generateUCells,
                            List<Integer> newRowIndex,
                            List<Integer> newColIndex) {
        //获取上一级的分组
        Map<Integer, TotalSetup> setupMap = setupList.stream().collect(Collectors.toMap(TotalSetup::getFirstR, Function.identity(), (v1, v2) -> v1));
        List<GroupRecord> collect = groupRecords.values()
                .stream()
                .filter(e -> EDataGrowthPlan.HORIZONTAL.equals(e.getDataGrowthPlan()))
                .sorted(Comparator.comparing(GroupRecord::getR))
                .collect(Collectors.toList());
        List<Integer> groupRIndex = collect.stream().map(GroupRecord::getR).distinct().collect(Collectors.toList());
        int index = groupRIndex.indexOf(groupR);
        int menuIndex = rowNos.indexOf(groupR);
        if(index>0){
            GroupRecord groupRecord = collect.get(index - 1);
            List<Integer> groupList = groupRecord.getGroupList();
            int startCIndex = colNos.indexOf(groupC);
            int startC = groupC;
            for (Integer scopeCount : groupList) {
                int endC = startC+scopeCount;
                int finalStartC = startC;
                long count = copyCNos.stream().filter(e -> e >= finalStartC && e < endC).count();
                int endCIndex = startCIndex+scopeCount+(int)count;

                //复制的列数据
                List<UCell> cloneColData = gridLinkedList.stream().map(e -> e.get(endCIndex - 1)).map(UCell::clone).collect(Collectors.toList());

                AtomicInteger atoRIndex = new AtomicInteger();
                int finalStartCIndex = startCIndex;
                cloneColData.forEach(clone -> {
                    int rIndex = atoRIndex.getAndIncrement();
                    clone.setS(newStyleKey);
                    if(rIndex<menuIndex){
                    }else if(rIndex==menuIndex){
                        clone.setV(alias).setT(UCellType.STRING);
                    }else{
                        clone.setV("--");
                        if(setupMap.containsKey(clone.getOR())){
                            TotalSetup totalSetup = setupMap.get(clone.getOR());
                            List<Integer> choseFieldsC = totalSetup.getFields().stream().map(StatsField::getC).collect(Collectors.toList());
                            List<UCell> rowData = gridLinkedList.get(rIndex);
                            List<Object> values = Arrays.stream(NumberUtil.range(finalStartCIndex, endCIndex - 1))
                                    .filter(e -> !newColIndex.contains(e))
                                    .mapToObj(rowData::get)
                                    .filter(e -> choseFieldsC.contains(e.getOC()))
                                    .map(UCell::getV)
                                    .collect(Collectors.toList());
                            Object calculation = funcFactory.apply(totalSetup.getStatsType(), values, totalSetup.getScale());
                            clone.setV(calculation).setT(UCellType.NUMBER);
                        }
                    }
                    gridLinkedList.get(rIndex).add(endCIndex,clone);
                });
                changeIndex(endCIndex,newColIndex);
                generateUCells.addAll(cloneColData);
                UCell first = CollectionUtil.getFirst(cloneColData);
                copyCNos.add(first.getC());
                startCIndex = endCIndex+1;
                startC = endC;
            }
        }else{

            AtomicInteger atoRIndex = new AtomicInteger();
            int size = CollectionUtil.getFirst(gridLinkedList).size();
            List<Integer> range = Arrays.stream(NumberUtil.range(0, size - 1)).filter(e -> !newColIndex.contains(e)).boxed().collect(Collectors.toList());

            List<UCell> cloneCells = gridLinkedList.stream().map(rowData -> {
                int rIndex = atoRIndex.getAndIncrement();
                UCell clone = CollectionUtil.getLast(rowData).clone();
                clone.setS(newStyleKey);
                if (rIndex >= menuIndex) {
                    if (rIndex == menuIndex) {
                        clone.setV(alias).setT(UCellType.STRING);
                    } else {
                        clone.setV("--");
                        if (setupMap.containsKey(clone.getOR())) {
                            TotalSetup totalSetup = setupMap.get(clone.getOR());
                            List<Integer> choseFieldsC = totalSetup.getFields().stream().map(StatsField::getC).collect(Collectors.toList());
                            List<Object> values = range.stream().map(rowData::get).filter(e -> choseFieldsC.contains(e.getOC())).map(UCell::getV).collect(Collectors.toList());
                            Object calculation = funcFactory.apply(totalSetup.getStatsType(), values, totalSetup.getScale());
                            clone.setV(calculation).setT(UCellType.NUMBER);
                        }
                    }
                }
                rowData.add(clone);
                return clone;
            }).collect(Collectors.toList());
            generateUCells.addAll(cloneCells);
        }
    }

    /**
     * 纵向拓展 行总计
     * @param gridLinkedList 设计块生成的单元格组成的二维链表
     * @param groupRecords 合并项
     * @param rowNos 行号列表
     * @param colNos 列号列表
     * @param copyRNos 复制的行的行号列表
     * @param newStyleKey 新的样式的id
     * @param alias 别称
     * @param generateUCells 生成的单元格
     * @param newRowIndex 生成的行的下标 列表
     * @param newColIndex 生成的列的下标 列表
     */
    private void vertical(
                          List<List<UCell>> gridLinkedList,
                          Map<String, GroupRecord> groupRecords,
                          List<Integer> rowNos, List<Integer> colNos,
                          List<Integer> copyRNos,
                          Integer groupR,
                          Integer groupC,
                          List<TotalSetup> setupList,
                          String newStyleKey,
                          String alias,
                          List<UCell> generateUCells,
                          List<Integer> newRowIndex,
                          List<Integer> newColIndex) {
        //判断是否有上一行
        int parentR = groupR -1;
        //没有就新增
        if(!rowNos.contains(parentR)){
            List<UCell> collect2 = gridLinkedList.get(0).stream().map(UCell::clone).peek(e -> e.setR(parentR)).collect(Collectors.toList());
            gridLinkedList.add(0,collect2);
            int i = rowNos.indexOf(groupR);
            rowNos.add(i,parentR);
            changeIndex(i,newRowIndex);
        }
        String key = groupR +"_"+ groupC;
        int headIndex = rowNos.indexOf(parentR);
        if(groupRecords.containsKey(key)){
            GroupRecord groupRecord = groupRecords.get(key);
            List<Integer> groupList = groupRecord.getGroupList();
            for (TotalSetup totalSetup : setupList) {
                int startR = groupR;

                int groupCIndex = colNos.indexOf(groupC);

                List<UCell> cloneColumn = gridLinkedList.stream().map(e -> e.get(groupCIndex)).map(UCell::clone).peek(e -> e.setS(newStyleKey)).collect(Collectors.toList());

                Arrays.stream(NumberUtil.range(0, headIndex)).forEach(e -> {
                    UCell uCell = cloneColumn.get(e);
                    if(e==0){
                        uCell.setV(alias).setT(UCellType.STRING);
                    }else{
                        uCell.setV("--").setT(UCellType.STRING);
                    }
                });

                List<StatsField> fields = totalSetup.getFields();
                //总计选的字段下标集
//                List<Integer> fieldCIndex = fields.stream().map(StatsField::getC).map(colNos::indexOf).distinct().collect(Collectors.toList());
                List<Integer> fieldCList = fields.stream().map(StatsField::getC).collect(Collectors.toList());
                EStatsType statsType = totalSetup.getStatsType();
                Integer scale = totalSetup.getScale();
                int startRIndex = rowNos.indexOf(groupR);
                for (Integer scopeCount : groupList) {
                    int endR = startR+scopeCount;
                    int finalStartR = startR;
                    long count = copyRNos.stream().filter(e -> e < endR && e >= finalStartR).count();

                    int endRIndex = startRIndex+scopeCount+(int)count;

                    int[] range = NumberUtil.range(startRIndex, endRIndex-1);
                    //范围内的单元格
                    List<List<UCell>> scopeCells = Arrays.stream(range).filter(e -> !newRowIndex.contains(e)).mapToObj(gridLinkedList::get).collect(Collectors.toList());
                    List<Object> values = scopeCells.stream().map(e -> e.stream().filter(v -> fieldCList.contains(v.getOC())).map(UCell::getV).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());

                    Object calculation = funcFactory.apply(statsType, values, scale);

                    String customSign = IdGenerator.getIdStr(36);

                    for (int i = 0; i < range.length; i++) {
                        int colIndex = range[i];
                        UCell uCell = cloneColumn.get(colIndex);
                        uCell.getCustom().setCustomMergeSign(customSign).setIgnoreGroupChain(Boolean.TRUE);
                        if(i==0){
                            uCell.setV(calculation).setT(UCellType.NUMBER);
                        }else{
                            uCell.setV(null);
                        }
                    }
                    startR = endR;
                    startRIndex = endRIndex;
                }

                for (int i = 0; i < cloneColumn.size(); i++) {
                    gridLinkedList.get(i).add(cloneColumn.get(i));
                }

                int colCount = CollectionUtil.getFirst(gridLinkedList).size();
                changeIndex(colCount-1,newColIndex);
                generateUCells.addAll(cloneColumn);
            }
        }
    }
}
