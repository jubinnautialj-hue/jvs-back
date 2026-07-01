package cn.bctools.rule.ess.autosign;

import com.tencentcloudapi.ess.v20201111.models.UserThreeFactor;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Create user auto sign enable url api.
 *
 * @author jvs
 */
@Slf4j

public class CreateUserAutoSignEnableUrlApi {

//    public static void main(String[] args) {
//
//        CreateUserAutoSignEnableUrlRequest req = prepareCreateUserAutoSignEnableUrlRequest("");
//
//        AutoSignConfig autoSignConfig = PrepareUserAutoSignConfig(
//                PrepareUserThreeFactor("姓名", "身份证号"),
//                "https://example.com", true, true, true);
//
//        req.setAutoSignConfig(autoSignConfig);
//
//        // 构造默认的api客户端调用实例
//        EssClient client = Client.getEssClient();
//
//        try {
//            CreateUserAutoSignEnableUrlResponse res = client.CreateUserAutoSignEnableUrl(req);
//            assert res != null;
//            log.info(CreateUserAutoSignEnableUrlResponse.toJsonString(res));
//        } catch (TencentCloudSDKException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Prepare user three factor user three factor.
     *
     * @param name      the name
     * @param idCardNum the id card num
     * @return the user three factor
     */
    public static UserThreeFactor prepareUserThreeFactor(String name, String idCardNum) {
        UserThreeFactor threeFactor = new UserThreeFactor();
        // 姓名
        threeFactor.setName(name);
        // 身份证号
        threeFactor.setIdCardNumber(idCardNum);
        threeFactor.setIdCardType("ID_CARD");
        return threeFactor;
    }

//    public static AutoSignConfig PrepareUserAutoSignConfig(
//            UserThreeFactor threeFactor,
//            String callBackUrl,
//            boolean selfDefineSeal,
//            boolean needSealImgCallback,
//            boolean needCertInfoCallback
//            ) {
//        AutoSignConfig autoSignConfig = new AutoSignConfig();
//        autoSignConfig.setUserInfo(threeFactor);
//
//        autoSignConfig.setCallbackUrl(callBackUrl);
//
//        autoSignConfig.setUserDefineSeal(selfDefineSeal);
//
//        autoSignConfig.setSealImgCallback(needSealImgCallback);
//
//        autoSignConfig.setCertInfoCallback(needCertInfoCallback);
//        return autoSignConfig;
//    }

//    public static CreateUserAutoSignEnableUrlRequest prepareCreateUserAutoSignEnableUrlRequest(String urlType) {
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserId(Config.OperatorUserId);
//
//        CreateUserAutoSignEnableUrlRequest req = new CreateUserAutoSignEnableUrlRequest();
//
//        req.setOperator(userInfo);
//
//        req.setUrlType(urlType);
//        req.setSceneKey("E_PRESCRIPTION_AUTO_SIGN");
//        return req;
//    }
}
