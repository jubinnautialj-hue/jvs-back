package cn.bctools.rule.dingding.attendance;


import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dingding.utils.DingUtils;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.date.DateUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceListRequest;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Ding oapi attendance list request.
 *
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "获取打卡结果",
        group = RuleGroup.钉钉平台,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-yinxingqiasanyaosurenzheng",
        explain = "获取企业内员工的实际打卡结果,例如，企业给一个员工设定的排班是上午9点和下午6点各打一次卡，即使员工在这期间打了多次，本接口也只会返回两条记录，包括上午的打卡结果和下午的打卡结果。")
public class DingOapiAttendanceListRequestImpl implements BaseCustomFunctionInterface<DingOapiAttendanceListRequestDto> {

    /**
     * The Api.
     */
    AuthTenantConfigServiceApi api;

    /**
     * The User extension service api.
     */
    UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public Object execute(DingOapiAttendanceListRequestDto dto, Map<String, Object> params) {
        //根据用户Id获取用户钉钉扩展uid
        List<UserExtensionDto> ding = userExtensionServiceApi.query(dto.getUsers(), "Ding").getData();
        if (ObjectNull.isNotNull(ding)) {
            List<String> users = ding.stream().map(e -> e.getExtension().getOrDefault("userid", null)).filter(ObjectNull::isNotNull).map(Object::toString).collect(Collectors.toList());
            if (ObjectNull.isNull(users)) {
                throw new BusinessException("选择用户没有绑定钉钉");
            }
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/attendance/list");
            OapiAttendanceListRequest req = new OapiAttendanceListRequest();
            req.setWorkDateFrom(DateUtil.formatDateTime(dto.getWorkDateFrom()));
            req.setWorkDateTo(DateUtil.formatDateTime(dto.getWorkDateTo()));
            req.setUserIdList(users);
            req.setOffset(0L);
            req.setLimit(50L);
            SysConfigDing sysConfigDing = DingUtils.getSysConfigDing(TenantContextHolder.getTenantId());
            try {
                OapiAttendanceListResponse rsp = client.execute(req, DingUtils.getAccessToken(sysConfigDing));
                if (rsp.isSuccess()) {
                    return BeanCopyUtil.copy(rsp.getRecordresult(), LinkedList.class);
                } else {
                    throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", rsp.getMessage(), rsp.getRequestId(), rsp.getErrorCode()));
                }
            } catch (ApiException e) {
                log.error("钉钉三方调用异常", e);
                throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage()));
            }
        }
        throw new BusinessException("用户没有绑定钉钉");
    }

}
