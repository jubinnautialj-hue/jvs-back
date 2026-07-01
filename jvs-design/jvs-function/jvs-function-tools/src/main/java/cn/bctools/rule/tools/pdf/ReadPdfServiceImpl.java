package cn.bctools.rule.tools.pdf;

import cn.bctools.common.utils.IdGenerator;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Pdf解析",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 10,
        explain = "根据提供的地址解析 pdf文件"

)
public class ReadPdfServiceImpl implements BaseCustomFunctionInterface<ReadPdfDto> {
    private static String DELETE_SYMBOL = "[^\\u4e00-\\u9fa50-9a-zA-Z]";

    @Override
    public Object execute(ReadPdfDto readPdfDto, Map<String, Object> params) {
        File destFile = new File(IdGenerator.getIdStr());
        HttpUtil.downloadFile(readPdfDto.getUrl(), destFile);
        String read = read(destFile);
        destFile.delete();
        return read;
    }


    public static String read(File file) {
        StringBuilder result = new StringBuilder();
        try {
            PDFParser parser = new PDFParser(new RandomAccessBuffer(new FileInputStream(file)));
            parser.parse();
            PDDocument doc = parser.getPDDocument();
            PDFTextStripper textStripper = new PDFTextStripper();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                textStripper.setStartPage(i);
                textStripper.setEndPage(i);
                //按顺序行读
                textStripper.setSortByPosition(true);
                String s = textStripper.getText(doc);
                //减少开支
                if (StrUtil.isNotBlank(s)) {
                    s = s.replaceAll(DELETE_SYMBOL, "");
                    if (StrUtil.isNotBlank(s)) {
                        result.append(s);
                    }
                }

            }
            doc.close();
            return result.toString();
        } catch (Exception e) {
            log.info("获取文件内容失败!");
            return null;
        }
    }
}
