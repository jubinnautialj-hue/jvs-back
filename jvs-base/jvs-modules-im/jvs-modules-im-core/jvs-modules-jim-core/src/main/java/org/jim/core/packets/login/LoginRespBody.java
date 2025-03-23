package org.jim.core.packets.login;

import org.jim.core.ImStatus;
import org.jim.core.Status;
import org.jim.core.packets.Command;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.User;

/**
 * im登录响应消息体
 */
public class LoginRespBody extends RespBody {

    private String token;
    private User user;

    public LoginRespBody(){
        this.setCommand(Command.COMMAND_LOGIN_RESP);
    }

    public LoginRespBody(Status status){
        this(status,null);
    }

    public LoginRespBody(Status status , User user){
        this(status, user, null);
    }

    public LoginRespBody(Status status , User user, String token){
        super(Command.COMMAND_LOGIN_RESP, status);
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static LoginRespBody success(){
        return new LoginRespBody(ImStatus.C10000);
    }

    public static LoginRespBody failed(){
        return new LoginRespBody(ImStatus.C10008);
    }

    public static LoginRespBody failed(String msg){
        LoginRespBody loginRespBody = new LoginRespBody(ImStatus.C10008);
        loginRespBody.setMsg(msg);
        return loginRespBody;
    }
}
