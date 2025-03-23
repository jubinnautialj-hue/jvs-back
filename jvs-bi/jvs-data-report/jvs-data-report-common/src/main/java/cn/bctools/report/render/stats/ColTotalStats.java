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

@Order(20)
@Component
@RequiredArgsConstructor
public class ColTotalStats implements BStats{

    private final FuncFactory funcFactory;

    @Override
    public void sort(List<UCell> groups) {
        ListUtil.sort(groups, UCell::compareTo);
        ListUtil.reverse(groups);
    }

    @Override
    public boolean status(UCell group) {
        return Optional.ofNullable(group).map(UCell::getCustom).map(UCellExpand::getColTotalStatsSetup).map(StatsSetup::getEnabled).orElse(Boolean.FALSE);
    }

    @Override
    public List<UCell> calculation(UCell groupCell,final List<UCell> cells, List<List<UCell>> gridLinkedList, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos,List<Integer> copyRNos,List<Integer> copyCNos,List<Integer> newRowIndex,List<Integer> newColIndex) {

        List<UCell> generates = new ArrayList<>();
        StatsSetup colTotalStatsSetup = groupCell.getCustom().getColTotalStatsSetup();

        String alias = colTotalStatsSetup.getAlias();
        List<TotalSetup> setupList = colTotalStatsSetup.getSetupList();
        Map<Integer, TotalSetup> setupMap = setupList.stream().collect(Collectors.toMap(TotalSetup::getFirstC, Function.identity(), (v1, v2) -> v1));

        String newStyleKey = buildStyle(groupCell, colTotalStatsSetup);

        switch (groupCell.getCustom().getDataGrowthPlan()){
            case HORIZONTAL:
                horizontal(groupCell, gridLinkedList, groupRecords, rowNos, colNos, copyCNos, newStyleKey, alias, setupList, generates,newRowIndex,newColIndex);
                break;
            case VERTICAL:
                vertical(groupCell, gridLinkedList, groupRecords, rowNos,colNos, copyRNos, newStyleKey, alias, setupMap, generates,newRowIndex,newColIndex);
                break;
        }
        return generates;
    }

    /**
     * 纵向拓展 列总计
     * @param group 分组单元格
     * @param lists 设计块生成的单元格组成的二维链表
     * @param groupRecords 合并项
     * @param rowNos 行号列表
     * @param colNos 列号列表
     * @param copyRNos 复制的行的行号列表
     * @param newStyleKey 新的样式的id
     * @param alias 别称
     * @param setupMap 统计设置
     * @param generates 生成的单元格
     * @param newRowIndex 生成的行的下标 列表
     * @param newColIndex 生成的列的下标 列表
     */
    private void vertical(UCell group,
                          List<List<UCell>> lists,
                          Map<String, GroupRecord> groupRecords,
                          List<Integer> rowNos,List<Integer> colNos,
                          List<Integer> copyRNos,
                          String newStyleKey,
                          String alias,
                          Map<Integer, TotalSetup> setupMap,
                          List<UCell> generates,
                          List<Integer> newRowIndex,
                          List<Integer> newColIndex) {
        List<List<Integer>> groups = getGroups(group, groupRecords);
        List<Integer> collect = groupRecords.values()
                .stream()
                .filter(e -> EDataGrowthPlan.VERTICAL.equals(e.getDataGrowthPlan()))
                .filter(e -> e.getR().equals(group.getR()))
                .sorted(Comparator.comparing(GroupRecord::getC))
                .map(GroupRecord::getC).collect(Collectors.toList());
        Integer c = group.getC();

        int parentC = c-1;
        Integer startR = group.getR();
        int startIndex = rowNos.indexOf(startR);

        List<Integer> groupCounts = new ArrayList<>();
        int menuIndex = colNos.indexOf(c);
        if(collect.contains(parentC)){
            int i = collect.indexOf(parentC);
            groupCounts = groups.get(i);
        }else{
            int sum = groupRecords.get(group.getR() + "_" + group.getC()).getGroupList().stream().mapToInt(e -> e).sum();
            groupCounts.add(sum);
        }

        for (Integer groupCount : groupCounts) {
            int endR = startR+groupCount;
            Integer finalStartR = startR;
            long count = copyRNos.stream().filter(e -> e < endR && e >= finalStartR).count();
            int insertIndex = startIndex+groupCount+(int) count;
            AtomicInteger atoColIndex = new AtomicInteger();
            List<Integer> rowIndices = Arrays.stream(NumberUtil.range(startIndex, insertIndex - 1)).filter(e -> !newRowIndex.contains(e)).boxed().collect(Collectors.toList());

            List<UCell> cloneRowData = lists.get(insertIndex - 1)
                    .stream()
                    .map(UCell::clone)
                    .peek(e -> {
                        e.setS(newStyleKey);
                        e.getCustom().setCustomMergeSign(IdGenerator.getIdStr(36));
                        int colIndex = atoColIndex.getAndIncrement();
                        if(colIndex<menuIndex){
                            return;
                        }
                        if(colIndex == menuIndex){
                            e.setV(alias).setT(UCellType.STRING);
                            return;
                        }
                        Object v = "--";
                        if(setupMap.containsKey(e.getCustom().getOC()) || newColIndex.contains(colIndex)){
                            List<Object> values = rowIndices.stream().map(lists::get).map(o -> o.get(colIndex)).map(UCell::getV).collect(Collectors.toList());

                            EStatsType statsType = EStatsType.SUM;
                            int scale = 2;
                            if(setupMap.containsKey(e.getOC())){
                                TotalSetup totalSetup = setupMap.get(e.getOC());
                                statsType = totalSetup.getStatsType();
                                scale = totalSetup.getScale();
                            }
                            v = funcFactory.apply(statsType,values,scale);
                            e.setT(UCellType.NUMBER);
                        }
                        e.setV(v);
                    })
                    .collect(Collectors.toList());
            Integer r = CollectionUtil.getFirst(cloneRowData).getR();
            copyRNos.add(r);
            lists.add(insertIndex,cloneRowData);
            changeIndex(insertIndex,newRowIndex);
            startIndex = insertIndex+1;
            generates.addAll(cloneRowData);
            startR = endR;
        }
    }

    /**
     * 纵向拓展 列总计
     * @param group 分组单元格
     * @param gridLinkedList 设计块生成的单元格组成的二维链表
     * @param groupRecords 合并项
     * @param rowNos 行号列表
     * @param colNos 列号列表
     * @param generateColNos 复制的列的列号列表
     * @param newStyleKey 新的样式的id
     * @param alias 别称
     * @param setupList 统计设置
     * @param generates 生成的单元格
     * @param newRowIndex 生成的行的下标 列表
     * @param newColIndex 生成的列的下标 列表
     */
    private void horizontal(UCell group,
                                   List<List<UCell>> gridLinkedList,
                                   Map<String, GroupRecord> groupRecords,
                                   List<Integer> rowNos,
                                   List<Integer> colNos,
                                   List<Integer> generateColNos,
                                   String newStyleKey,
                                   String alias,
                                   List<TotalSetup> setupList,
                                   List<UCell> generates,
                                   List<Integer> newRowIndex,
                                   List<Integer> newColIndex) {

        Integer groupC = group.getC();
        Integer groupR = group.getR();
        String key = groupR+"_"+groupC;
        if(groupRecords.containsKey(key)){
            int parentC = groupC-1;
            //检查需要clone的列是否存在
            if(!colNos.contains(parentC)){
                int index = colNos.indexOf(groupC);
                //不存在得新增这一列
                gridLinkedList.forEach(list -> {
                    UCell first = CollectionUtil.getFirst(list);
                    UCell clone = first.clone();
                    clone.setC(parentC);
                    list.add(index,clone);
                });
                colNos.add(index,parentC);
                changeIndex(index,newColIndex);
            }

            GroupRecord groupRecord = groupRecords.get(key);
            List<Integer> groupList = groupRecord.getGroupList();
            int i = rowNos.indexOf(group.getR());
            List<UCell> cloneRowData = gridLinkedList.get(i).stream().map(UCell::clone).peek(e -> e.setS(newStyleKey)).collect(Collectors.toList());

            for (TotalSetup totalSetup : setupList) {
                List<StatsField> fields = totalSetup.getFields();
                EStatsType statsType = totalSetup.getStatsType();
                Integer scale = totalSetup.getScale();

                List<UCell> currentRowData = cloneRowData.stream().map(UCell::clone).peek(e ->e.setV("--").setT(UCellType.STRING)).collect(Collectors.toList());

                Integer startC = group.getC();
                int startCIndex = colNos.indexOf(startC);
                Arrays.stream(NumberUtil.range(0, startCIndex-1)).forEach(e -> {
                    UCell uCell = currentRowData.get(e);
                    if(e==0){
                        uCell.setV(alias).setT(UCellType.STRING);
                    }else{
                        uCell.setV("--").setT(UCellType.STRING);
                    }
                });
                for (Integer scopeCount : groupList) {
                    Integer endC = startC+scopeCount;
                    Integer finalStartC = startC;
                    long newCols = generateColNos.stream().filter(e -> e < endC && e >= finalStartC).count();
                    int endCIndex = startCIndex+scopeCount+(int)newCols;
                    //范围
                    int[] range = NumberUtil.range(startCIndex, endCIndex-1);

                    List<Integer> scopeCIndices = Arrays.stream(range).filter(e -> !newColIndex.contains(e)).boxed().collect(Collectors.toList());

                    AtomicInteger atoRIndex = new AtomicInteger();
                    List<Object> values = gridLinkedList.stream()
                            //排除新增的行
                            .filter(e -> !newRowIndex.contains(atoRIndex.getAndIncrement()))
                            //获取范围列中的数据
                            .map(e -> scopeCIndices.stream()
                                    .map(e::get)
                                    .collect(Collectors.toList()))
                            .flatMap(Collection::stream)
                            //获取指定的行
                            .filter(e -> fields.stream().anyMatch(field -> field.getR().equals(e.getOR()) && field.getC().equals(e.getOC())))
                            //获取值
                            .map(UCell::getV)
                            .collect(Collectors.toList());

                    Object calculation = funcFactory.apply(statsType,values,scale);

                    String customSign = IdGenerator.getIdStr(36);
                    for (int i1 = 0; i1 < range.length; i1++) {
                        int index = range[i1];
                        UCell uCell = currentRowData.get(index);
                        uCell.getCustom().setCustomMergeSign(customSign).setIgnoreGroupChain(Boolean.TRUE);
                        if(i1==0){
                            uCell .setV(calculation).setT(UCellType.NUMBER);
                        }else{
                            uCell .setV(null);
                        }
                    }
                    startC = endC;
                    startCIndex = endCIndex;
                }
                gridLinkedList.add(currentRowData);
                changeIndex(gridLinkedList.size()-1,newRowIndex);
                generates.addAll(currentRowData);
            }
        }
    }

    private List<List<Integer>> getGroups(UCell group, Map<String, GroupRecord> groupRecords){
        return groupRecords.values()
                .stream()
                .filter(e -> EDataGrowthPlan.VERTICAL.equals(e.getDataGrowthPlan()))
                .filter(e -> e.getR().equals(group.getR()))
                .sorted(Comparator.comparing(GroupRecord::getC))
                .map(GroupRecord::getGroupList).collect(Collectors.toList());
    }
}
