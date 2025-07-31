package cn.bctools.rule.data.sqlserver.select;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.dm.utils.SQLStringUtils;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
import cn.bctools.rule.data.sqlserver.SqlServerBase;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author guojing
 */
@Slf4j
@Service
@Rule(value = "SqlServer查询",
        group = RuleGroup.数据插件,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 10,
//        iconUrl = "rule-redisyunshujukuRedisban",
        explain = "SqlServer查询",
        demoDisabled = true
)
@AllArgsConstructor
public class SqlServerServiceImpl extends SqlServerBase implements BaseCustomFunctionInterface<SqlServerFunctionDto> {

    ModelInterface modelInterface;

    @Override
    public Object execute(SqlServerFunctionDto dto, Map<String, Object> params) {
        if (StrUtil.isNotEmpty(dto.getExecSql())) {
            if (!StrUtil.startWithIgnoreCase(dto.getExecSql(), "select")) {
                throw new BusinessException("当前节点为SqlServer查询节点，请检查SQL执行语句关键字");
            }
        }
        Object byKey = modelInterface.getByKey(dto.getDatabaseName());
        String replace = SQLStringUtils.replace(dto.getExecSql(), dto.getParam());
        DatasourceSelectedOption option = BeanCopyUtil.copy(DatasourceSelectedOption.class, byKey);
        Function<DatasourceSelectedOption, DriverManagerDataSource> sqlserver_templateFunction = super.options(option);

        try {
            return ClientConfig.init(option, sqlserver_templateFunction).queryData(replace, dto.getResultType());
        } catch (SQLException e) {
        }
        return null;
    }
}
