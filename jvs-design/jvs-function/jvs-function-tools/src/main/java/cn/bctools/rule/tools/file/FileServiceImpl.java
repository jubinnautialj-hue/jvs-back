package cn.bctools.rule.tools.file;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "文件预览下载",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文件,
        testShowEnum = TestShowEnum.JSON,
        order = 16,
//        iconUrl = "rule-a-wenjianjiawenjian",
        explain = "最后一个节点使用时，可对传入的文件进行预览或者下载。"
)
public class FileServiceImpl implements BaseCustomFunctionInterface<FileDto> {

    @Override
    public Object execute(FileDto dto, Map<String, Object> params) {
        return new RuleFile()
                .setName(dto.getFileName())
                .setOutputType(dto.getFileType())
                .setOriginalName(dto.getFileName())
                .setUrl(dto.getFileUrl());
    }

}
