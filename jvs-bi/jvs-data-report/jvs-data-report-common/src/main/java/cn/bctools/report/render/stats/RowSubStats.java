package cn.bctools.report.render.stats;

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

@Order(5)
@Component
@RequiredArgsConstructor
public class RowSubStats implements BStats{

    private final FuncFactory funcFactory;

    @Override
    public void sort(List<UCell> groups) {
        ListUtil.sort(groups, UCell::compareTo);
        ListUtil.reverse(groups);
    }


    @Override
    public boolean status(UCell group) {
        return Optional.ofNullable(group).map(UCell::getCustom).map(UCellExpand::getRowSubStatsSetup).map(StatsSetup::getEnabled).orElse(Boolean.FALSE);
    }

    @Override
    public List<UCell> calculation(UCell groupCell,final List<UCell> cells, List<List<UCell>> gridLinkedList, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos,List<Integer> copyRNos,List<Integer> copyCNos,List<Integer> newRowIndex,List<Integer> newColIndex) {
        List<UCell> generateCells = new ArrayList<>();
        StatsSetup rowSubStatsSetup = groupCell.getCustom().getRowSubStatsSetup();
        if(!rowSubStatsSetup.getEnabled()){
            return generateCells;
        }
        List<TotalSetup> setupList = Optional.ofNullable(rowSubStatsSetup.getSetupList()).orElse(new ArrayList<>()).stream().filter(e -> CollectionUtil.isNotEmpty(e.getFields())).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(setupList)){
            return generateCells;
        }
        EDataGrowthPlan dataGrowthPlan = groupCell.getCustom().getDataGrowthPlan();
        switch (dataGrowthPlan){
            case VERTICAL:
                vertical(groupCell,setupList,gridLinkedList,rowNos,colNos,generateCells,newRowIndex,newColIndex);
                break;
            case HORIZONTAL:
                horizontal(groupCell,setupList,gridLinkedList,groupRecords,rowNos,colNos,copyCNos,generateCells,newRowIndex,newColIndex);
                break;
        }
        return generateCells;
    }

    /**
     * 横向拓展 列小计
     * @param group
     * @param setupList
     * @param lists
     * @param colNos
     * @param generateCells
     * @return
     */
    private List<UCell> vertical(UCell group,List<TotalSetup> setupList,List<List<UCell>> lists, List<Integer> rowNos,List<Integer> colNos,List<UCell> generateCells,List<Integer> newRowIndex,List<Integer> newColIndex ) {
        StatsSetup rowSubStatsSetup = group.getCustom().getRowSubStatsSetup();
        /*
        新增一行并且记录这一行，行号在计算中可以为负数，reset时会进行计算
         */
        int copyIndex = rowNos.indexOf(group.getR());
        List<UCell> uCells = lists.get(copyIndex);
        List<UCell> cloneRowData = uCells.stream().map(UCell::clone).collect(Collectors.toList());
        Integer cloneR = CollectionUtil.getFirst(cloneRowData).getR();
        int parentR = cloneR-1;
        if(!rowNos.contains(parentR)){
            rowNos.add(copyIndex,parentR);
            cloneRowData.forEach(e -> e.setR(parentR));
            lists.add(copyIndex, cloneRowData);
            changeIndex(copyIndex,newRowIndex);
        }

        String newStyleKey = buildStyle(group, rowSubStatsSetup);

        String alias = rowSubStatsSetup.getAlias();

        Integer lastColNo = CollectionUtil.getLast(colNos)+1;
        colNos.add(lastColNo);

        int colCopyIndex = colNos.indexOf(group.getC());

        int headIndex = rowNos.indexOf(parentR);

        for (TotalSetup totalSetup : setupList) {
            List<StatsField> fields = totalSetup.getFields();
            List<Integer> fieldCList = fields.stream().map(StatsField::getC).collect(Collectors.toList());

            EStatsType statsType = totalSetup.getStatsType();
            Integer scale = totalSetup.getScale();
            Integer firstR = totalSetup.getFirstR();
            for (int i = 0; i < lists.size(); i++) {
                List<UCell> row = lists.get(i);
                UCell clone = row.get(colCopyIndex).clone();
                clone.getCustom().setMergeIgnore(Boolean.TRUE);

                clone.setS(newStyleKey);
                if(i==0){
                    clone.setV(alias).setT(UCellType.STRING);
                }else if (i<=headIndex){
                    clone.setV("--");
                }else{
                    clone.setV("--");
                    if(!newRowIndex.contains(i)){
                        if(clone.getOR().equals(firstR)){
                            clone.setS(newStyleKey);
                            AtomicInteger atoColIndex = new AtomicInteger();
                            //不是新增的列 并且 原始C 与设置相同
                            List<Object> values = row.stream().filter(e -> !newColIndex.contains(atoColIndex.getAndIncrement())).filter(e -> fieldCList.contains(e.getOC())).map(UCell::getV).collect(Collectors.toList());
                            Object calculation = funcFactory.apply(statsType, values, scale);
                            clone.setV(calculation).setT(UCellType.NUMBER);
                        }
                    }
                }
                row.add(clone);
                generateCells.add(clone);
            }
            int colCount = CollectionUtil.getFirst(lists).size();
            changeIndex(colCount-1,newColIndex);
        }
        return generateCells;
    }

    /**
     * 横向拓展 行小计
     * @param group
     * @param setupList
     * @param lists
     * @param groupRecords
     * @param rowNos
     * @param colNos
     * @param generateCells
     * @return
     */
    private List<UCell> horizontal(UCell group,
                                   List<TotalSetup> setupList,
                                   List<List<UCell>> gridLinkedList,
                                   Map<String, GroupRecord> groupRecords,
                                   List<Integer> rowNos,
                                   List<Integer> colNos,
                                   List<Integer> copyCNos,
                                   List<UCell> generateCells,
                                   List<Integer> newRowIndex,
                                   List<Integer> newColIndex) {
        String groupKey = group.getR()+"_"+group.getC();
        //分组数
        GroupRecord groupRecord = groupRecords.get(groupKey);

        StatsSetup rowSubStatsSetup = group.getCustom().getRowSubStatsSetup();
        String alias = rowSubStatsSetup.getAlias();
        List<Integer> groupList = groupRecord.getGroupList();

        Map<String, TotalSetup> setupMap = setupList.stream().collect(Collectors.toMap(e -> e.getFirstR()+"_"+e.getFirstC(), Function.identity(), (v1, v2) -> v1));

        String newStyleKey = buildStyle(group, rowSubStatsSetup);

        int startCIndex = colNos.indexOf(group.getC());
        int startC = group.getC();

        int headIndex = rowNos.indexOf(group.getR());

        for (Integer scopeCount : groupList) {
            int endC = startC+scopeCount;

            //统计范围内生成了多少列
            int finalStartC = startC;
            long count = copyCNos.stream().filter(e -> e < endC && e >= finalStartC).count();

            int endCIndex = startCIndex+scopeCount+ (int) count;

            //复制前一列的数据
            int cloneIndex = endCIndex-1;

            List<UCell> cloneColumns = gridLinkedList.stream().map(e -> e.get(cloneIndex)).map(UCell::clone).peek(e ->e.setS(newStyleKey)).collect(Collectors.toList());

            AtomicInteger atoRIndex = new AtomicInteger();

            int finalStartCIndex = startCIndex;
            cloneColumns.forEach(clone -> {
                int rIndex = atoRIndex.getAndIncrement();
                if(rIndex<headIndex){
                }else if(rIndex==headIndex){
                    clone.setV(alias).setT(UCellType.STRING);
                }else{
                    clone.setV("--");
                    String key = clone.getOR()+"_"+clone.getOC();
                    if(setupMap.containsKey(key)){
                        if(!newRowIndex.contains(rIndex)){
                            TotalSetup totalSetup = setupMap.get(key);
                            EStatsType statsType = totalSetup.getStatsType();
                            Integer scale = totalSetup.getScale();
                            List<Integer> scopeCIndices = Arrays.stream(NumberUtil.range(finalStartCIndex, endCIndex-1)).filter(e -> !newColIndex.contains(e)).boxed().collect(Collectors.toList());
                            List<UCell> rowData = gridLinkedList.get(rIndex);
                            List<Integer> choseFieldsC = totalSetup.getFields().stream().map(StatsField::getC).collect(Collectors.toList());
                            List<Object> values = scopeCIndices.stream().map(rowData::get).filter(e ->  choseFieldsC.contains(e.getOC())).map(UCell::getV).collect(Collectors.toList());
                            Object calculation = funcFactory.apply(statsType, values, scale);
                            clone.setV(calculation).setT(UCellType.NUMBER);
                        }
                    }
                }
                gridLinkedList.get(rIndex).add(endCIndex,clone);
            });
            changeIndex(endCIndex,newColIndex);

            generateCells.addAll(cloneColumns);

            Integer c = CollectionUtil.getFirst(cloneColumns).getC();
            copyCNos.add(c);

            //跳过生成的这一列
            startCIndex = endCIndex+1;
            startC = endC;
        }
        return generateCells;
    }
}
