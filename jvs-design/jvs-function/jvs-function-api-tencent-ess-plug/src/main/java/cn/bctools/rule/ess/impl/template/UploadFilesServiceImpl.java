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
import com.tencentcloudapi.ess.v20201111.models.Caller;
import com.tencentcloudapi.ess.v20201111.models.UploadFile;
import com.tencentcloudapi.ess.v20201111.models.UploadFilesRequest;
import com.tencentcloudapi.ess.v20201111.models.UploadFilesResponse;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "上传文件-电子签",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景：用于合同，印章的文件上传。文件上传以后， 如果是PDF格式文件可配合用PDF文件创建签署流程接口进行合同流程的发起 如果是其他类型可以配合创建文件转换任务接口转换成PDF文件\n" +
                "\n" +
                "注:\n" +
                "\n" +
                "图片类型(png/jpg/jpeg)限制大小为5M以下, PDF/word/excel等其他格式限制大小为60M以下\n" +
                "联调开发环境调用时需要设置Domain接口请求域名为 file.test.ess.tencent.cn，正式环境需要设置为file.ess.tencent.cn"
)
@AllArgsConstructor
public class UploadFilesServiceImpl implements BaseCustomFunctionInterface<UploadFilesDto> {
    @Override
    public Object execute(UploadFilesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            UploadFilesRequest req = new UploadFilesRequest();
            try {
                req.setFileInfos(BeanCopyUtil.copys(dto.getFileInfos(), UploadFile.class).toArray(new UploadFile[dto.getFileInfos().size()]));
            } catch (Exception ignored) {

            }
            req.setFileType(dto.getFileType());
            req.setCaller(BeanCopyUtil.copy(dto.getCaller(), Caller.class));
            req.setBusinessType(dto.getBusinessType());
            req.setCoverRect(dto.getCoverRect());
            // 返回的resp是一个UploadFilesResponse的实例，与请求对象对应
            UploadFilesResponse resp = client.UploadFiles(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
