package cn.bctools.rule.ess.autosign;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import cn.bctools.rule.exception.RuleException;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreatePersonAuthCertificateImageRequest;
import com.tencentcloudapi.ess.v20201111.models.CreatePersonAuthCertificateImageResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Create person auth certificate image.
 *
 * @author jvs
 */
@Slf4j

public class CreatePersonAuthCertificateImage {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        CreatePersonAuthCertificateImageRequest req = prepareCreatePersonAuthCertificateImageRequest();
        // 身份证号码
        req.setIdCardNumber("110*************234");
        req.setIdCardType("ID_CARD");
        req.setUserName("用户的名称");

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        try {
            CreatePersonAuthCertificateImageResponse res = client.CreatePersonAuthCertificateImage(req);
            assert res != null;
            log.info(CreatePersonAuthCertificateImageResponse.toJsonString(res));
        } catch (TencentCloudSDKException e) {
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }
    }

    /**
     * 构造请求基本参数
     *
     * @return the create person auth certificate image request
     */
    public static CreatePersonAuthCertificateImageRequest prepareCreatePersonAuthCertificateImageRequest() {
        CreatePersonAuthCertificateImageRequest req = new CreatePersonAuthCertificateImageRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Config.OPERATOR_USER_ID);
        req.setOperator(userInfo);
        return req;
    }
}
