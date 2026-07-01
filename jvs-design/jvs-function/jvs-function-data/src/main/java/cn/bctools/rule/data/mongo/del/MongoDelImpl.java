package cn.bctools.rule.data.mongo.del;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.mongo.MongoDBOption;
import cn.bctools.rule.data.mongo.mongodb.MongoCustomTemplate;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Rule(value = "Mongo删除",
             group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.布尔,
        testShowEnum = TestShowEnum.JSON,
        order = 43,
        explain = "Mongo删除"
)
public class MongoDelImpl implements BaseCustomFunctionInterface<DelDto> {

    private final MongoCustomTemplate mongoCustomTemplate;
    private final ModelInterface modelInterface;

    @Override
    public Object execute(DelDto delDto, Map<String, Object> params) {
        Object byKey = modelInterface.getByKey(delDto.getOptions());
        MongoDBOption mongoDBOption = BeanCopyUtil.copy(MongoDBOption.class, byKey);
        mongoCustomTemplate.del(mongoDBOption, delDto.getTableName(), delDto.getQueryArgs());
        return true;
    }
}
