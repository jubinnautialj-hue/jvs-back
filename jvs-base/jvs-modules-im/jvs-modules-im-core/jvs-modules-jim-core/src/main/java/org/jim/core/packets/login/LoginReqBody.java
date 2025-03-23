package org.jim.core.packets.login;

import org.jim.core.packets.Command;
import org.jim.core.packets.Message;

/**
 * im登录请求消息体
 */
public class LoginReqBody extends Message {

    private static final long serialVersionUID = -10113316720288444L;
    private String logType;
    private String value;
    private String tempLinkId;

    public LoginReqBody(){}

    public LoginReqBody(String logType, String value){
        this.logType = logType;
        this.value = value;
        this.cmd = Command.COMMAND_LOGIN_REQ.getNumber();
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTempLinkId() {
        return tempLinkId;
    }

    public void setTempLinkId(String tempLinkId) {
        this.tempLinkId = tempLinkId;
    }
}
