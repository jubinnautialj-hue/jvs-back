package cn.bctools.rule.tools.uid;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * @author jvs
 * @describe 生成平台唯一编码
 */
@Rule(value = "生成平台唯一编码",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 32,
        iconUrl = "rule-dian",
        explain = "根据填写的进制，生成唯一性编码"

)
public class UUidUtilsServiceImpl implements BaseCustomFunctionInterface<UidStrDto> {

    @Override
    @SneakyThrows
    public Object execute(UidStrDto dto, Map<String, Object> params) {
        if (dto.getRadix() > 36) {
            throw new BusinessException("超出长度");
        }
        return IdGenerator.getIdStr(dto.getRadix());
    }

}
