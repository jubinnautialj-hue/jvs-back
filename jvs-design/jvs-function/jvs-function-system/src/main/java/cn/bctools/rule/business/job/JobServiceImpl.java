package cn.bctools.rule.business.job;


import cn.bctools.auth.api.api.AuthJobServiceApi;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.auth.api.api.AuthJobServiceApi;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author guojing
 * @describe 执行一个查询方法
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "选择岗位",
        group = RuleGroup.常用插件,
        returnType = ClassType.对象,
        order = 41,
//        iconUrl = "rule-drdsfenbushiguanxixingshujukufuwuDRD",
        explain = "根据选择的岗位或传入的岗位id，返回岗位的名称、id等信息"
)
@AllArgsConstructor
public class JobServiceImpl implements BaseCustomFunctionInterface<JobDto> {
    AuthJobServiceApi jobServiceApi;

    @Override
    public Object execute(JobDto listDto, Map<String, Object> params) {
        return jobServiceApi.getById(listDto.getJob()).getData();
    }

}
