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
import cn.hutool.core.io.file.FileNameUtil;
import com.amazonaws.util.Base64;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "Base64转文件",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "Base64转文件"
)
public class Base64ConvertFileServiceImpl implements BaseCustomFunctionInterface<Base64ConvertFileDto> {
    OssTemplate ossTemplate;

    @Override
    public Object execute(Base64ConvertFileDto dto, Map<String, Object> params) {
        //直接返回流
        byte[] serialize = Base64.decode(dto.getBase64());
        String prefix = "." + FileNameUtil.getSuffix(dto.getName());
        InputStream byteArrayInputStream = new ByteArrayInputStream(serialize);
        String module = RuleConstant.OSS_BUCKET_NAME_PATH + "file";
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, module, byteArrayInputStream, dto.getName(), true);
        String url = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
        String s = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setSize(baseFile.getSize())
                .setFileName(baseFile.getFileName())
                .setOutputType(OutputType.download)
                .setModule(module)
                .setFileType(prefix)
                .setOriginalName(dto.getName())
                .setName(dto.getName())
                .setPreviewUrl(s)
                .setUrl(url);
    }
}
