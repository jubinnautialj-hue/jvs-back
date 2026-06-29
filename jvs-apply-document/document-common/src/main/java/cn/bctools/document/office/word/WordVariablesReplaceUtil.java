package cn.bctools.document.office.word;

import cn.bctools.common.exception.BusinessException;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;


@Slf4j
public class WordVariablesReplaceUtil {

    public static InputStream searchAndReplace(InputStream inputStream, Map<String, Object> model) {
        try {
            ZipSecureFile.setMinInflateRatio(-1.0d);
            ConfigureBuilder builder = Configure.builder();
            builder.setValidErrorHandler(new Configure.DiscardHandler());
            XWPFTemplate template = XWPFTemplate.compile(inputStream, builder.build()).render(model);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            template.write(outputStream);
            ByteArrayInputStream in = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
            return in;
        } catch (Exception exception) {
            log.info("错误信息,", exception);
            throw new BusinessException(exception.getMessage());
        }
    }


}
