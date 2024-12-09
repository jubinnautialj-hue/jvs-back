package cn.bctools.rule.tools.random;

import cn.bctools.database.util.IdGenerator;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: GuoZi
 */
@Service
@AllArgsConstructor
@Rule(value = "生成唯一ID",
        group = RuleGroup.工具插件,
        explain = "生成唯一ID",
        test = true,
        order = 0,
        returnType = ClassType.文本,
//        iconUrl = "rule-ziyuan",
        testShowEnum = TestShowEnum.TEXT
)
public class IdNumberServiceImpl implements BaseCustomFunctionInterface {

    @Override
    public Object execute(Object o, Map params) {
        return IdGenerator.getIdStr();
    }

}
