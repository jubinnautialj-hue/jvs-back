package cn.bctools.report.render.calculation;

import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.render.Calculation;
import cn.bctools.report.render.CalculationType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.utils.USheetContext;
import cn.bctools.report.utils.SourceUtils;
import cn.bctools.report.utils.SqlUtils;
import cn.bctools.report.utils.USheetUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 普通设计块 计算
 * @author wl
 */
@Component(CalculationType.NORMAL)
@RequiredArgsConstructor
public class NormalCalculation implements Calculation {

    private final SourceUtils sourceUtils;

    @Override
    public boolean check(RenderingGroup conf) {
        return true;
    }

    @Override
    public List<UCell> data(RenderingGroup conf) {
        List<UCell> renderData = new ArrayList<>();

        List<UCell> uCells = conf.getCells();
        if(CollectionUtil.isEmpty(uCells)){
            return renderData;
        }

        List<UCell> otherCell = uCells.stream().filter(e -> !EValueType.数据集.equals(e.getCustom().getValueType())).collect(Collectors.toList());
        renderData.addAll(otherCell);

        List<UCell> factoryCell = uCells.stream().filter(e -> EValueType.数据集.equals(e.getCustom().getValueType())).collect(Collectors.toList());

        if(CollectionUtil.isEmpty(factoryCell)){
            return renderData;
        }
        //数据拓展方向
        EDataGrowthPlan dataGrowthPlan = CollectionUtil.getFirst(uCells).getCustom().getDataGrowthPlan();

        List<UField> collect = factoryCell.stream().map(UCell::getCustom).map(UCellExpand::getField).collect(Collectors.toList());

        String tableName = sourceUtils.getTableNames(factoryCell);

        String collect1 = collect.stream().map(SqlUtils::buildQueryField).distinct().collect(Collectors.joining(","));
        String sql = "SELECT " + collect1 + " FROM " + tableName;

        UField first = CollectionUtil.getFirst(collect);
        String where = SqlUtils.buildQueryWhere(first.getExecuteName());
        if(StrUtil.isNotBlank(where)){
            sql += " WHERE " + where;
        }

        String orderBy = SqlUtils.buildOrderBy(uCells);

        if(StrUtil.isNotBlank(orderBy)){
            sql+=orderBy;
        }

        if(EDataGrowthPlan.NONE.equals(dataGrowthPlan)){
            sql += " LIMIT 1";
        }

        List<Map<String, Object>> data = sourceUtils.getData(sql);

        int indexAdd = 0;
        for (Map<String, Object> row : data) {
            for (UCell confCell : factoryCell) {
                String fieldKey = USheetUtils.getUCellFieldKey(confCell);
                UCell uCell = confCell.clone();
                Object v = row.get(fieldKey);
                v = USheetUtils.nullReplacement(confCell,v);
                uCell.setV(v);
                if(EDataGrowthPlan.VERTICAL.equals(dataGrowthPlan)){
                    uCell.setC(confCell.getC());
                    uCell.setR(confCell.getR()+indexAdd);
                }else{
                    uCell.setC(confCell.getC()+indexAdd);
                    uCell.setR(confCell.getR());
                }
                uCell.setT(USheetUtils.fieldType2CellType(confCell));
                renderData.add(uCell);
            }
            //增加
            indexAdd++;
        }
        
        return renderData;
    }

    @Override
    public List<URange> merge(RenderingGroup conf, List<UCell> cells) {
        if (EValueType.固定值.equals(conf.getBaseValueType()) || EValueType.空.equals(conf.getBaseValueType())) {
            Map<String, URange> customMerge = USheetContext.getCustomMerge();
            Map<String, List<UCell>> groupMap = cells.stream().collect(Collectors.groupingBy(e -> e.getOR() + "_" + e.getOC()));
            List<URange> ranges = new ArrayList<>();
            for (UCell cell : conf.getCells()) {
                String key = cell.getOR() + "_" + cell.getOC();
                if(customMerge.containsKey(key)){
                    URange uRange = customMerge.get(key);
                    int rowCount = uRange.getEndRow()-uRange.getStartRow();
                    int colCount = uRange.getEndColumn()-uRange.getStartColumn();

                    List<UCell> uCells = groupMap.get(key);
                    ListUtil.sort(uCells,UCell::compareTo);
                    UCell first = CollectionUtil.getFirst(uCells);
                    uRange.setStartRow(first.getR())
                            .setStartColumn(first.getC())
                            .setEndRow(first.getR()+rowCount)
                            .setEndColumn(first.getC()+colCount);
                    ranges.add(uRange);
                }
            }
            return ranges;
        }
        return USheetUtils.merge(conf,cells);
    }

    @Override
    public void stats(RenderingGroup conf, List<UCell> cells) {
        USheetUtils.stats(conf,cells);
    }
}
