package cn.bctools.rule.tools.file;

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
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zx
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "zip压缩",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 18,
//        iconUrl = "rule-a-wenjianjiawenjian",
        explain = "可将多个文件对象打包为一个压缩包"
)
public class ZipFileServiceImpl implements BaseCustomFunctionInterface<ZipFileDto> {

    OssTemplate ossTemplate;

    @Override
    public Object execute(ZipFileDto dto, Map<String, Object> params) {
        List<String> fileNames = dto.getFiles().stream().map(e -> ObjectNull.isNull(e.getOriginalName()) ? e.getName() : e.getOriginalName()).collect(Collectors.toList());
        //压缩文件可能存在同名的情况下对名称进行重命名
        for (int i = 0; i < fileNames.size() - 1; i++) {
            for (int j = i + 1; j < fileNames.size(); j++) {
                if (fileNames.get(i).equals(fileNames.get(j))) {
                    fileNames.set(i, (j + "_" + fileNames.get(i)));
                }
            }
        }
        List<ByteArrayInputStream> byteArrayInputStreams = dto.getFiles().stream().map(e -> ossTemplate.fileLink(e.getFileName(), e.getBucketName()))
                .map(e -> new ByteArrayInputStream(HttpUtil.downloadBytes(e)))
                .collect(Collectors.toList());
        //获取输接受文件
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipUtil.zip(out, fileNames.toArray(new String[0]), byteArrayInputStreams.toArray(new ByteArrayInputStream[0]));
        //直接返回流
        InputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
        String module = RuleConstant.OSS_BUCKET_NAME_PATH + "file";
        BaseFile baseFile = ossTemplate.put(OssSystemCons.OSS_BUCKET_NAME, module, byteArrayInputStream, dto.getName() + ".zip", true);
        String url = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
        String s = ossTemplate.fileLink(baseFile.getFileName(), OssSystemCons.OSS_BUCKET_NAME);
        return new RuleFile().setBucketName(OssSystemCons.OSS_BUCKET_NAME)
                .setSize(baseFile.getSize())
                .setFileName(baseFile.getFileName())
                .setOutputType(OutputType.download)
                .setModule(module)
                .setFileType(".zip")
                .setOriginalName(dto.getName() + ".zip")
                .setName(dto.getName() + ".zip")
                .setPreviewUrl(s)
                .setUrl(url);
    }

}
