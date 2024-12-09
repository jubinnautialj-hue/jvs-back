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
import com.tencentcloudapi.ess.v20201111.models.CreateConvertTaskApiRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateConvertTaskApiResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "创建文件转换任务-电子签",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（CreateConvertTaskApi）用来将word、excel、html、图片、txt类型文件转换为PDF文件。\n" +
                "前提条件：源文件已经通过 文件上传接口完成上传，并得到了源文件的资源Id。\n" +
                "适用场景1：已经上传了一个word文件，希望将该word文件转换成pdf文件后发起合同 适用场景2：已经上传了一个jpg图片文件，希望将该图片文件转换成pdf文件后发起合同\n" +
                "转换文件是一个耗时操作，若想查看转换任务是否完成，可以通过查询转换任务状态接口获取任务状态。\n" +
                "注:\n" +
                "\n" +
                "支持的文件类型有doc、docx、xls、xlsx、html、jpg、jpeg、png、bmp、txt\n" +
                "可通过发起合同时设置预览来检查转换文件是否达到预期效果"
)
@AllArgsConstructor
public class CreateFileConversionTaskServiceImpl implements BaseCustomFunctionInterface<CreateFileConversionTaskDto> {

    @Override
    public Object execute(CreateFileConversionTaskDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateConvertTaskApiRequest req = new CreateConvertTaskApiRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setResourceId(dto.getResourceId());
            req.setResourceType(dto.getResourceType());
            req.setResourceName(dto.getResourceName());
            // 返回的resp是一个CreateConvertTaskApiResponse的实例，与请求对象对应
            CreateConvertTaskApiResponse resp = client.CreateConvertTaskApi(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
