package cn.bctools.document.message;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.service.MessageConfigService;
import cn.bctools.message.push.dto.messagepush.BaseMessage;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author xiaohui
 */
@Component
@Slf4j
public class MessageFactory {
    @Autowired
    private Map<String, MessagePushInterface> shapeMap;
    @Autowired
    private MessageConfigService messageConfigService;

    /**
     * @param operatorType 操作类型
     * @param dcLibrary    文档对象
     * @param user         操作的用户信息
     */
    public void messagePush(DcLibraryLogOperationTypeEnum operatorType, DcLibrary dcLibrary, UserDto user) {
        String dcLibraryId = dcLibrary.getId();
        MessagePushTypeEnum messagePushTypeEnum = MessagePushTypeEnum.document_del;
        //定义动作
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            if (operatorType.equals(DcLibraryLogOperationTypeEnum.DELETE)) {
                messagePushTypeEnum = MessagePushTypeEnum.library_del;
            } else {
                messagePushTypeEnum = MessagePushTypeEnum.library_update;
            }
        } else {
            switch (operatorType) {
                case ADD:
                    messagePushTypeEnum = MessagePushTypeEnum.document_add;
                    break;
                case UPDATE:
                    messagePushTypeEnum = MessagePushTypeEnum.document_update;
                    break;
                default:
            }
        }
        //获取知识库
        if (!dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            dcLibraryId = dcLibrary.getKnowledgeId();
        }
        List<MessageConfig> messageConfigs = messageConfigService.getUser(dcLibraryId, messagePushTypeEnum);
        if (messageConfigs.isEmpty()) {
            return;
        }
        MessagePushTypeEnum finalMessagePushTypeEnum = messagePushTypeEnum;
        log.info("消息对象为,{}", JSONUtil.toJsonStr(messageConfigs));
        messageConfigs.forEach(e -> {
            if (e.getUserIds().isEmpty()) {
                log.info("用户id为空");
                return;
            }
            //获取对应的实现类
            MessagePushInterface messagePushInterface = shapeMap.get(e.getFacilitator());
            //进行数据的处理 下水道操作
            BaseMessage data = (BaseMessage) messagePushInterface.getData(e, dcLibrary, finalMessagePushTypeEnum, user);
            data.setClientCode("knowledge");
            //消息推送
            data.setTenantId(dcLibrary.getTenantId());
            Boolean aBoolean = messagePushInterface.pushMessage(data);
            log.info("消息推送结果,{},消息数据,{}", aBoolean, JSONUtil.toJsonStr(data));
        });
    }
}
