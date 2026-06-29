package org.jim.core.packets.group;

import lombok.Data;
import org.jim.core.packets.Message;

/**
 * @Author: ZhuXiaoKang
 * @Description:  组用户信息修改请求入参
 */
@Data
public class GroupUserUpdateReqBody extends Message {

    /**
     * 组id
     */
    private String groupId;

    /**
     * 用户id
     */
    private String userId;
}
