package cn.bctools.design.util;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class ConditionMergeStrategy<T> extends AbstractMergeStrategy {
    private final List<T> dataList;
    private final int[] mergeColumns;
    private final Function<T, String> idGetter;
    private final Map<String, int[]> idRowMap = new HashMap<>();
    private final Set<String> mergedRegions = new HashSet<>();
    private final int headerRowCount;  // 关键：记录表头行数

    /**
     * @param dataList 数据列表
     * @param mergeColumns 需要合并的列索引
     * @param idGetter 合并依据字段的获取函数
     * @param headerRowCount 表头行数（通常为1）
     */
    public ConditionMergeStrategy(List<T> dataList, int[] mergeColumns,
                                   Function<T, String> idGetter, int headerRowCount) {
        this.dataList = dataList;
        this.mergeColumns = mergeColumns;
        this.idGetter = idGetter;
        this.headerRowCount = headerRowCount;  // 设置表头行数
        initIdRowMap();
    }

    private void initIdRowMap() {
        AtomicInteger rowIndex = new AtomicInteger(0);
        dataList.forEach(item -> {
            int currentRow = rowIndex.getAndIncrement();
            String id = idGetter.apply(item);
            idRowMap.compute(id, (k, v) ->
                    v == null ? new int[]{currentRow, currentRow} : new int[]{v[0], currentRow}
            );
        });
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        // 跳过表头行处理
        if (relativeRowIndex == null || relativeRowIndex <= headerRowCount) {
            return;
        }

        // 计算数据行索引（减去表头行数）
        int dataRowIndex = relativeRowIndex - headerRowCount - 1;
        if (dataRowIndex < 0 || dataRowIndex >= dataList.size()) return;

        String currentId = idGetter.apply(dataList.get(dataRowIndex));
        int[] range = idRowMap.get(currentId);

        if (range == null || range[0] == range[1]) return;

        for (int col : mergeColumns) {
            if (cell.getColumnIndex() != col) continue;

            // 生成区域唯一标识
            String regionKey = range[0] + "-" + range[1] + "-" + col;

            if (!mergedRegions.contains(regionKey)) {
                try {
                    // 关键：数据行索引转换为Excel行索引（加上表头偏移）
                    int startRow = range[0] + headerRowCount;
                    int endRow = range[1] + headerRowCount;

                    CellRangeAddress region = new CellRangeAddress(
                            startRow, endRow, col, col
                    );

                    sheet.addMergedRegion(region);
                    mergedRegions.add(regionKey);
                } catch (IllegalStateException e) {
                    System.err.println("合并区域跳过: " + e.getMessage());
                }
            }
            break;
        }
    }
}
