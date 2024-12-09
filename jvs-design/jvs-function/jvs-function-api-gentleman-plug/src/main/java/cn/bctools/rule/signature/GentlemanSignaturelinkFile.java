package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignaturelinkFileDto;

import java.util.Map;

/**
 * The type Gentleman signaturelink file.
 *
 * @author jvs
 */
@Rule(value = "获取文件下载链接",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "获取最新的合同文件下载地址，可能文件还没有签署完成，建议合同签署完成后再获取下载地址。下载链接地址有效期是30分钟，超过30分钟需要重新请求下载链接地址\n" +
                "\n"
)
@AllArgsConstructor
public class GentlemanSignaturelinkFile implements BaseCustomFunctionInterface<GentlemanSignaturelinkFileDto> {

    @Override
    public Object execute(GentlemanSignaturelinkFileDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/linkFile", dto);
    }

}
