package cn.bctools.word.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.poi.excel.cell.CellUtil.getCellValue;


@Slf4j
public class ExcelVariablesReplaceUtil {

    @SneakyThrows
    public static void main(String[] args) {
//        List<ExcelVariable> list = getList();
        String pathname = "/Users/guojing/Desktop/aabc.xlsx";
//        File file = new File(pathname);
//        BufferedInputStream inputStream = FileUtil.getInputStream(file);
//        ExcelUtils.readAll(inputStream, Object.class);
//        System.out.println(1);
//        writeExcel(list, inputStream, new FileOutputStream(new File("abc.xlsx")));
        Map<Object, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> cellMap = new HashMap<>();
        cellMap.put("abc", "B7Rx6C(name,age,bb,dd,eff,abc)");
        map.put(2, cellMap);
        List<Dict> dicts = searchData(FileUtil.getInputStream(pathname), map);
        System.out.println(dicts);

    }


    /**
     * 写入变量数据
     *
     * @param map         需要填充的数据有哪些
     * @param inputStream excel文件输入对象数据
     * @param fos         转换后的输出结果
     */
    public static void writeExcel(Map<String, Object> map, InputStream inputStream, ByteArrayOutputStream fos) throws IOException {
        List<ExcelVariable> collect = map.keySet().stream().map(e -> {
            Object value = map.get(e);
            ExcelVariable.ExcelType type = ExcelVariable.ExcelType.String;
            //判断是否是数组
            if (value instanceof List && ObjectNull.isNotNull(value)) {
                if (((List<?>) value).get(0) instanceof Map) {
                    type = ExcelVariable.ExcelType.List;
                }
            } else if (value instanceof Date) {
                type = ExcelVariable.ExcelType.Date;
            } else if (value instanceof Number) {
                type = ExcelVariable.ExcelType.Number;
            }
            return new ExcelVariable().setName(e).setType(type).setValue(value);
        }).collect(Collectors.toList());
        writeExcel(collect, inputStream, fos);
    }

    public static void writeExcel(List<ExcelVariable> list, InputStream inputStream, ByteArrayOutputStream fos) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //     通过模板搜索出来的变量
        Set<ExcelVariable> search = searchVariables(workbook);
        List<ExcelVariable> excelVariables = openList(list);
        list.removeIf(e -> ExcelVariable.ExcelType.List.equals(e.getType()));
        list.addAll(excelVariables);

        Map<String, ExcelVariable> variableMap = list.stream().distinct().collect(Collectors.toMap(e -> "${{" + e.getName() + "}}", Function.identity()));

        //根据变量确定
        Map<Integer, List<ExcelVariable>> listMap = search.stream().collect(Collectors.groupingBy(ExcelVariable::getSheet));

        //遍历数据，区别哪些不是数组，哪些是数组
        for (Integer sheetI : listMap.keySet()) {

            Set<String> nameNowRow = new HashSet<>();
            //获取第一个里面的变量有哪些
            //获取 sheet
            Sheet sheet = workbook.getSheetAt(sheetI);

            listMap.get(sheetI).stream().filter(e -> variableMap.containsKey(e.getName())).map(e -> {
                ExcelVariable excelVariable = variableMap.get(e.getName());
                return new ExcelVariable().setValue(excelVariable.getValue()).setType(excelVariable.getType()).setSheet(e.getSheet()).setRow(e.getRow()).setColumn(e.getColumn()).setName(e.getName());
            }).forEach(e -> {
                Row row = sheet.getRow(e.getRow());
                if (ExcelVariable.ExcelType.List.equals(e.getType())) {
                    //根据字段确定是否添加行  确定添加几行
                    List rowList = (List) e.getValue();
                    if (!nameNowRow.contains(e.getListKey())) {
                        //没有添加过行号了，先添加行
                        // 向下移动行
                        sheet.shiftRows(e.getRow(), sheet.getLastRowNum(), rowList.size() - 1);
                        for (int i = 0; i < rowList.size() - 1; i++) {
                            Row nowRow = sheet.createRow(e.getRow() + i);
                            //拷贝格式
                            copyRow(row, nowRow);
                        }
                        nameNowRow.add(e.getListKey());
                    }
                    for (int i = 0; i < rowList.size(); i++) {
                        Row line = sheet.getRow(e.getRow() + i);
                        Cell cell = line.getCell(e.getColumn());
                        Object e1 = rowList.get(i);
                        if (ObjectNull.isNotNull(e1)) {
                            writeValue(cell, e1);
                        } else {
                            cell.setBlank();
                        }
                    }
                } else {
                    Cell cell = row.getCell(e.getColumn());
                    write(cell, e);
                }
            });
        }
        workbook.write(fos);
    }


    private static void copyRow(Row sourceRow, Row newRow) {
        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell newCell = newRow.createCell(i);

            if (sourceCell != null) {
                // 复制单元格的样式
                CellStyle newCellStyle = newRow.getSheet().getWorkbook().createCellStyle();
                newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
                newCell.setCellStyle(newCellStyle);

                // 复制单元格的值
                switch (sourceCell.getCellType()) {
                    case STRING:
                        newCell.setCellValue(sourceCell.getStringCellValue());
                        break;
                    case NUMERIC:
                        newCell.setCellValue(sourceCell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        newCell.setCellValue(sourceCell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        newCell.setCellFormula(sourceCell.getCellFormula());
                        break;
                    case BLANK:
                        newCell.setBlank();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void write(Cell cell, ExcelVariable e) {
        String stringCellValue = cell.getStringCellValue();
        if (!stringCellValue.equals(e.getName())) {
            //需要判断原始数据中是否存在其它的变量
            cell.setCellValue(stringCellValue.replace(e.getName(), e.getValue().toString()));
            return;
        }
        switch (e.getType()) {
            //不存在写入 list数据，这里应该根据值的类型进行写入而不是根据单元格的类型写入
            case Number:
                double doub = new BigDecimal(e.getValue().toString()).doubleValue();
                cell.setCellValue(doub);
                break;
            case Date:
                cell.setCellValue((LocalDateTime) e.getValue());
                break;
            case String:
                cell.setCellValue(e.getValue().toString());
                break;
            case Boolean:
            default:
                writeValue(cell, e.getValue());
        }
    }

    private static void writeValue(Cell cell, Object e) {
        if (e instanceof Date) {
            cell.setCellValue(e.toString());
        } else if (e instanceof Number) {
            cell.setCellValue(Double.valueOf(e.toString()));
        } else {
            cell.setCellValue(e.toString());
        }
    }

    private static List<ExcelVariable> openList(List<ExcelVariable> list) {
        return list.stream()
                .filter(e -> ExcelVariable.ExcelType.List.equals(e.getType()))
                .flatMap(e -> {
                    List<ExcelVariable> excelVariables = new ArrayList<>();
                    //转换为 Map
                    List<Map<String, Object>> value = (List) e.getValue();
                    for (Map<String, Object> body : value) {
                        body.keySet().stream()
                                .map(c -> {
                                    List read = value.stream().map(f -> f.get(c)).collect(Collectors.toList());
                                    return new ExcelVariable().setType(ExcelVariable.ExcelType.List).setListKey(e.getName()).setName(e.getName() + "." + c).setValue(read);
                                })
                                .forEach(excelVariables::add);
                    }
                    return excelVariables.stream().distinct();
                })
                .collect(Collectors.toList());
    }

    private static List<ExcelVariable> getList() {
        BigDecimal bigDecimal = new BigDecimal("268181782.03");
        ArrayList<ExcelVariable> objects = new ArrayList<>();
        objects.add(new ExcelVariable().setName("cc").setType(ExcelVariable.ExcelType.Number).setValue(268181782));
        objects.add(new ExcelVariable().setName("a").setType(ExcelVariable.ExcelType.String).setValue("这是测试的 a"));
        objects.add(new ExcelVariable().setName("b").setType(ExcelVariable.ExcelType.Date).setValue(DateUtil.now()));
        objects.add(new ExcelVariable().setName("c").setType(ExcelVariable.ExcelType.Date).setValue(new Date()));
        objects.add(new ExcelVariable().setName("e").setType(ExcelVariable.ExcelType.Date).setValue("2022-12-12"));
        List list = new ArrayList<>();
        list.add(Dict.create().set("name", "abc").set("age", bigDecimal).set("date", DateUtil.date()));
        list.add(Dict.create().set("name", "abc").set("age1", bigDecimal).set("date", DateUtil.date()));
        list.add(Dict.create().set("name", "abc").set("age2", 268181782.03).set("date", DateUtil.date()));
        list.add(Dict.create().set("name", "abc").set("age3", 268181782.03).set("date", DateUtil.date()));
        objects.add(new ExcelVariable().setName("f").setType(ExcelVariable.ExcelType.List).setValue(list));
        return objects;
    }


    /**
     * 内容中的变量
     */
    private static String VARIABLE_PATTERN = "\\$\\{.*\\{.*?\\}\\}";

    public static void replaceVariables(String inputFilePath, String outputFilePath, Map<String, Object> variables) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 遍历每个工作表
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        //todo 这里只处理了字符串，还有其它的数据类型
                        String cellValue = cell.getStringCellValue();
                        // 替换变量
                        for (Map.Entry<String, Object> entry : variables.entrySet()) {
                            String placeholder = entry.getKey();
                            Object replacement = entry.getValue();
//                            cellValue = cellValue.replace(placeholder, replacement);
                        }
                        // 更新单元格的值
                        cell.setCellValue(cellValue);

                        switch (cell.getCellType()) {
                            case STRING:
                            case NUMERIC:
                            case BOOLEAN:
                        }

                        if (cell.getCellType() == CellType.STRING) {

                        }
                    }
                }
            }

            // 写入新的 Excel 文件
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * 替换Excel模板文件内容
     *
     * @param data  文档数据
     * @param sheet sheet数据
     */
    private static void replaceSheet(Map<String, Object> data, Sheet sheet) {
        try {
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                Row row = (Row) rows.next();
                if (row == null) {
                    continue;
                }
                int num = row.getLastCellNum();
                for (int i = 0; i < num; i++) {
                    Cell cell = row.getCell(i);
                    if (ObjectUtil.isNull(cell) || StrUtil.isNotEmpty(cell.getStringCellValue())) {
                        continue;
                    }
                    String text = cell.getStringCellValue();
                    //可能存在多个
                    List<String> list = ReUtil.findAll(VARIABLE_PATTERN, text, 1);
                    if (!list.isEmpty()) {
                        //去重
                        list = list.parallelStream().distinct().collect(Collectors.toList());
                        for (int i1 = 0; i1 < list.size(); i1++) {
                            String key = list.get(i1);
                            String value = "";
                            if (data.containsKey(key)) {
                                value = data.get(key).toString();
                            }
                            String regex = "\\$\\{" + key + "\\}";
                            text = text.replaceAll(regex, value);
                        }
                        cell.setCellValue(text);
                    }
                }
            }
        } catch (Exception e) {
            log.error("内容替换错误!", e);
            throw e;
        }
    }

    /**
     * excel 变量替换
     *
     * @param data        变量结果集
     * @param inputStream 模板输入流
     */
    public static InputStream searchAndReplace(InputStream inputStream, Map<String, Object> data) throws BusinessException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                XSSFSheet sheetAt = workbook.getSheetAt(i);
                //替换内容
                replaceSheet(data, sheetAt);
            }
            //二进制OutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            ByteArrayInputStream in = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
            return in;
        } catch (Exception e) {
            log.error("文本内容替换错误", e);
            throw new BusinessException("文本内容替换错误");
        }
    }

    private static Set<ExcelVariable> searchVariables(XSSFWorkbook workbook) {
        Set<ExcelVariable> variables = new HashSet<>();
        int numberOfSheets = workbook.getNumberOfSheets();

        for (int i = 0; i < numberOfSheets; i++) {
            XSSFSheet sheetAt = workbook.getSheetAt(i);
            variables.addAll(variables(sheetAt, i));
        }

        return variables;
    }

    /**
     * 查询 excel里面的变量有哪些
     *
     * @param inputStream 模板输入流
     */
    public static Set<ExcelVariable> searchVariables(InputStream inputStream) throws BusinessException {
        Set<ExcelVariable> variables = new HashSet<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            int numberOfSheets = workbook.getNumberOfSheets();

            for (int i = 0; i < numberOfSheets; i++) {
                XSSFSheet sheetAt = workbook.getSheetAt(i);
                variables.addAll(variables(sheetAt, i));
            }

        } catch (Exception e) {
            log.error("文本内容替换错误", e);
            throw new BusinessException("文本内容替换错误");
        }
        return variables;
    }

    public static List<Dict> searchData(InputStream inputStream, Map<Object, Map<String, Object>> map) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            List<Dict> data = map.keySet().stream().map(e -> {
                        XSSFSheet sheetAt;
                        if (e instanceof Number) {
                            sheetAt = workbook.getSheetAt(((Number) e).intValue());
                        } else {
                            sheetAt = workbook.getSheet(e.toString());
                        }
                        if (ObjectNull.isNull(sheetAt)) {
                            return null;
                        }
                        //搜索 value
                        Map<String, Object> body = map.get(e);
                        body.keySet().forEach(key -> {
                            String field = body.get(key).toString();
                            //是否是表格
                            if (field.contains("x") || field.contains("X")) {
                                CellReference cellReference = parseCellReference(field.split("x")[0].toLowerCase().replace("r", ""));
                                short col = cellReference.getCol();
                                int row = cellReference.getRow();
                                Integer cell = Integer.valueOf(field.split("x")[1].split("\\(")[0].toString().toLowerCase().replace("c", ""));
                                List<String> fields = Arrays.stream(field.substring(field.indexOf("(") + 1, field.indexOf(")")).split(",")).collect(Collectors.toList());
                                if (fields.size() != cell) {
                                    throw new BusinessException(field + "描述不正确,字段个数与列数必须相同");
                                }
                                List<Object> objectList = new ArrayList<>();

                                List<XSSFCellStyle> list = new ArrayList<>();
                                for (int i = 0; i < col; i++) {
                                    list.add(null);
                                }
                                getList(col, cell, sheetAt, row, list, objectList, fields);
                                body.put(key, objectList);
                            } else {
                                CellReference cellReference = parseCellReference(field.toLowerCase());
                                XSSFCell cell = sheetAt.getRow(cellReference.getRow()).getCell(cellReference.getCol());
                                body.put(key, getCellValue(cell));
                            }
                        });
                        return Dict.create().set(sheetAt.getSheetName(), body);
                    })
                    .filter(ObjectNull::isNotNull)
                    .collect(Collectors.toList());
            return data;
        } catch (Exception e) {
            log.error("解析错误", e);
            throw new BusinessException("Excel 解析错误");
        }
    }

    private static void getList(short col, Integer cell, XSSFSheet sheetAt, int row, List<XSSFCellStyle> list, List<Object> objectList, List<String> fields) {
        while (true) {
            Dict dict = Dict.create();
            for (int i = col; i < cell; i++) {
                //遍历列取值
                XSSFCell cell1 = sheetAt.getRow(row).getCell(i);
                if (list.size() <= i) {
                    list.add(i, cell1.getCellStyle());
                }
                if (areCellStylesSimilar(cell1.getCellStyle(), list.get(i))) {
                    //如果样式一致才继续向下读取，如果不一致则退出读取数据规则
                    dict.set(fields.get(i), getCellValue(cell1, true));
                } else {
                    //这里需要判断是否是最后一个，如不是最后一个需要删除行级数据，，退出
                    return;
                }
            }
            objectList.add(dict);
            row++;
        }
    }

    public static boolean areCellStylesSimilar(XSSFCellStyle style1, XSSFCellStyle style2) {
        // 比较字体
        Font font1 = style1.getFont();
        Font font2 = style2.getFont();

        if (!font1.getFontName().equals(font2.getFontName())) {
            return false;
        }
        if (font1.getFontHeightInPoints() != font2.getFontHeightInPoints()) {
            return false;
        }
        if (font1.getBold() != font2.getBold()) {
            return false;
        }
        if (font1.getColor() != font2.getColor()) {
            return false;
        }

        // 比较对齐方式
        if (style1.getAlignment() != style2.getAlignment()) {
            return false;
        }
        if (style1.getVerticalAlignment() != style2.getVerticalAlignment()) {
            return false;
        }

        // 比较边框
        if (style1.getBorderBottom() != style2.getBorderBottom()) {
            return false;
        }
        if (style1.getBottomBorderColor() != style2.getBottomBorderColor()) {
            return false;
        }

        // 比较填充颜色
        if (style1.getFillForegroundColor() != style2.getFillForegroundColor()) {
            return false;
        }
        if (style1.getFillPattern() != style2.getFillPattern()) {
            return false;
        }

        // 比较数字格式
        if (!style1.getDataFormatString().equals(style2.getDataFormatString())) {
            return false;
        }

        // 其他可以根据需要添加的比较...

        return true; // 如果所有比较都通过，返回 true
    }

    public static int[] parseDimensions(String input) {
        // 使用正则表达式匹配行和列
        String regex = "(\\d+)R\\s*x\\s*(\\d+)C";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 提取行数和列数
            int rows = Integer.parseInt(matcher.group(1));
            int columns = Integer.parseInt(matcher.group(2));
            return new int[]{rows, columns};
        } else {
            return null; // 返回 null 表示格式不匹配
        }
    }

    public static CellReference parseCellReference(String cell) {
        StringBuilder column = new StringBuilder();
        StringBuilder row = new StringBuilder();

        // 遍历输入字符串的每个字符
        for (char ch : cell.toCharArray()) {
            if (Character.isLetter(ch)) {
                // 如果是字母，添加到列部分
                column.append(ch);
            } else if (Character.isDigit(ch)) {
                // 如果是数字，添加到行部分
                row.append(ch);
            }
        }

        return new CellReference(Integer.parseInt(row.toString()) - 1, getColumnIndex(column.toString()));
    }

    public static int getColumnIndex(String column) {
        int index = 0;
        int length = column.length();

        for (int i = 0; i < length; i++) {
            // 将每个字符转换为小写
            char c = Character.toLowerCase(column.charAt(i));
            // 根据字符计算索引
            index = index * 26 + (c - 'a' + 1);
        }

        // 返回索引减去1，因为索引是从0开始的
        return index - 1;
    }


    private static Set<ExcelVariable> variables(XSSFSheet sheet, int sheetI) {
        Set<ExcelVariable> variables = new HashSet<>();
        try {
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext()) {
                Row row = (Row) rows.next();
                if (row == null) {
                    continue;
                }
                int num = row.getLastCellNum();
                for (int i = 0; i < num; i++) {
                    Cell cell = row.getCell(i);
                    if (ObjectNull.isNull(cell)) {
                        continue;
                    }
                    if (ObjectNull.isNull(cell.getCellType())) {
                        continue;
                    }
                    if (isFormulaCell(cell)) {
                        continue;
                    }
                    Object stringCellValue = null;
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            //可能是时间类型，或数字类型
                            stringCellValue = cell.getNumericCellValue();
                            break;
                        case STRING:
                            stringCellValue = cell.getStringCellValue();
                            break;
                        case BOOLEAN:
                            stringCellValue = cell.getBooleanCellValue();
                    }

                    if (ObjectUtil.isNull(cell)) {
                        continue;
                    }
                    if (!(stringCellValue instanceof String)) {
                        continue;
                    }
                    String text = cell.getStringCellValue();
                    //可能存在多个
                    List<String> list = ReUtil.findAll(VARIABLE_PATTERN, text, 0);
                    if (!list.isEmpty()) {
                        list.forEach(a -> {
                            Arrays.stream(a.split(" ")).forEach(e -> {
                                variables.add(new ExcelVariable().setName(e).setCellStyle(cell.getCellStyle()).setType(ExcelVariable.ExcelType.String)
                                        .setSheet(sheetI)
                                        .setRow(row.getRowNum())
                                        .setColumn(cell.getColumnIndex()));
                            });
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("内容替换错误!", e);
            throw e;
        }
        return variables;
    }

    private static boolean isFormulaCell(Cell cell) {
        // 判断单元格是否为公式
        return cell != null && cell.getCellType() == CellType.FORMULA;
    }


}
