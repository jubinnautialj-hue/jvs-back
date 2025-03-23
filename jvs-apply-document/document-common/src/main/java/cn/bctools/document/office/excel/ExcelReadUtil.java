package cn.bctools.document.office.excel;

import cn.bctools.common.exception.BusinessException;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExcelReadUtil {
    private static Workbook workbook;
    private static Sheet sheet;
    private static Row row;


    /**
     * @param inputStream 文件流
     * @description: 读取excel数据
     * @date: 2021/7/8 15:12
     */
    public static List<String> readExcel(InputStream inputStream, String nameSuffix) {
        if (inputStream == null) {
            return null;
        }
        List<String> resultList = new ArrayList<>();
        try {
            // 通过poi解析流中的workbook
            if ("xls".equals(nameSuffix)) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                return readExcel(inputStream);
            }
            // 获取所有workbook中的sheet工作表数量
            int number = workbook.getNumberOfSheets();
            for (int i = 0; i < number; i++) {
                // 获取到每一个sheet工作表
                sheet = workbook.getSheetAt(i);
                // 根据sheet获取数据
                List<String> data = getData(sheet);
                resultList.addAll(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("获取excel内容错误", e);
        }
        return resultList;
    }


    public static List<String> readExcel(InputStream inputStream) {
        List<String> cellContents = new ArrayList<>();
        try {
            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();

            XMLReader parser = XMLReaderFactory.createXMLReader();
            ContentHandler handler = new MySheetHandler(sst, cellContents);
            parser.setContentHandler(handler);

            Iterator<InputStream> sheets = r.getSheetsData();
            while (sheets.hasNext()) {
                InputStream sheet = sheets.next();
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource);
                sheet.close();
            }
            pkg.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cellContents;
    }

    @Slf4j
    private static class MySheetHandler extends DefaultHandler {

        private StringBuilder cellContent;
        private boolean isCell;
        private boolean isString;
        private List<String> cellContents;
        private SharedStringsTable sharedStringsTable;

        public MySheetHandler(SharedStringsTable sharedStringsTable, List<String> cellContents) {
            this.sharedStringsTable = sharedStringsTable;
            this.cellContents = cellContents;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("c")) { // 表示开始处理单元格
                String cellType = attributes.getValue("t");
                isString = cellType != null && cellType.equals("s"); // 如果 t 属性存在且值为 "s"，表示单元格内容是字符串
                cellContent = new StringBuilder();
                isCell = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String value;
            if (qName.equals("c")) {
                isCell = false;
                if (isString) {
                    // 如果单元格内容是字符串，根据索引获取共享字符串表中的对应值
                    int index = Integer.parseInt(cellContent.toString());
                    RichTextString itemAt = sharedStringsTable.getItemAt(index);
                    value = itemAt.getString();
                } else {
                    // 如果单元格内容不是字符串，直接输出
                    value = cellContent.toString();
                }
                if (StrUtil.isNotBlank(value)) {
                    value = value.replaceAll("[^\\u4e00-\\u9fa50-9a-zA-Z]", "");
                    if (StrUtil.isNotBlank(value)) {
                        log.info("读取到内容为:{}", value);
                        cellContents.add(value);
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isCell) {
                cellContent.append(ch, start, length);
            }
        }

    }

    public static List<String> getData(Sheet sheet) {
        List<String> list = new ArrayList<>();
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        if (rowNum > 0) {
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                Row firstRow = sheet.getRow(i - 1);
                //读取数据从1开始
                row = sheet.getRow(i);
                if (ObjectUtil.isNull(firstRow) || ObjectUtil.isNull(row)) {
                    continue;
                }
                for (short colIx = row.getFirstCellNum(); colIx < row.getLastCellNum(); colIx++) {
                    Cell cell = row.getCell(colIx);
                    if (cell == null) {
                        continue;
                    }
                    list.add(getCellFormatValue(cell));
                }
            }
        }
        return list;
    }


    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private static String getCellFormatValue(Cell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            CellType cellType = cell.getCellType();
            switch (cellType) {
                // 如果当前Cell的Type为NUMERIC
                case NUMERIC:
                case FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }
}
