package cn.bctools.rule.data.postgresql.select;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.dm.utils.SQLStringUtils;
import cn.bctools.rule.data.postgresql.PgsqlSelectOption;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

/**
 * @author wl
 */
@Slf4j
@Service
@Rule(value = "postgresql查询",
        group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.数字,
        testShowEnum = TestShowEnum.JSON,
        order = 46,
        explain = "postgresql查询"
)
@RequiredArgsConstructor
public class PgsqlSelectImpl implements BaseCustomFunctionInterface<PgsqlSelectDto> {

    private final ModelInterface modelInterface;

    @Override
    public Object execute(PgsqlSelectDto dto, Map<String, Object> params) {

        if (StrUtil.isNotEmpty(dto.getExecSql())) {
            if (!StrUtil.startWithIgnoreCase(dto.getExecSql(), "select")) {
                throw new BusinessException("当前节点为postgresql查询，请检查SQL执行语句关键字");
            }
        }
        Object byKey = modelInterface.getByKey(dto.getDatabaseName());
        PgsqlSelectOption option = BeanCopyUtil.copy(PgsqlSelectOption.class, byKey);
        String replace = SQLStringUtils.replace(dto.getExecSql(), dto.getParam());

        Function<PgsqlSelectOption, PGSimpleDataSource> pgsql_templateFunction = e -> {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            String url = ClientConfig.PGSQL_URL.apply(option);
            dataSource.setUrl(url);
            dataSource.setUser(option.getSourceUserName());
            dataSource.setPassword(option.getSourcePwd());
            return dataSource;
        };

        try {
            return ClientConfig.init(option, pgsql_templateFunction).queryData(replace, dto.getResultType());
        } catch (Exception e) {
        }
        return null;
    }
}
