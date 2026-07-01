package cn.bctools.rule.tools.file;

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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "生成文件",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 17,
//        iconUrl = "rule-a-wenjianjiawenjian",
        explain = "使用传入的文件内容生成一个文件对象"
)
public class GenerateFileServiceImpl implements BaseCustomFunctionInterface<GenerateFileDto> {

    OssTemplate ossTemplate;

    @Override
    public Object execute(GenerateFileDto dto, Map<String, Object> params) {
        //直接返回流
        byte[] serialize = dto.getText().getBytes(Charset.defaultCharset());
        InputStream byteArrayInputStream = new ByteArrayInputStream(serialize);
        String module = RuleConstant.OSS_BUCKET_NAME_PATH + "file";
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, module, byteArrayInputStream, dto.getName() + ".txt", true);
        String url = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
        String s = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setSize(baseFile.getSize())
                .setFileName(baseFile.getFileName())
                .setOutputType(OutputType.download)
                .setModule(module)
                .setFileType(".txt")
                .setOriginalName(dto.getName() + ".txt")
                .setName(dto.getName() + ".txt")
                .setPreviewUrl(s)
                .setUrl(url);
    }

}
