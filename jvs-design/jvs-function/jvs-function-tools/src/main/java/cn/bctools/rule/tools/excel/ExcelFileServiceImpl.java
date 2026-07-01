package cn.bctools.rule.tools.excel;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oss.cons.OssSystemCons;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.OutputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "生成Excel",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 15,
//        iconUrl = "rule-excel",
        explain = "将传入的数据导出为Excel"
)
public class ExcelFileServiceImpl implements BaseCustomFunctionInterface<ExcelFileDto> {

    OssTemplate ossTemplate;

    @Override
    public Object execute(ExcelFileDto dto, Map<String, Object> params) {
        //自定义标题别名
        Map<String, String> titleName = dto.getTitleName();
        // 通过工具类创建writer，默认创建xls格式
        File destFile = new File(IdWorker.get32UUID() + ".xlsx");
        ExcelWriter writer = ExcelUtil.getWriter(destFile);

        writer.merge(titleName.size(), dto.getSheet(), true);
        for (String s : titleName.keySet()) {
            writer.addHeaderAlias(s, titleName.get(s));
        }
        //如果标题不为空
        if (ObjectNull.isNotNull(dto.getSheet())) {
            //寻找标题是否有此数据,如果有,则分多sheet  如果没有,只重命名一个名称
            Map<String, List<Map<String, Object>>> collect = dto.getBody().stream().collect(Collectors.groupingBy(e ->
                    e.getOrDefault(dto.getSheet(), "未分类").toString()
            ));
            //获取多个sheet的数据
            if (collect.keySet().size() > 1) {
                //表示多个
                int i = 0;
                //根据分组选多个
                for (String o : collect.keySet()) {

                    List<Map<String, Object>> maps = collect.get(o);
                    writer.setSheet(i++);
                    writer.getSheet().setDefaultRowHeight((short) 500);
                    writer.merge(0, 0, 0, maps.size() + 1, o, true);
                    writer.renameSheet(o);
                    writer.passCurrentRow();
                    writer.passCurrentRow();
                    writer.write(maps, true);
                    //自适应列
                    setSizeColumn(writer.getSheet(), maps.size() + 1);
                }
            } else {
                writer.setRowHeight(-1, 500);
                writer.write(dto.getBody(), true);
                //设置
                writer.renameSheet(dto.getSheet());
            }
        } else {
            writer.setRowHeight(-1, 500);
            writer.write(dto.getBody(), true);
            //自适应列
            setSizeColumn(writer.getSheet(), dto.getBody().size() + 1);
            //设置
            writer.renameSheet(dto.getSheet());
        }
        writer.flush();
        writer.close();

        //获取 输出的byte[]
        String originalName = dto.getName() + ".xlsx";
        String module = RuleConstant.OSS_BUCKET_NAME_PATH + "excel";
        try {
            BaseFile put = ossTemplate.putFile(OssSystemCons.OSS_BUCKET_NAME, originalName, new FileInputStream(destFile), module);
            String url = ossTemplate.fileLink(put.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
            return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                    .setSize(put.getSize())
                    .setFileName(put.getFileName())
                    .setName(originalName)
                    .setOutputType(OutputType.download)
                    .setModule(module)
                    .setFileType(".xlsx")
                    .setOriginalName(originalName + StrUtil.DOT + "xlsx")
                    .setUrl(url);
        } catch (FileNotFoundException e) {
            throw new BusinessException("逻辑生成Excel转换错误");
        } finally {
            destFile.delete();
        }

    }

    /**
     * 自适应宽度(中文支持)
     *
     * @param sheet sheet
     * @param size  因为for循环从0开始，size值为 列数-1
     */
    public static void setSizeColumn(Sheet sheet, int size) {
        for (int columnNum = 0; columnNum <= size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }

                if (currentRow.getCell(columnNum) != null) {
                    Cell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == CellType.STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(columnNum, columnWidth * 256);
        }
    }

}
