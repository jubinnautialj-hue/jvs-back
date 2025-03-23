package cn.bctools.im.processor;

import org.jim.core.ImChannelContext;
import org.jim.core.packets.User;
import org.jim.core.packets.login.LoginReqBody;
import org.jim.server.processor.SingleProtocolCmdProcessor;

/**
 * 登录处理器
 */
public interface LoginCmdProcessor extends SingleProtocolCmdProcessor {

    /**
     * 获取用户信息（登录）
     * @param var1
     * @param var2
     * @return
     */
    User getUser(LoginReqBody var1, ImChannelContext var2);

    /**
     * 登录成功回调方法
     * @param var1 用户信息
     * @param var2 IM通道上下文
     */
    void onSuccess(User var1, ImChannelContext var2);

    /**
     * 登录失败回调方法
     * @param var1 IM通道上下文
     */
    void onFailed(ImChannelContext var1);
}
