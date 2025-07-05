package cn.bctools.auth.login.auth.wx.enterprise;

import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.AuthRequestCustomFactory;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthWeChatEnterpriseWebRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhuxiaokang
 * 企业微信登录
 */
@Slf4j
@AllArgsConstructor
@Component("WECHAT_ENTERPRISE_WEB")
public class WxEnterpriseWebHandler extends BaseWxEnterprise implements LoginHandler<AuthCallback> {
    AuthRequestCustomFactory factory;
    WxEnterpriseDept deptHandler;
    WxEnterpriseUser userHandler;

    @Override
    public User handle(String code, String appId, AuthCallback callback) {
        AuthWeChatEnterpriseWebRequest authRequest = (AuthWeChatEnterpriseWebRequest) factory.get(LoginTypeEnum.WECHAT_ENTERPRISE_WEB.getValue());
        AuthResponse response = authRequest.login(callback);
        if (!response.ok()) {
            throw new BusinessException("enterprise_wechat_登录失败");
        }
        log.info("[login] 获取企业微信登录信息: {}", JSONUtil.toJsonStr(response));
        AuthUser authUser = (AuthUser) response.getData();
        return getUser(LOGIN_TYPE, authUser);
    }

    @Override
    public void bind(User user, String code, String appId) {

    }

    @Override
    public SyncUserDto syncUserDeptAll() {
        String accessToken = getAccessToken();
        List<Dept> deptAll = deptHandler.getDeptAll(accessToken);
        if (ObjectNull.isNotNull(deptAll)) {
            SyncUserDto deptUserAll = userHandler.getDeptUserAll(accessToken, deptAll);
            deptUserAll.setList(deptAll);
            return deptUserAll;
        } else {
            return new SyncUserDto();
        }
    }
}
