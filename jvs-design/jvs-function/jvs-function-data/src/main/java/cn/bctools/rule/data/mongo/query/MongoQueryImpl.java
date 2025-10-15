package cn.bctools.rule.data.mongo.query;

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
@Rule(value = "Mongo查询",
             group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.数组,
        testShowEnum = TestShowEnum.JSON,
        order = 43,
        explain = "Mongo查询"
)
public class MongoQueryImpl implements BaseCustomFunctionInterface<QueryDto> {

    private final MongoCustomTemplate mongoCustomTemplate;

    private final ModelInterface modelInterface;

    @Override
    public Object execute(QueryDto queryDto, Map<String, Object> params) {
        Object byKey = modelInterface.getByKey(queryDto.getOptions());
        MongoDBOption mongoDBOption = BeanCopyUtil.copy(MongoDBOption.class, byKey);
        return mongoCustomTemplate.list(mongoDBOption, queryDto.getTableName(), queryDto.getQueryArgs());
    }
}
