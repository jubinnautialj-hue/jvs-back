package cn.bctools.rule.dingding.task;


import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dingding.utils.DingUtils;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskRequest;
import com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Ding create todo task with options.
 *
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "创建待办",
        group = RuleGroup.钉钉平台,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 1,
//        iconUrl = "rule-yinxingqiasanyaosurenzheng",
        explain = "实现发起一个钉钉待办任务。该待办事项会出现在钉钉客户端“待办事项”页面。"
)
public class DingCreateTodoTaskWithOptionsImpl implements BaseCustomFunctionInterface<DingCreateTodoTaskWithOptionsDto> {

    /**
     * The Api.
     */
    AuthTenantConfigServiceApi api;

    /**
     * The User extension service api.
     */
    UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public Object execute(DingCreateTodoTaskWithOptionsDto dto, Map<String, Object> params) {
        //根据用户Id获取用户钉钉扩展uid
        List<UserExtensionDto> ding = userExtensionServiceApi.query(Collections.singletonList(dto.getUser()), "Ding").getData();
        if (ObjectNull.isNotNull(ding)) {
            String openId = ding.get(0).getOpenId();
            CreateTodoTaskRequest todo = new CreateTodoTaskRequest();
            todo.setSubject(dto.getSubject());
            todo.setDetailUrl(new CreateTodoTaskRequest.CreateTodoTaskRequestDetailUrl().setPcUrl(dto.getPcUrl()));
            todo.setDescription(dto.getDescription());
            try {
                CreateTodoTaskResponse response = DingUtils.getClient().createTodoTaskWithOptions(openId, todo, DingUtils.getCreateTodoTaskHeaders(), new RuntimeOptions());
                if (response.getStatusCode().equals(200)) {
                    return BeanCopyUtil.copy(response.getBody(), HashMap.class);
                } else {
                    throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", response.getBody()));
                }
            } catch (Exception e) {
                log.error("钉钉三方调用异常", e);
                throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage()));
            }
        }
        throw new BusinessException("用户没有绑定钉钉");
    }
}
