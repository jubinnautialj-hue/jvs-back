package cn.bctools.document.office.pdf;


import cn.bctools.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * pdf文件合并 但是目前只支持
 */
@Slf4j
public class PdfMergeDocUtils {

    /**
     * 参数1:需要合并的pdf的文件对象list
     * 参数2:合并之后pdf存储的全路径file对象
     */
    public static void mergePdf(List<InputStream> fileList, File outFile) throws Exception {
        // pdf合并工具类
        PDFMergerUtility mergePdf = new PDFMergerUtility();
        for (int i = 0; i < fileList.size(); i++) {
            mergePdf.addSource(fileList.get(i));
        }
        // 设置合并生成pdf文件
        mergePdf.setDestinationFileName(outFile.getPath());
        // 合并pdf
        try {
            MemoryUsageSetting memoryUsageSetting = MemoryUsageSetting.setupMainMemoryOnly();
            mergePdf.mergeDocuments(memoryUsageSetting);
        } catch (Exception e) {
            log.info("合并pdf错误", e);
            throw new BusinessException("合并pdf错误");
        }
    }
}



