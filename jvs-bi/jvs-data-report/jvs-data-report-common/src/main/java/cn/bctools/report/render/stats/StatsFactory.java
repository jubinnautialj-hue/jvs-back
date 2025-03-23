package cn.bctools.report.render.stats;

import cn.bctools.report.dto.GroupRecord;
import cn.bctools.report.model.univer.UCell;
import cn.hutool.core.collection.ListUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsFactory {

    private final List<? extends BStats > statsBeans;

    public List<UCell> doCalculation(List<List<UCell>> lists,List<UCell> cells,List<UCell> groups,Map<String, GroupRecord> groupRecordMap,List<Integer> rowNos ,List<Integer> colNos){
        List<UCell> generates = new ArrayList<>();
        //复制的行
        List<Integer> copyRNos = new ArrayList<>();
        //复制的列
        List<Integer> copyCNos = new ArrayList<>();

        List<Integer> newRowIndex = new ArrayList<>();
        List<Integer> newColIndex = new ArrayList<>();

        ListUtil.sort(groups, UCell::compareTo);
        ListUtil.reverse(groups);

        for (UCell group : groups) {
            for (BStats stats : statsBeans) {
                //是否启用
                boolean status = stats.status(group);
                if (status) {
                    List<UCell> calculation = stats.calculation(group, cells, lists, groupRecordMap, rowNos, colNos, copyRNos, copyCNos,newRowIndex,newColIndex);
                    generates.addAll(calculation);
                }
            }
        }
        return generates;
    }

}
