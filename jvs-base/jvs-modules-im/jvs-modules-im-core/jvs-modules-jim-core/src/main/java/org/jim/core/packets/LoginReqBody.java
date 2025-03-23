package org.jim.core.packets;

import lombok.Data;

/**
 * 版本: [1.0]
 * 功能说明: 登陆命令请求包体
 * 作者: WChao 创建时间: 2017年9月12日 下午3:13:22
 */
@Data
public class LoginReqBody extends Message {

    private static final long serialVersionUID = -10113316720288444L;

    /**
     * 登录类型
     */
    private String logType;
    /**
     * 登录的值
     */
    private String value;

    /**
     * 临时连接id
     */
    private String tempLinkId;
}
