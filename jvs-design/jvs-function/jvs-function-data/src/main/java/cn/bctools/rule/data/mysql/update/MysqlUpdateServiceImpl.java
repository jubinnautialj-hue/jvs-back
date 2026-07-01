package cn.bctools.rule.data.mysql.update;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.dm.utils.SQLStringUtils;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author jvs
 */
@Slf4j
@Service
@Rule(value = "Mysql更新",
        group = RuleGroup.数据插件,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 8,
//        iconUrl = "rule-redisyunshujukuRedisban",
        explain = "Mysql更新",
        demoDisabled = true
)
@AllArgsConstructor
public class MysqlUpdateServiceImpl implements BaseCustomFunctionInterface<MysqlUpdateDto> {
    ModelInterface modelInterface;


    @Override
    public Object execute(MysqlUpdateDto dto, Map<String, Object> params) {
        if (StrUtil.isNotEmpty(dto.getExecSql())) {
            if (!StrUtil.startWithIgnoreCase(dto.getExecSql(), "update")) {
                throw new BusinessException("当前节点为Mysql更新节点，请检查SQL执行语句关键字");
            }
        }
        Object byKey = modelInterface.getByKey(dto.getDatabaseName());


        DatasourceSelectedOption option = BeanCopyUtil.copy(DatasourceSelectedOption.class,byKey);
        String replace = SQLStringUtils.replace(dto.getExecSql(), dto.getParam());

        Function<DatasourceSelectedOption, DriverManagerDataSource> mysql_templateFunction = e -> {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            String url = ClientConfig.MYSQL_URL.apply(option);
            dataSource.setUrl(url);
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUsername(option.getUserName());
            dataSource.setPassword(option.getPassWord());
            return dataSource;
        };

        try {
            return ClientConfig.init(option, mysql_templateFunction).updateData(replace);
        } catch (SQLException e) {
        }
        return null;
    }
}
