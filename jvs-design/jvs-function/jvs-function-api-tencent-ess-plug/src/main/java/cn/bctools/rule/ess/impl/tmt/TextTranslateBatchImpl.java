package cn.bctools.rule.ess.impl.tmt;

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
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchResponse;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "文本翻译的批量接口",
        group = RuleGroup.机器翻译,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
        explain = "文本翻译的批量接口,默认接口请求频率限制：5次/秒。"
)
@AllArgsConstructor
public class TextTranslateBatchImpl implements BaseCustomFunctionInterface<TextTranslateBatchDto> {

    @Override
    public Object execute(TextTranslateBatchDto dto, Map<String, Object> params) {
        TmtClient client = TenantUtil.getTmtClient(dto.getOptions());
        try {
            TextTranslateBatchRequest req = new TextTranslateBatchRequest();
            req.setSource("auto");
            req.setProjectId(Long.valueOf(0));
            req.setSourceTextList(dto.getSourceTextList().toArray(new String[0]));
            req.setTarget(dto.getTarget());
            TextTranslateBatchResponse resp = client.TextTranslateBatch(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
