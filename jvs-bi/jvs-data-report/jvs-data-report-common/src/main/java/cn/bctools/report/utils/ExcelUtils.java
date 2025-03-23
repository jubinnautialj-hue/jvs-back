package cn.bctools.report.utils;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.USheet;
import cn.bctools.report.model.univer.conf.UColumnData;
import cn.bctools.report.model.univer.conf.URowData;
import cn.bctools.report.model.univer.plugin.SheetDrawingPlugin;
import cn.bctools.report.model.univer.style.BD;
import cn.bctools.report.model.univer.style.RGB;
import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.render.plugin.USheetConditionFormatingPlugin;
import cn.bctools.report.utils.excel.enums.EConditionSubType;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    /**
     * 创建工作表
     *
     * @param workbook
     * @param uSheet
     * @param styles
     */
    public static void createSheet(XSSFWorkbook workbook, USheet uSheet, Map<String, XSSFCellStyle> styles, JSONObject drawingMap, Map<String, String> resourceMap){
        //生产工作表名称
        String name = Optional.ofNullable(uSheet.getName()).orElse(IdGenerator.getIdStr(36));
        // 创建一个工作表
        XSSFSheet sheet = workbook.createSheet(name);

        // 创建绘图对象
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        //设置浮动图片
        if(drawingMap.containsKey(uSheet.getId())){
            JSONObject obj = drawingMap.getJSONObject(uSheet.getId());
            JSONObject data = obj.getJSONObject("data");
            List<SheetDrawingPlugin> collect = data.values()
                    .stream()
                    .map(e -> BeanUtil.copyProperties(e, SheetDrawingPlugin.class)).collect(Collectors.toList());
            for (SheetDrawingPlugin sheetDrawingPlugin : collect) {
                String source = sheetDrawingPlugin.getSource();
                //创建锚点
                XSSFClientAnchor xssfClientAnchor = getXssfClientAnchor(sheetDrawingPlugin);
                drawingPic(source,workbook,xssfClientAnchor,drawing);
            }
        }

        uSheet.getMergeData().stream().filter(e -> !Objects.equals(e.getStartRow(), e.getEndRow()) || !e.getStartColumn().equals(e.getEndColumn())).forEach(e -> {
            CellRangeAddress cellAddresses = new CellRangeAddress(e.getStartRow(),e.getEndRow(),e.getStartColumn(),e.getEndColumn());
            sheet.addMergedRegionUnsafe(cellAddresses);
        });

        //提取univer单元格数据
        Map<Integer, Map<Integer, UCell>> cellData = uSheet.getCellData();

        //设置列宽
        uSheet.getColumnData().forEach((colNum, uColumnData) -> Optional.ofNullable(uColumnData).map(UColumnData::getW).ifPresent(e -> sheet.setColumnWidth(colNum,pixelToSheetNum(e))));

        //设置数据
        Map<Integer, URowData> rowData = uSheet.getRowData();
        for (Map.Entry<Integer, Map<Integer, UCell>> univerRow : cellData.entrySet()) {
            Integer rowIndex = univerRow.getKey();
            XSSFRow row = sheet.createRow(rowIndex);
            //设置行高
            if(rowData.containsKey(rowIndex)){
                URowData uRowData = rowData.get(rowIndex);
                Optional.ofNullable(uRowData).filter(e -> e.getH()!=null).map(URowData::getH).map(e -> e * 0.75*20).ifPresent(e -> row.setHeight( e.shortValue()));
            }

            Map<Integer, UCell> univerColumns = univerRow.getValue();
            for (Map.Entry<Integer, UCell> uCellColumn : univerColumns.entrySet()) {
                Integer columnNum = uCellColumn.getKey();
                UCell uCell = uCellColumn.getValue();
                if(uCell.getV()==null && uCell.getP()!=null){
                    Object p = uCell.getP();
                    JSONObject entries = JSONUtil.parseObj(p);
                    JSONObject drawingConf = entries.getJSONObject("drawings");
                    if(!drawingConf.isEmpty()){
                        try {
                            String drawingsOrder = entries.getJSONArray("drawingsOrder").get(0, String.class);
                            JSONObject jsonObject = drawingConf.getJSONObject(drawingsOrder);
                            String source = jsonObject.getStr("source");

                            XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor();
                            xssfClientAnchor.setCol1(columnNum);
                            xssfClientAnchor.setRow1(rowIndex);
                            xssfClientAnchor.setCol2(columnNum);
                            xssfClientAnchor.setRow2(rowIndex);
                            drawingPic(source,workbook,xssfClientAnchor,drawing);
                        } catch (Exception e) {
                            log.error("绘制图片失败");
                        }
                    }
                }else{
                    XSSFCell xssfCell = row.createCell(columnNum);
                    if(StrUtil.isNotBlank(uCell.getF())){
                        String formula = StrUtil.replace(uCell.getF(), StringPool.EQUALS, StringPool.EMPTY);
                        try {
                            xssfCell.setCellFormula(formula);
                            xssfCell.setCellType(CellType.FORMULA);
                        } catch (FormulaParseException  | IllegalStateException e) {
                            xssfCell.setCellValue(formula);
                            xssfCell.setCellType(CellType.STRING);
                        }
                    }else{
                        String uCellValue = StrUtil.toStringOrNull(uCell.getV());
                        if(uCellValue==null){
                            xssfCell.setCellType(CellType.BLANK);
                        }else{
                            switch (uCell.getT()){
                                case 2:
                                    if(NumberUtil.isNumber(uCellValue)){
                                        xssfCell.setCellType(CellType.NUMERIC);
                                        BigDecimal bigDecimal = new BigDecimal(uCellValue);
                                        if(bigDecimal.scale()>0){
                                            xssfCell.setCellValue(bigDecimal.floatValue());
                                        }else{
                                            xssfCell.setCellValue(bigDecimal.longValue());
                                        }
                                        break;
                                    }
                                    //时间
                                    try {
                                        DateTime parse = DateUtil.parse(uCellValue);
                                        long epochSecond = parse.toLocalDateTime().toEpochSecond((ZoneOffset) ZoneOffset.systemDefault());
                                        double excelDateValue = epochSecond / 86400.0 + 25569;
                                        xssfCell.setCellType(CellType.NUMERIC);
                                        xssfCell.setCellValue(excelDateValue);
                                        break;
                                    } catch (Exception e) {
                                        log.error("时间转换失败");
                                    }
                                    xssfCell.setCellType(CellType.STRING);
                                    xssfCell.setCellValue(uCellValue);
                                    break;
                                case 3:
                                    xssfCell.setCellType(CellType.BOOLEAN);
                                    xssfCell.setCellValue(Boolean.parseBoolean(uCellValue));
                                    break;
                                default:
                                    xssfCell.setCellType(CellType.STRING);
                                    xssfCell.setCellValue(uCellValue);
                            }
                        }
                    }

                    if(uCell.getS()!=null){
                        if(uCell.getS() instanceof String){
                            xssfCell.setCellStyle(styles.get(uCell.getS()));
                        }else {
                            UStyle uStyle = BeanUtil.toBean(uCell.getS(), UStyle.class);
                            XSSFCellStyle xssfCellStyle = convertStyle(workbook, uStyle);
                            xssfCell.setCellStyle(xssfCellStyle);
                        }
                    }
                }

            }
        }

        //设置条件格式
        convertConditionFormatting(resourceMap,uSheet,sheet);

    }

    private static void drawingPic(String source,XSSFWorkbook workbook,XSSFClientAnchor anchor,XSSFDrawing drawing){
        // 去掉前缀，获取纯 Base64 数据
        String base64Data = source.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        int picIndex = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
        XSSFPicture picture = drawing.createPicture(anchor, picIndex);
        picture.resize(1);
    }

    private static XSSFClientAnchor getXssfClientAnchor(SheetDrawingPlugin sheetDrawingPlugin) {
        SheetDrawingPlugin.Transform transform = sheetDrawingPlugin.getTransform();

        SheetDrawingPlugin.Location from = sheetDrawingPlugin.getSheetTransform().getFrom();
        SheetDrawingPlugin.Location to = sheetDrawingPlugin.getSheetTransform().getTo();

        return new XSSFClientAnchor(
                0,0,
                transform.getWidth(),transform.getHeight(),
                from.getColumn(),from.getRow(),
                to.getColumn(),to.getRow());
    }

    public static XSSFCellStyle convertStyle(XSSFWorkbook workbook,UStyle uStyle){
        //单元格样式
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        if(uStyle == null){
            return cellStyle;
        }
        /*
        字体
         */
        XSSFFont font = workbook.createFont();
        //是否加粗
        Boolean bl = Optional.of(uStyle).map(UStyle::getBl).map(e -> e == 1).orElse(false);
        font.setBold(bl);
        //文字颜色
        Optional.of(uStyle).map(UStyle::getCl).map(RGB::getRgb).map(ExcelUtils::rgbToXSSFColor).ifPresent(font::setColor);
        //字体大小
        if(uStyle.getFs()!=null){
            font.setFontHeightInPoints(uStyle.getFs().shortValue());
        }
        //字体倾斜
        if(uStyle.getIt()!=null && uStyle.getIt()==1){
            font.setItalic(Boolean.TRUE);
        }
        font.setStrikeout(uStyle.getSt()!=null && uStyle.getSt().getS()==1);
        //字体
        if(StrUtil.isNotBlank(uStyle.getFf())){
            font.setFontName(uStyle.getFf());
        }
        //下划线
        if(uStyle.getUl()!=null){
            font.setUnderline(Font.U_SINGLE);
        }
        cellStyle.setFont(font);

        //单元格背景色
        Optional.of(uStyle).map(UStyle::getBg).map(RGB::getRgb).map(ExcelUtils::rgbToXSSFColor).ifPresent(e -> {
            cellStyle.setFillForegroundColor(e);
            // 填充模式
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        });

        // 水平居中
        Integer ht = uStyle.getHt();
        HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
        if (ht!=null) {
            switch (ht){
                case 2:
                    horizontalAlignment = HorizontalAlignment.CENTER;
                    break;
                case 3:
                    horizontalAlignment = HorizontalAlignment.RIGHT;
            }
        }
        cellStyle.setAlignment(horizontalAlignment);
        // 垂直居中
        Integer vt = uStyle.getVt();
        VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
        if (vt!=null) {
            switch (vt){
                case 1:
                    verticalAlignment = VerticalAlignment.TOP;
                    break;
                case 3:
                    verticalAlignment = VerticalAlignment.BOTTOM;
            }
        }
        cellStyle.setVerticalAlignment(verticalAlignment);

        //提取边框
        BD bd = uStyle.getBd();
        // 设置边框
        if(bd!=null){
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
        }
        if(uStyle.getN()!=null){
            try {
                String pattern = uStyle.getN().getPattern();
                if (StrUtil.isNotBlank(pattern)) {
                    CreationHelper createHelper = workbook.getCreationHelper();
                    short format = createHelper.createDataFormat().getFormat(pattern);
                    cellStyle.setDataFormat(format);
                }
            } catch (Exception e) {
                log.error("univer格式化转excel格式化失败");
            }
        }
        return cellStyle;
    }

    public static XSSFColor rgbToXSSFColor(String rgb) {
        // 去掉#号
        Integer[] colorNums = new Integer[3];
        if (rgb.startsWith("#")) {
            rgb = rgb.substring(1);
            colorNums[0] = Integer.parseInt(rgb.substring(0, 2), 16);
            colorNums[1] = Integer.parseInt(rgb.substring(2, 4), 16);
            colorNums[2] = Integer.parseInt(rgb.substring(4, 6), 16);
        }

        if(StrUtil.startWith(rgb,"rgb")){
            String regex = "(\\(.*?\\))";
            List<String> allGroup0 = ReUtil.findAllGroup0(regex, rgb);
            String colorStr = CollectionUtil.getFirst(allGroup0);
            colorStr = StrUtil.replace(colorStr, StringPool.LEFT_BRACKET,"");
            colorStr = StrUtil.replace(colorStr, StringPool.RIGHT_BRACKET,"");
            colorNums = StrUtil.split(colorStr, StringPool.COMMA).stream().map(StrUtil::trim).map(BigDecimal::new).map(BigDecimal::intValue).collect(Collectors.toList()).toArray(new Integer[]{});
        }

        // 将RGB字符串转换为字节数组
        byte[] rgbBytes = new byte[3];
        // 红色
        rgbBytes[0] =  colorNums[0].byteValue();
        // 绿色
        rgbBytes[1] = colorNums[1].byteValue();
        // 蓝色
        rgbBytes[2] = colorNums[2].byteValue();
        // 创建XSSFColor对象
        return new XSSFColor(rgbBytes, null);
    }

    private static int pixelToSheetNum(int pixelWidth){
        int v = pixelToChartWidth(pixelWidth);
        v = Math.min(v, 255);
        return v*256;
    }

    /**
     * 将像素宽度转换为 Excel 列宽
     *
     * @param pixelWidth 像素宽度
     * @return 列宽（单位：字符宽度）
     */
    private static int pixelToChartWidth(int pixelWidth) {
        // Excel 中 1 个列宽单位 ≈ 7 像素
        return pixelWidth / 7;
    }

    /**
     * {"c58670b33a3c57df22f9b565192f2342-sheet":[{"cfId":"JW4H2ZyW","ranges":[{"startRow":2,"startColumn":5,"rangeType":0,"sheetId":"c58670b33a3c57df22f9b565192f2342-sheet","unitId":"c58670b33a3c57df22f9b565192f2342-workbook","endRow":41,"endColumn":5}],"rule":{"config":[{"color":"#F96800","value":{"type":"min","value":10},"index":0},{"color":"#8BBB11","value":{"type":"max","value":90},"index":1}],"type":"colorScale"},"stopIfTrue":false}]}
     * @param resourceMap
     * @param uSheet
     * @param xssfSheet
     */
    private static void convertConditionFormatting(Map<String,String> resourceMap,USheet uSheet,XSSFSheet xssfSheet){
        USheetConditionFormatingPlugin bean = SpringContextUtil.getBean(USheetConditionFormatingPlugin.class);
        String name = bean.getName();
        if(!resourceMap.containsKey(name)){
            return;
        }
        String configStr = resourceMap.get(name);
        if(StrUtil.isBlank(configStr)){
            return;
        }
        JSONObject config = JSONUtil.parseObj(configStr);
        String uSheetId = uSheet.getId();
        if(!config.containsKey(uSheetId)){
            return;
        }
        JSONArray arr = config.getJSONArray(uSheetId);

        // 获取条件格式工具
        XSSFSheetConditionalFormatting sheetCF = xssfSheet.getSheetConditionalFormatting();

        List<USheetConditionFormatingPlugin.Conf> list = arr.toList(USheetConditionFormatingPlugin.Conf.class);

        for (USheetConditionFormatingPlugin.Conf conf : list) {
            List<CellRangeAddress> collect = conf.getRanges()
                    .stream()
                    .map(range -> new CellRangeAddress(range.getStartRow(), range.getEndRow(), range.getStartColumn(), range.getEndColumn()))
                    .collect(Collectors.toList());

            CellRangeAddress[] ranges = collect.toArray(new CellRangeAddress[]{});

            JSONObject uRule = JSONUtil.parseObj(conf.getRule());
            String type = uRule.getStr("type");
            if(StrUtil.equals("colorScale",type)){
                colorScale(sheetCF, uRule, ranges);
            }

            if(StrUtil.equals("dataBar",type)){
                dataBar(uRule, sheetCF, ranges);
            }

            if(StrUtil.equals("highlightCell",type)){
                String subType = uRule.getStr("subType");
                if(!EConditionSubType.contains(subType)){
                    return;
                }
                EConditionSubType conditionSubType = EConditionSubType.valueOf(subType);
                //获取样式
                UStyle style = uRule.getJSONObject("style").toBean(UStyle.class);
                String operator = uRule.getStr("operator");
                //设置条件格式
                SpringContextUtil.getBean(conditionSubType.getHClass()).handle(sheetCF,style,operator,uRule,ranges);
            }

        }

    }

    private static void dataBar(JSONObject uRule, XSSFSheetConditionalFormatting sheetCF, CellRangeAddress[] ranges) {
        JSONObject dataBarConfig = uRule.getJSONObject("config");

        String positiveColor = dataBarConfig.getStr("positiveColor");
        XSSFColor xssfColor = rgbToXSSFColor(positiveColor);
        XSSFConditionalFormattingRule conditionalFormattingRule = sheetCF.createConditionalFormattingRule(xssfColor);
        XSSFDataBarFormatting dataBarFormatting = conditionalFormattingRule.getDataBarFormatting();

                /*
                阈值 最小值
                 */
        XSSFConditionalFormattingThreshold minThreshold = dataBarFormatting.getMinThreshold();
        JSONObject minConfig = dataBarConfig.getJSONObject("min");
        dataBarThreshold(minConfig, minThreshold);

                /*
                阈值 最大值
                 */
        JSONObject maxConfig = dataBarConfig.getJSONObject("max");
        XSSFConditionalFormattingThreshold maxThreshold = dataBarFormatting.getMaxThreshold();
        dataBarThreshold(maxConfig, maxThreshold);

        sheetCF.addConditionalFormatting(ranges,conditionalFormattingRule);
    }

    private static void dataBarThreshold(JSONObject minConfig, XSSFConditionalFormattingThreshold minThreshold) {
        String minType = minConfig.getStr("type");
        ConditionalFormattingThreshold.RangeType minRangeType = getRangeType(minType);
        if(ConditionalFormattingThreshold.RangeType.FORMULA.equals(minRangeType)){
            minThreshold.setFormula(minConfig.getStr("value"));
        }else{
            minThreshold.setValue(minConfig.getDouble("value"));
        }
        minThreshold.setRangeType(minRangeType);
    }

    /**
     * 色阶
     * @param sheetCF
     * @param uRule
     * @param ranges
     */
    private static void colorScale(XSSFSheetConditionalFormatting sheetCF, JSONObject uRule, CellRangeAddress[] ranges) {
        // 创建色阶条件格式
        XSSFConditionalFormattingRule rule = sheetCF.createConditionalFormattingColorScaleRule();
        // 设置色阶的颜色（3色阶：最小值、中间值、最大值）
        ColorScaleFormatting colorScale = rule.getColorScaleFormatting();
        JSONArray uRuleConfig = uRule.getJSONArray("config");
        List<XSSFColor> colors = new ArrayList<>();
        List<ConditionalFormattingThreshold> thresholds = new ArrayList<>();
        for (int i1 = 0; i1 < uRuleConfig.size(); i1++) {
            String color = uRuleConfig.getJSONObject(i1).getStr("color");
            XSSFColor xssfColor = rgbToXSSFColor(color);
            colors.add(xssfColor);
            JSONObject jsonObject = uRuleConfig.getJSONObject(i1).getJSONObject("value");

            String valueType = jsonObject.getStr("type");
            ConditionalFormattingThreshold threshold = colorScale.createThreshold();

            String value = jsonObject.getStr("value");

            ConditionalFormattingThreshold.RangeType rangeType = getRangeType(valueType);
            if(ConditionalFormattingThreshold.RangeType.FORMULA.equals(rangeType)){
                threshold.setFormula(value);
            }else{
                threshold.setValue(Double.parseDouble(value));
            }
            threshold.setRangeType(rangeType);
            thresholds.add(threshold);
        }
        XSSFColor[] colorsArray = colors.toArray(new XSSFColor[]{});
        colorScale.setColors(colorsArray);

        //设置阈值
        ConditionalFormattingThreshold[] thresholdsArray = thresholds.toArray(new ConditionalFormattingThreshold[]{});
        colorScale.setThresholds(thresholdsArray);

        sheetCF.addConditionalFormatting(ranges,rule);
    }

    public static ConditionalFormattingThreshold.RangeType getRangeType(String type){
        ConditionalFormattingThreshold.RangeType rangeType = null;
        switch (type){
            case "min":
                rangeType = ConditionalFormattingThreshold.RangeType.MIN;
                break;
            case "max":
                rangeType = ConditionalFormattingThreshold.RangeType.MAX;
                break;
            case "num":
                rangeType = ConditionalFormattingThreshold.RangeType.NUMBER;
                break;
            case "percent":
                rangeType = ConditionalFormattingThreshold.RangeType.PERCENT;
                break;
            case "percentile":
                rangeType = ConditionalFormattingThreshold.RangeType.PERCENTILE;
                break;
            case "formula":
                rangeType = ConditionalFormattingThreshold.RangeType.FORMULA;
        }
        return rangeType;
    }

    /**
     * 设置条件格式样式
     * @param conditionalFormattingRule 条件格式
     * @param style univer样式
     */
    public static void setXSSFPatternFormatting(XSSFConditionalFormattingRule conditionalFormattingRule, UStyle style){

        XSSFPatternFormatting patternFormatting = conditionalFormattingRule.createPatternFormatting();

        //背景色
        XSSFColor xssfColor = Optional.ofNullable(style).map(UStyle::getBg).map(RGB::getRgb).map(ExcelUtils::rgbToXSSFColor).orElse(null);
        patternFormatting.setFillBackgroundColor(xssfColor);

        XSSFFontFormatting fontFormatting = conditionalFormattingRule.createFontFormatting();

        if(style!=null ){
            boolean it = style.getIt()!=null && style.getIt() == 1;
            boolean bl = style.getBl()!=null && style.getBl() == 1;

            fontFormatting.setFontStyle(it,bl);

            if(style.getUl()!=null && style.getUl().getS()==1){
                fontFormatting.setUnderlineType(FontUnderline.SINGLE.getByteValue());
            }

            if(style.getSt()!=null && style.getSt().getS()==1){
                setStrikeOut(fontFormatting);
            }

            if(style.getCl()!=null){
                String rgb = style.getCl().getRgb();
                fontFormatting.setFontColor(ExcelUtils.rgbToXSSFColor(rgb));
            }
        }
    }

    /**
     * 设置删除线
     * poi没有明确的方法设置删除线
     * 通过反射的方式设置
     * @param fontFormatting
     */
    public static void setStrikeOut(XSSFFontFormatting fontFormatting){
        try {
            Field font = XSSFFontFormatting.class.getDeclaredField("_font");
            font.setAccessible(true);
            CTFont ctFont = (CTFont) font.get(fontFormatting);
            ctFont.setStrikeArray(null);
            ctFont.addNewStrike().setVal(true);
            font.set(fontFormatting,ctFont);
        } catch (Exception e) {
            System.out.println("删除线设置错误");
        }
    }

    /**
     * 坐标转引用
     * @param r 行下标
     * @param c 列下标
     * @return 引用
     */
    public static String indexToRef(int r, int c){
        String colName = ExcelUtil.indexToColName(c);
        return colName+(r+1);
    }
}
