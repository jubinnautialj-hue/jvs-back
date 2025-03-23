package cn.bctools.report.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.model.univer.USheet;
import cn.bctools.report.model.univer.UWorkbook;
import cn.bctools.report.model.univer.conf.USheetPluginDTO;
import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.render.RenderFactory;
import cn.bctools.report.service.JvsDataReportService;
import cn.bctools.report.utils.ExcelToPdfUtil;
import cn.bctools.report.utils.ExcelUtils;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itextpdf.text.DocumentException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "文件接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final JvsDataReportService jvsDataReportService;
    private final RenderFactory renderFactory;

    @GetMapping("/export/{id}")
    public void export(HttpServletResponse response, @PathVariable("id") String id) {
        JvsDataReport report = jvsDataReportService.getById(id);
        Assert.notNull(report,() -> new BusinessException("未找到报表"));
        renderFactory.compute(report);
        UWorkbook reportDesign = report.getReportDesign();

        //提取univer样式数据
        Map<String, UStyle> styles = reportDesign.getStyles();
        //提取univer工作表
        Map<String, USheet> sheets = reportDesign.getSheets();
        //提取浮动图片资源
        List<USheetPluginDTO> resources = reportDesign.getResources();

        USheetPluginDTO uSheetPluginDTO = resources.stream().filter(e -> "SHEET_DRAWING_PLUGIN".equals(e.getName())).findFirst().get();

        JSONObject drawingMap = JSONUtil.parseObj(uSheetPluginDTO.getData());

        Map<String, String> resourceMap = resources.stream().collect(Collectors.toMap(USheetPluginDTO::getName, USheetPluginDTO::getData));
        // 创建一个新的工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map<String, XSSFCellStyle> xssStyle = styles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> ExcelUtils.convertStyle(workbook, e.getValue())));

        sheets.forEach((key, value) -> ExcelUtils.createSheet(workbook, value, xssStyle,drawingMap,resourceMap));

        // 设置响应头
        try {
            String fileName = report.getReportName()+ ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            ServletOutputStream outputStream = response.getOutputStream();

            // 将工作簿写入文件
            workbook.write(outputStream);

            log.info("生成excel完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭工作簿
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("工作簿关闭失败");
            }
        }
    }

    @GetMapping("/export/pdf/{id}")
    public void exportPDF(HttpServletResponse response, @PathVariable("id") String id) {
        JvsDataReport report = jvsDataReportService.getById(id);
        Assert.notNull(report,() -> new BusinessException("未找到报表"));
        renderFactory.compute(report);
        UWorkbook reportDesign = report.getReportDesign();

        //提取univer样式数据
        Map<String, UStyle> styles = reportDesign.getStyles();
        //提取univer工作表
        Map<String, USheet> sheets = reportDesign.getSheets();
        //提取浮动图片资源
        List<USheetPluginDTO> resources = reportDesign.getResources();

        USheetPluginDTO uSheetPluginDTO = resources.stream().filter(e -> "SHEET_DRAWING_PLUGIN".equals(e.getName())).findFirst().get();

        JSONObject drawingMap = JSONUtil.parseObj(uSheetPluginDTO.getData());

        Map<String, String> resourceMap = resources.stream().collect(Collectors.toMap(USheetPluginDTO::getName, USheetPluginDTO::getData));
        // 创建一个新的工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        Map<String, XSSFCellStyle> xssStyle = styles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> ExcelUtils.convertStyle(workbook, e.getValue())));

        sheets.forEach((key, value) -> ExcelUtils.createSheet(workbook, value, xssStyle,drawingMap,resourceMap));

        // 设置响应头
        try {
            String fileName = report.getReportName()+ ".pdf";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            ServletOutputStream outputStream = response.getOutputStream();

            XSSFSheet sheet = workbook.getSheetAt(0);
            ExcelToPdfUtil.excelToPdf(sheet,outputStream);

            log.info("生成excel完成");
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭工作簿
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("工作簿关闭失败");
            }
        }
    }
}
