package cn.bctools.rule.data.kingbase.delete;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.config.ClientConfig;
import cn.bctools.rule.data.kingbase.KingBaseDatasourceSelectedOption;
import cn.bctools.rule.data.kingbase.utils.SQLStringUtils;
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
@Rule(value = "KingBase删除",
        group = RuleGroup.数据插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 227,
        explain = "KingBase删除"
)
@AllArgsConstructor
public class KingBaseDeleteServiceImpl implements BaseCustomFunctionInterface<KingBaseDeleteDto> {
    ModelInterface modelInterface;


    @Override
    public Object execute(KingBaseDeleteDto dto, Map<String, Object> params) {
        if (StrUtil.isNotEmpty(dto.getExecSql())) {
            if (!StrUtil.startWithIgnoreCase(dto.getExecSql(), "delete")) {
                throw new BusinessException("当前节点为KingBase删除节点，请检查SQL执行语句关键字");
            }
        }
        Object byKey = modelInterface.getByKey(dto.getDatabaseName());
        KingBaseDatasourceSelectedOption option = BeanCopyUtil.copy(KingBaseDatasourceSelectedOption.class,byKey);
        String replace = SQLStringUtils.replace(dto.getExecSql(), dto.getParam());

        Function<KingBaseDatasourceSelectedOption, DriverManagerDataSource> KingBase_templateFunction = e -> {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            String url = ClientConfig.KINGBASE_FUNCTION_URL.apply(option);
            dataSource.setUrl(url);
            dataSource.setDriverClassName("com.kingbase8.Driver");
            dataSource.setUsername(option.getUserName());
            dataSource.setSchema(option.getSchema());
            dataSource.setPassword(option.getPassWord());
            return dataSource;
        };

        try {
            return ClientConfig.init(option, KingBase_templateFunction).updateData(replace);
        } catch (SQLException e) {
        }
        return null;
    }
}
