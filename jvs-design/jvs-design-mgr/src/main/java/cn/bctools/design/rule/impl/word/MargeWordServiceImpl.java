package cn.bctools.design.rule.impl.word;

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
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@Rule(value = "文档合并",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 8,
//        iconUrl = "rule-Messages",
        explain = "多个文档进行合并处理，组合成一个文档"
)
@AllArgsConstructor
public class MargeWordServiceImpl implements BaseCustomFunctionInterface<MargeWordDto> {

    OssTemplate ossTemplate;

    @SneakyThrows
    @Override
    public Object execute(MargeWordDto wordDto, Map<String, Object> params) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MargeDoc.mergeDocx(wordDto.getFileUrls(), stream, wordDto.getNewPage());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stream.toByteArray());
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, RuleConstant.OSS_BUCKET_NAME_PATH + "word", byteArrayInputStream, wordDto.getFileName() + StrUtil.DOT + "docx", true);
        String fileLink = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setFileName(baseFile.getFileName())
                .setSize(baseFile.getSize())
                .setName(wordDto.getFileName())
                .setOutputType(OutputType.download)
                .setModule(RuleConstant.OSS_BUCKET_NAME_PATH + "docx")
                .setFileType(StrUtil.DOT + "docx")
                .setOriginalName(wordDto.getFileName() + StrUtil.DOT + "docx")
                .setUrl(fileLink);
    }

}
