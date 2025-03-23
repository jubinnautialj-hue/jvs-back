package cn.bctools.data.factory.notice.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.notice.bo.ReceiverBo;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知-获取接收人
 */
public interface NoticeReceiverHandler {

    /**
     * 获取接收用户id
     *
     * @param receiver 接收人配置
     * @return 接收人id集合
     */
    List<String> getUserIds(List<ReceiverBo> receiver);

    /**
     * 获取用户信息
     *
     * @return {@link UserDto}
     */
    List<UserDto> getUser(List<ReceiverBo> receiver);
}
