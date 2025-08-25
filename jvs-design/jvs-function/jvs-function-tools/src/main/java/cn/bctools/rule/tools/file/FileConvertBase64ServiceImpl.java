package cn.bctools.rule.tools.file;

import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "文件转Base64",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "文件转Base64"
)
public class FileConvertBase64ServiceImpl implements BaseCustomFunctionInterface<FileConvertBase64Dto> {
    OssTemplate ossTemplate;

    @SneakyThrows
    @Override
    public Object execute(FileConvertBase64Dto dto, Map<String, Object> params) {
        //直接返回流
        String url = null;
        if (dto.getUrl() instanceof String) {
            url = dto.getUrl().toString();

        } else if (dto.getUrl() instanceof RuleFile) {
            url = ossTemplate.fileLink(((RuleFile) dto.getUrl()).getFileName(), ((RuleFile) dto.getUrl()).getBucketName());
        }

        String encodedData = new String(Base64.encode(HttpUtil.downloadBytes(url)));
        String cleanPath = new URI(url).getPath();
        String mimeType = FileUtil.getMimeType(cleanPath);
        return "data:" + mimeType + ";base64," + encodedData;
    }
}
