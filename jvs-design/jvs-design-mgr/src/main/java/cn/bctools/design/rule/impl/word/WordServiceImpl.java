package cn.bctools.design.rule.impl.word;

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
import cn.bctools.word.utils.ExcelVariablesReplaceUtil;
import cn.bctools.word.utils.WordPdfUtil;
import cn.bctools.word.utils.WordVariableReplaceUtil;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "文档模板替换",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 9,
//        iconUrl = "rule-Messages",
        explain = "替换模板文件中对应标识的字段，返回转换完成的文件的地址、文件名等数据。支持 xlsx、docx、pdf输出。变量名：文字类型 {key1}、{key2} 支持图片自定义宽高。格式：${IMAGE#key1?w=200&h=100}  key1 key2 为变量名"
)
@AllArgsConstructor
public class WordServiceImpl implements BaseCustomFunctionInterface<WordDto> {

    OssTemplate ossTemplate;
    static final String DOCX = "docx";
    static final String XLSX = "xlsx";
    static final String DOC = "doc";
    static final String PDF = "pdf";

    static Cache<String, byte[]> fileByte = CacheUtil.newFIFOCache(500, 10 * 60 * 1000);


    @SneakyThrows
    @Override
    public Object execute(WordDto wordDto, Map<String, Object> params) {
        if (ObjectNull.isNull(wordDto.getFileType())) {
            wordDto.setFileType(DOCX);
        }
        byte[] templateBytes = fileByte.get(wordDto.getFileUrl(), () -> HttpUtil.downloadBytes(wordDto.getFileUrl()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (wordDto.getFileType().equals(XLSX)) {
            //表示 excel输出
            ExcelVariablesReplaceUtil.writeExcel(wordDto.getBody(), new ByteArrayInputStream(templateBytes), outputStream);
        } else {
            wordDto.setFileName(wordDto.getFileName() + StrUtil.DOT + wordDto.getFileType());
            String fileName = wordDto.getFileUrl();
            fileName = fileName.substring(fileName.lastIndexOf(StrUtil.DOT) + 1);
            if (!fileName.contains(DOC)) {
                throw new BusinessException("文件链接格式不对必须是doc或docx");
            }


            WordprocessingMLPackage template = WordVariableReplaceUtil.template(templateBytes, wordDto.getBody());
            if (PDF.equals(wordDto.getFileType())) {
                WordPdfUtil.convertDocx2Pdf(template, outputStream);
            } else {
                WordPdfUtil.convertDocx2Docx(template, outputStream);
            }
        }
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "word", byteArrayInputStream, wordDto.getFileName() + StrUtil.DOT + wordDto.getFileType(), true);
        String fileLink = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setSize(baseFile.getSize())
                .setFileName(baseFile.getFileName())
                .setName(wordDto.getFileName())
                .setOutputType(OutputType.download)
                .setModule(RuleConstant.OSS_BUCKET_NAME_PATH + wordDto.getFileType())
                .setFileType(StrUtil.DOT + wordDto.getFileType())
                .setOriginalName(baseFile.getOriginalName())
                .setUrl(fileLink);
    }

}
