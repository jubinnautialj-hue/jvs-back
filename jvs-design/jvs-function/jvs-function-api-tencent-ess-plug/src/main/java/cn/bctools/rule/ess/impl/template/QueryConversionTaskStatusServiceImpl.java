package cn.bctools.rule.ess.impl.template;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.ess.util.TenantUtil;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.GetTaskResultApiRequest;
import com.tencentcloudapi.ess.v20201111.models.GetTaskResultApiResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询转换任务状态-电子签",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（GetTaskResultApi）用来查询转换任务的状态。如需发起转换任务，请使用创建文件转换任务接口进行资源文件的转换操作\n" +
                "前提条件：已调用 创建文件转换任务接口进行文件转换，并得到了返回的转换任务Id。\n" +
                "\n" +
                "适用场景：已创建一个文件转换任务，想查询该文件转换任务的状态，或获取转换后的文件资源Id。\n" +
                "注：\n" +
                "\n" +
                "大文件转换所需的时间可能会比较长"
)
@AllArgsConstructor
public class QueryConversionTaskStatusServiceImpl implements BaseCustomFunctionInterface<QueryConversionTaskStatusDto> {

    @Override
    public Object execute(QueryConversionTaskStatusDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            GetTaskResultApiRequest req = new GetTaskResultApiRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setTaskId(dto.getTaskId());
            // 返回的resp是一个GetTaskResultApiResponse的实例，与请求对象对应
            GetTaskResultApiResponse resp = client.GetTaskResultApi(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
