package cn.bctools.rule.data.mongo.add;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.mongo.MongoDBOption;
import cn.bctools.rule.data.mongo.mongodb.MongoCustomTemplate;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
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
@Rule(value = "Mongo插入",
        group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.布尔,
        testShowEnum = TestShowEnum.JSON,
        order = 43,
        explain = "Mongo插入"
)
public class MongoInsertImpl implements BaseCustomFunctionInterface<InsertDto> {

    private final MongoCustomTemplate mongoCustomTemplate;
    private final ModelInterface modelInterface;

    /**
     * {"options":"2fcb53b59dd56f4584acbbccaaa3fb4fcd757bb4ec1f8da955b8c4c6e16c259d1f8b8094d2c9d989ebcf9a37e1e0b3f4845777e1988f672870843d4fe44df114742b3e1f272d20342068f14a290c91651313d11a8ab050891176f6b26871e7f627d520ce66a9189a74caa36126e8fc7d01c4465efcfcd00f69a0f6bba71177a9257c51b8928abc57dda045260b826bb5"
     * ,"tableName":"test"
     * ,"body":{"incr":19,"name":"dd"}
     * }
     *
     * @param insertDto
     * @param params
     * @return
     */
    @Override
    public Object execute(InsertDto insertDto, Map<String, Object> params) {
        //解密数据库配置
        Object byKey = modelInterface.getByKey(insertDto.getOptions());
        MongoDBOption mongoDBOption = BeanCopyUtil.copy(MongoDBOption.class, byKey);
        mongoCustomTemplate.insert(mongoDBOption, insertDto.getTableName(), insertDto.getBody());
        return true;
    }
}
