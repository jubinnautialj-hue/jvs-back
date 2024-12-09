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
import cn.bctools.word.utils.WordPdfUtil;
import cn.bctools.word.utils.WordVariableReplaceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
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
        testShowEnum = TestShowEnum.TEXT,
        order = 9,
//        iconUrl = "rule-Messages",
        explain = "替换模板文件中对应标识的字段，返回转换完成的文件的地址、文件名等数据。"
)
@AllArgsConstructor
public class WordServiceImpl implements BaseCustomFunctionInterface<WordDto> {

    OssTemplate ossTemplate;
    static final String DOCX = "docx";
    static final String DOC = "doc";
    static final String PDF = "pdf";

    static Cache<String, byte[]> fileByte = CacheUtil.newFIFOCache(500, 10 * 60 * 1000);


    @Override
    public Object execute(WordDto wordDto, Map<String, Object> params) {
        if (ObjectNull.isNull(wordDto.getFileType())) {
            wordDto.setFileType(DOCX);
        }
        wordDto.setFileName(wordDto.getFileName() + StrUtil.DOT + wordDto.getFileType());
        String fileName = wordDto.getFileUrl();
        fileName = fileName.substring(fileName.lastIndexOf(StrUtil.DOT) + 1);
        if (!fileName.contains(DOC)) {
            throw new BusinessException("文件链接格式不对必须是doc或docx");
        }

        byte[] templateBytes = fileByte.get(wordDto.getFileUrl(), () -> HttpUtil.downloadBytes(wordDto.getFileUrl()));

        WordprocessingMLPackage template = WordVariableReplaceUtil.template(templateBytes, wordDto.getBody());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (PDF.equals(wordDto.getFileType())) {
            WordPdfUtil.convertDocx2Pdf(template, outputStream);
        } else {
            WordPdfUtil.convertDocx2Docx(template, outputStream);
        }
        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "word", byteArrayInputStream, wordDto.getFileName() + StrUtil.DOT + wordDto.getFileType(), true);
        String fileLink = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setFileName(baseFile.getFileName())
                .setName(wordDto.getFileName())
                .setOutputType(OutputType.download)
                .setModule(RuleConstant.OSS_BUCKET_NAME_PATH + wordDto.getFileType())
                .setFileType(StrUtil.DOT + wordDto.getFileType())
                .setOriginalName(wordDto.getFileName())
                .setUrl(fileLink);
    }

}
