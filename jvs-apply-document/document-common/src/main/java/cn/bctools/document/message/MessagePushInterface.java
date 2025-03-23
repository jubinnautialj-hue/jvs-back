package cn.bctools.document.message;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.enums.MessagePushTypeEnum;

/**
 * 消息发送统一规范接口
 *
 * @author admin
 */
public interface MessagePushInterface<T> {
    /**
     * 消息推送 动态消息推送
     *
     * @param message 不同消息的数据
     * @return 消息是否发送成功
     */
    Boolean pushMessage(T message);

    /**
     * 普通消息推送
     *
     * @param text    消息推送内容
     * @param userDto 用户信息
     * @return 消息是否推送成功
     */
    Boolean textPushMessage(String text, UserDto userDto);

    /**
     * 数据组装 就是下水道操作 比如模板替换 数据转换 数据验证等等
     *
     * @param messageConfig 消息绑定数据
     * @param dcLibrary     文档数据
     * @param operatorType  当前操作类型
     * @param user          操作的用户信息
     * @return 返回不同消息的消息体
     */
    T getData(MessageConfig messageConfig, DcLibrary dcLibrary, MessagePushTypeEnum operatorType, UserDto user);
}
