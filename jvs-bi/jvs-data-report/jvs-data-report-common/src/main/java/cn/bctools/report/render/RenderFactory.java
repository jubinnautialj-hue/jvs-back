package cn.bctools.report.render;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.USheet;
import cn.bctools.report.model.univer.UWorkbook;
import cn.bctools.report.model.univer.conf.UColumnData;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.model.univer.conf.URowData;
import cn.bctools.report.model.univer.conf.USheetPluginDTO;
import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.render.scanner.BaseScanner;
import cn.bctools.report.utils.USheetContext;
import cn.bctools.report.utils.USheetUtils;
import cn.bctools.report.utils.UWorkbookContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 根据设计 计算渲染数据
 *
 * @author wl
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RenderFactory {

    private final List<? extends BaseScanner> SCANNER;
    private final Map<String,? extends Calculation> FILL_BEAN_MAP;
    private final List<? extends USheetPlugin> PLUGINS;

    /**
     * 计算报表渲染数据
     * @param report
     */
    public void compute(JvsDataReport report){
        UWorkbook reportDesign = report.getReportDesign();
        if(reportDesign==null || reportDesign.getSheets()==null){
            return;
        }
        List<USheetPluginDTO> resources = reportDesign.getResources();
        Map<String, UStyle> styles = reportDesign.getStyles();
        UWorkbookContext.setStyles(styles);
        reportDesign.getSheets().forEach((sheetId,sheet) -> this.render(reportDesign.getId(),sheetId,sheet,resources));
        reportDesign.setStyles(UWorkbookContext.getStyles());
    }

    /**
     * 执行渲染操作
     * @param unitId 工作簿id
     * @param sheetId 工作表id
     * @param sheet 工作表
     * @param resources 工作簿资源
     */
    private void render(String unitId, String sheetId, USheet sheet, List<USheetPluginDTO> resources) {
        // 扫描需要渲染的组合
        try {
            Map<Integer, Map<Integer, UCell>> cellData = sheet.getCellData();
            List<RenderingGroup> renderingGroups = scanner(cellData);

            // 设置合并数据
            USheetContext.setCustomMerge(sheet.getMergeData());
            // 设置查询条件
            USheetContext.setSearch(sheet.getCustom().getQueryConf());

            // 计算设计块
            calculateDesignBlocks(unitId, sheetId, renderingGroups);

            // 获取所有单元格数据
            List<UCell> allCellData = USheetContext.getCells();
            //设置所有值为空的单元格为字符串
            allCellData.forEach(e -> e.setT(e.getV()==null?1:e.getT()));

            // 计算插件
            calculatePlugins(resources, sheetId, allCellData);

            // 计算列宽和行高
            calculateColumnAndRowData(sheet, allCellData);

            // 更新单元格数据
            updateSheetData(sheet, allCellData);

            // 计算最大行号和列号
            calculateMaxRowAndColumn(sheet);
        } finally {
            // 清除缓存
            USheetContext.clear();
        }
    }

    /**
     * 计算设计块，包括数据、偏移、合并项和单元格占用
     */
    private void calculateDesignBlocks(String unitId, String sheetId, List<RenderingGroup> renderingGroups) {
        List<URange> mergeData = new ArrayList<>();
        renderingGroups.stream().filter(e -> !EValueType.空.equals(e.getBaseValueType())).forEach(group -> {
            String serviceName = group.getRenderingType().getServiceName();
            Calculation calculation = FILL_BEAN_MAP.get(serviceName);
            if (calculation == null) {
                throw new BusinessException("未知的渲染内容");
            }
            boolean check = calculation.check(group);
            if(!check){
                throw new BusinessException("无法计算，出现不支持的渲染内容");
            }

            // 计算数据
            List<UCell> data = calculation.data(group);
            // 计算小计 合计
            calculation.stats(group,data);
            // 计算偏移
            USheetUtils.offset(group,data);
            // 计算合并项
            List<URange> merge = calculation.merge(group, data);
            merge.forEach(e -> e.setSheetId(sheetId).setUnitId(unitId));
            // 计算单元格占用
            USheetContext.setOccupied(group, data);
            mergeData.addAll(merge);
            USheetContext.setCells(data);
        });
        USheetContext.setMerge(mergeData);
    }

    /**
     * 计算插件
     */
    private void calculatePlugins(List<USheetPluginDTO> resources, String sheetId, List<UCell> allCellData) {
        PLUGINS.forEach(plugin -> plugin.compute(resources, sheetId, allCellData));
    }

    /**
     * 计算列宽和行高
     */
    private void calculateColumnAndRowData(USheet sheet, List<UCell> allCellData) {
        List<UCellExpand> uCellExpands = allCellData.stream()
                .map(UCell::getCustom)
                .collect(Collectors.toList());

        // 计算列宽
        Map<Integer, UColumnData> columnData = sheet.getColumnData();
        sheet.setColumnData(USheetUtils.calculateColumnData(uCellExpands, columnData));

        // 计算行高
        Map<Integer, URowData> rowData = sheet.getRowData();
        sheet.setRowData(USheetUtils.calculateRowData(uCellExpands, rowData));
    }

    /**
     * 更新单元格数据和合并数据
     */
    private void updateSheetData(USheet sheet, List<UCell> allCellData) {
        List<URange> mergeData = USheetContext.getMerge();
        Map<Integer, Map<Integer, UCell>> sheetData = allCellData.stream()
                .collect(Collectors.groupingBy(UCell::getR, Collectors.toMap(UCell::getC, UCell::clear, (v1, v2) -> v2)));
        sheet.setMergeData(mergeData);
        sheet.setCellData(sheetData);
    }

    /**
     * 计算最大行号和列号
     */
    private void calculateMaxRowAndColumn(USheet sheet) {
        Map<Integer, Map<Integer, UCell>> sheetData = sheet.getCellData();

        // 计算最大行号
        sheetData.keySet().stream()
                .max(Integer::compare)
                .filter(maxRow -> maxRow > sheet.getRowCount())
                .map(e -> e+10)
                .ifPresent(sheet::setRowCount);

        // 计算最大列号
        sheetData.values().stream()
                .flatMap(uCellMap -> uCellMap.keySet().stream())
                .max(Integer::compare)
                .filter(maxColumn -> maxColumn > sheet.getColumnCount())
                .map(e -> e+10)
                .ifPresent(sheet::setColumnCount);
    }

    /**
     * 扫描需要渲染的组合
     * @param cellData 单元格设置
     * @return 需要渲染的组合列表
     */
    public List<RenderingGroup> scanner(Map<Integer, Map<Integer, UCell>> cellData) {
        // 将二维的cellData转换为一维列表，并按行列排序，同时设置r、c、or、oc属性
        List<UCell> oneDimensional = flattenAndSortCellData(cellData);
        //过滤为空的单元格
//        List<UCell> notEmpty = oneDimensional
//                .stream()
//                .filter(e -> !EValueType.空.equals(e.getCustom().getValueType()))
//                .collect(Collectors.toList());
        // 使用SCANNER扫描并生成渲染组合
        return SCANNER.stream()
                .flatMap(scanner -> scanner.scanner(filterUnflaggedCells(oneDimensional), cellData).stream())
                .filter(Objects::nonNull)
                .sorted(RenderingGroup::compareTo).collect(Collectors.toList());
    }

    /**
     * 将二维的cellData转换为一维列表，并按行列排序，同时设置r、c、or、oc属性
     * @param cellData 单元格设置
     * @return 排序后的一维单元格列表
     */
    private List<UCell> flattenAndSortCellData(Map<Integer, Map<Integer, UCell>> cellData) {
        return cellData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(rowEntry -> rowEntry.getValue().entrySet().stream()
                        .peek(cellEntry -> {
                            UCell uCell = cellEntry.getValue();
                            uCell.setR(rowEntry.getKey());
                            uCell.setC(cellEntry.getKey());
                            uCell.getCustom().setOR(rowEntry.getKey());
                            uCell.getCustom().setOC(cellEntry.getKey());
                        })
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue))
                .collect(Collectors.toList());
    }

    /**
     * 过滤出未标记的单元格
     * @param cells 所有单元格列表
     * @return 未标记的单元格列表
     */
    private List<UCell> filterUnflaggedCells(List<UCell> cells) {
        return cells.stream()
                .filter(cell -> !cell.getCustom().getFlagged())
                .collect(Collectors.toList());
    }
}
