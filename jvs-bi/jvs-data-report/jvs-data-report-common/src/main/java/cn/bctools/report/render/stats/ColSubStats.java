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

/**
 * 列小计
 */
@Order(15)
@Component
@RequiredArgsConstructor
public class ColSubStats implements BStats{

    private final FuncFactory funcFactory;

    @Override
    public boolean status(UCell group) {
        return Optional.ofNullable(group).map(UCell::getCustom).map(UCellExpand::getColSubStatsSetup).map(StatsSetup::getEnabled).orElse(Boolean.FALSE);
    }

    @Override
    public void sort(List<UCell> groups) {
        ListUtil.sort(groups, UCell::compareTo);
        ListUtil.reverse(groups);
    }

    @Override
    public List<UCell> calculation(UCell groupCell,final List<UCell> cells, List<List<UCell>> gridLinkedList, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos,List<Integer> copyRNos,List<Integer> copyCNos,List<Integer> newRowIndex,List<Integer> newColIndex) {
        EDataGrowthPlan dataGrowthPlan = groupCell.getCustom().getDataGrowthPlan();
        if(EDataGrowthPlan.VERTICAL.equals(dataGrowthPlan)){
            return vertical(groupCell, gridLinkedList, groupRecords, rowNos,colNos, copyRNos,newRowIndex,newColIndex);
        }
        if(EDataGrowthPlan.HORIZONTAL.equals(dataGrowthPlan)){
            return horizontal(groupCell,gridLinkedList,rowNos,colNos,newRowIndex,newColIndex);
        }
        return new ArrayList<>();
    }

    /**
     * 纵向拓展 列小计
     * @param uCell
     * @param lists
     * @param groupRecords
     * @param rowNos
     * @param copyRNos
     * @return
     */
    private List<UCell> vertical(UCell uCell,  List<List<UCell>> lists, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos, List<Integer> copyRNos,List<Integer> newRowIndex, List<Integer> newColIndex) {
        List<UCell> generateCells = new ArrayList<>();
        //如果开启小计
        StatsSetup colSubStatsSetup = uCell.getCustom().getColSubStatsSetup();
        Map<Integer, TotalSetup> setupMap = colSubStatsSetup.getSetupList().stream().collect(Collectors.toMap(TotalSetup::getFirstC, Function.identity(),(v1,v2) -> v1));
        if(colSubStatsSetup.getEnabled()){

            String styleKey = buildStyle(uCell, colSubStatsSetup);
            //
            String key = uCell.getR()+"_"+ uCell.getC();
            //获取当前单元格的分组记录数据
            if(!groupRecords.containsKey(key)){
                return generateCells;
            }
            GroupRecord groupRecord = groupRecords.get(key);
            //当前设计块的生成的单元格 按行排序，并按列分组
            // 获取分组单元格的当前列
            Integer r = groupRecord.getR();
            Integer c = groupRecord.getC();
            List<Integer> groupList = groupRecord.getGroupList();
            int startIndex = rowNos.indexOf(r);
            Integer startR = r;
            int menuIndex = colNos.indexOf(c);
            for (int i = 0; i < groupList.size(); i++) {
                Integer scopeCount = groupList.get(i);
                int endR = startR+scopeCount;

                Integer finalStartR = startR;
                long count = copyRNos.stream().filter(e -> e < endR && e >= finalStartR).count();

                int endIndex = startIndex+scopeCount+(int) count;

                if(scopeCount>1){
                    int copyIndex = endIndex-1;
                    List<UCell> uCells = lists.get(copyIndex);

                    //范围内的所有行数据
                    List<List<UCell>> dataStore = Arrays.stream(NumberUtil.range(startIndex, endIndex - 1)).filter(e -> !newRowIndex.contains(e)).mapToObj(lists::get).collect(Collectors.toList());

                    AtomicInteger atoColIndex = new AtomicInteger();
                    //复制的行数据
                    List<UCell> cloneColumns = uCells.stream()
                            .map(UCell::clone)
                            .peek(e -> {
                                e.setS(styleKey);
                                e.getCustom().setCustomMergeSign(IdGenerator.getIdStr(36));
                                int colIndex = atoColIndex.getAndIncrement();
                                if(colIndex<menuIndex){
                                    return;
                                }
                                if(colIndex==menuIndex){
                                    e.setV(colSubStatsSetup.getAlias()).setT(UCellType.STRING);
                                    return;
                                }
                                Object v = "--";
                                if(setupMap.containsKey(e.getCustom().getOC()) || newColIndex.contains(colIndex)){
                                    List<Object> values;
                                    //如果是新增的列
                                    EStatsType statsType = EStatsType.SUM;
                                    int scale = 2;
                                    if(setupMap.containsKey(e.getCustom().getOC())){
                                        TotalSetup totalSetup = setupMap.get(e.getCustom().getOC());
                                        statsType = totalSetup.getStatsType();
                                        scale = totalSetup.getScale();
                                    }
                                    values = dataStore.stream().map(o -> o.get(colIndex)).map(UCell::getV).collect(Collectors.toList());
                                    v = funcFactory.apply(statsType,values,scale);
                                    e.setV(v).setT(UCellType.NUMBER);
                                }else{
                                    e.setT(UCellType.STRING).setV(v);
                                }

                            })
                            .collect(Collectors.toList());

                    generateCells.addAll(cloneColumns);

                    lists.add(endIndex,cloneColumns);

                    //改变 新增的行的下标
                    changeIndex(endIndex,newRowIndex);

                    Integer cloneRNo = CollectionUtil.getFirst(cloneColumns).getR();
                    copyRNos.add(cloneRNo);
                    //跳过生成的行
                    startIndex = endIndex+1;
                }else{
                    startIndex = endIndex;
                }
                startR = endR;
            }
        }
        return generateCells;
    }

    /**
     * 横向拓展 列小计
     * @param group
     * @param lists
     * @return
     */
    private List<UCell> horizontal(UCell group, List<List<UCell>> lists,List<Integer> rowNos,List<Integer> colNos,List<Integer> newRowIndex, List<Integer> newColIndex) {
        StatsSetup colSubStatsSetup = group.getCustom().getColSubStatsSetup();
        List<UCell> generateCells = new ArrayList<>();
        if(!colSubStatsSetup.getEnabled()){
            return generateCells;
        }
        List<TotalSetup> setupList = Optional.ofNullable(colSubStatsSetup.getSetupList()).orElse(new ArrayList<>()).stream().filter(e -> CollectionUtil.isNotEmpty(e.getFields())).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(setupList)){
            return generateCells;
        }

        Integer c = group.getC();
        int parentC = c-1;
        //检查需要clone的列是否存在
        if(!colNos.contains(parentC)){
            int index = colNos.indexOf(c);
            //不存在得新增这一列
            lists.forEach(list -> {
                UCell first = CollectionUtil.getFirst(list);
                UCell clone = first.clone();
                clone.setC(parentC);
                list.add(index,clone);
            });
            colNos.add(index,parentC);
            changeIndex(index,newColIndex);
        }

        String alias = colSubStatsSetup.getAlias();
        String styleKey = buildStyle(group, colSubStatsSetup);

        int startCIndex = colNos.indexOf(c);
        setupList.forEach(setup -> {
            List<StatsField> fields = setup.getFields();

            List<Integer> fieldsR = fields.stream().map(StatsField::getR).collect(Collectors.toList());
            List<Integer> fieldsC = fields.stream().map(StatsField::getC).collect(Collectors.toList());

            //新增一行
            List<UCell> cloneRowData = CollectionUtil.getLast(lists).stream().map(UCell::clone).peek(e -> e.getCustom().randomCustomSign()).collect(Collectors.toList());
            Integer cloneRowNo = CollectionUtil.getFirst(cloneRowData).getR();
            int r = cloneRowNo+1;
            rowNos.add(r);

            EStatsType statsType = setup.getStatsType();
            int scale = setup.getScale();

            for (int i = 0; i < cloneRowData.size(); i++) {
                UCell cloneCell = cloneRowData.get(i);
                cloneCell.setS(styleKey);
                if(i==0){
                    cloneCell.setV(alias).setT(UCellType.STRING);
                    continue;
                }

                Object v = "--";
                if(i<startCIndex){
                    cloneCell.setV(v);
                   continue;
                }
                if(newColIndex.contains(i) || !fieldsC.contains(cloneCell.getOC())){
                    cloneCell.setV(v);
                    continue;
                }

                int colIndex = i;
                AtomicInteger atoRIndex = new AtomicInteger();
                List<Object> values = lists.stream()
                        .filter(e -> !newRowIndex.contains(atoRIndex.getAndIncrement()))
                        .map(e -> e.get(colIndex))
                        .filter(e -> fieldsR.contains(e.getOR()))
                        .map(UCell::getV).collect(Collectors.toList());
                Object calculation = funcFactory.apply(statsType,values,scale);
                cloneCell.setV(calculation).setT(UCellType.NUMBER);
                cloneCell.setR(r);
            }
            lists.add(cloneRowData);
            changeIndex(lists.size()-1,newRowIndex);
            generateCells.addAll(cloneRowData);
        });
        return generateCells;
    }
}
