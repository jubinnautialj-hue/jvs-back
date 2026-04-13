package cn.bctools.rule.tools.file;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
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
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.URLUtil;
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
        List<String> fileNames;
        List<ByteArrayInputStream> byteArrayInputStreams;
        if (ObjectNull.isNotNull(dto.getFiles())) {
            if (dto.getFiles().get(0) instanceof String) {
                fileNames = (List<String>) dto.getFiles().stream().map(e -> FileNameUtil.getName(e.toString())).collect(Collectors.toList());
                byteArrayInputStreams = (List<ByteArrayInputStream>) dto.getFiles().stream()
                        .map(e -> new ByteArrayInputStream(HttpUtil.downloadBytes(e.toString())))
                        .collect(Collectors.toList());
            } else {
                fileNames = (List<String>) dto.getFiles().stream().map(e -> ObjectNull.isNull(JvsJsonPath.read(e, "originalName")) ? JvsJsonPath.read(e, "fileName").toString() : JvsJsonPath.read(e, "originalName").toString()).collect(Collectors.toList());
                byteArrayInputStreams = (List<ByteArrayInputStream>) dto.getFiles().stream().map(e -> ossTemplate.fileLink(JvsJsonPath.read(e, "fileName").toString(), JvsJsonPath.read(e, "bucketName").toString()))
                        .map(e -> new ByteArrayInputStream(HttpUtil.downloadBytes(e.toString()))).collect(Collectors.toList());
            }
        } else {
            throw new BusinessException("文件链接必须填写");
        }
        // 先提取纯文件名,去除可能包含的路径
        fileNames = fileNames.stream()
                .map(FileNameUtil::getName)
                .map(URLUtil::decode)
                .collect(Collectors.toList());
        //压缩文件可能存在同名的情况下对名称进行重命名
        for (int i = 0; i < fileNames.size() - 1; i++) {
            for (int j = i + 1; j < fileNames.size(); j++) {
                if (fileNames.get(i).equals(fileNames.get(j))) {
                    fileNames.set(i, (j + "_" + fileNames.get(i)));
                }
            }
        }
        //统一添加ZIP包内目录前缀,确保所有文件在同一目录下
        String zipDirName = dto.getName() + "/";
        fileNames = fileNames.stream()
                .map(name -> zipDirName + name)
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
