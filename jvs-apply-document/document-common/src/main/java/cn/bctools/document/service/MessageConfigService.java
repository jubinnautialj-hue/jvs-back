package cn.bctools.document.service;

import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.enums.MessagePushTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author xiaohui
 */
public interface MessageConfigService extends IService<MessageConfig> {

    /**
     * 获取消息数据包含用户信息 简称为下水道操作
     *
     * @param knowledgeId  文库id
     * @param operatorType 操作类型
     * @return 消息数据
     */
    List<MessageConfig> getUser(String knowledgeId, MessagePushTypeEnum operatorType);


    /**
     * 权限变更时同步用户信息  反正用户变更 消息数据没有删除
     *
     * @param dcLibrary     文档对象
     * @param removeUserIds 被删除了的用户id
     */
    void syncUserInfo(List<String> removeUserIds, DcLibrary dcLibrary);

    /**
     * 权限变更时同步用户信息  反正用户变更 消息数据没有删除
     *
     * @param list 消息数据
     * @param id   文档id
     */
    void saveMessage(List<MessageConfig> list, String id);
}
