package cn.bctools.document.office.excel;

import cn.bctools.common.exception.BusinessException;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class ExcelVariablesReplaceUtil {
    /**
     * 内容中的变量
     */
    private static String VARIABLE_PATTERN = "\\$\\{(.*?)\\}";

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
}
