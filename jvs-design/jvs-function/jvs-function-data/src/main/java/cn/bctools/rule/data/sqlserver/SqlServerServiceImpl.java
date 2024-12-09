package cn.bctools.rule.data.sqlserver;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * @author st
 */
@Slf4j
@Service
@Order(20)
@Rule(
        value = "SqlServer",
        group = RuleGroup.数据插件,
        test = true,
        enable = false,

        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
        order = 21,
//        iconUrl = "rule-mongodbyunshujukuMongoDB",
        explain = "是非关系数据库当中功能最丰富，最像关系数据库的。它支持的数据结构非常松散，是类似json的bson格式，因此可以存储比较复杂的数据类型。"

)
public class SqlServerServiceImpl implements BaseCustomFunctionInterface<SqlServerFunctionDto> {

    @Override
    public Object execute(SqlServerFunctionDto esFunctionDto, Map<String, Object> params) {
        return null;
    }
}
