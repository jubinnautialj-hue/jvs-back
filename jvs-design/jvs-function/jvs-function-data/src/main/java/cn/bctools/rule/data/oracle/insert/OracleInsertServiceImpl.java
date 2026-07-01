package cn.bctools.rule.data.oracle.insert;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.dm.utils.SQLStringUtils;
import cn.bctools.rule.data.oracle.OracleBaseDto;
import cn.bctools.rule.data.oracle.OracleSelectedOption;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

@Slf4j
@Service
@Rule(value = "Oracle插入",
        group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.数字,
        testShowEnum = TestShowEnum.JSON,
        order = 41,
        explain = "Oracle插入"
)
@RequiredArgsConstructor
public class OracleInsertServiceImpl implements BaseCustomFunctionInterface<OracleBaseDto> {

    @Autowired
    ModelInterface modelInterface;


    @Override
    public Object execute(OracleBaseDto dto, Map<String, Object> params) {
        if (StrUtil.isNotEmpty(dto.getExecSql())) {
            if (!StrUtil.startWithIgnoreCase(dto.getExecSql(), "insert")) {
                throw new BusinessException("当前节点为Oracle插入节点，请检查SQL执行语句关键字");
            }
        }
        Object byKey = modelInterface.getByKey(dto.getDatabaseName());
        OracleSelectedOption option = BeanCopyUtil.copy(OracleSelectedOption.class, byKey);

        String replace = SQLStringUtils.replace(dto.getExecSql(), dto.getParam());
        replace = replace.trim();
        if (replace.endsWith(";")) {
            replace = replace.substring(0, replace.length() - 1);
        }
        Function<OracleSelectedOption, DriverManagerDataSource> oracle_templateFunction = e -> {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            String url = ClientConfig.ORACLE_URL.apply(option);
            dataSource.setUrl(url);
            dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            dataSource.setUsername(option.getSourceUserName());
            dataSource.setPassword(option.getSourcePwd());
            //链接的属性
            Properties properties = new Properties();
            //设置超时时间
            properties.setProperty("connectTimeout", String.valueOf(10000));
            dataSource.setConnectionProperties(properties);
            return dataSource;
        };
        return ClientConfig.init(option, oracle_templateFunction).insertData(replace);
    }
}
