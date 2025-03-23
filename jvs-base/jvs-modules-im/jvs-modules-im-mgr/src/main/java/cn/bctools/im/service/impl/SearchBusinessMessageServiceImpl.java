package cn.bctools.im.service.impl;

import cn.bctools.im.service.SearchMessageService;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.message.MessageReqBody;
import org.jim.core.packets.message.MessageReqTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: ZhuXiaoKang
 * @Description: 业务消息查询
 */
@Service
public class SearchBusinessMessageServiceImpl implements SearchMessageService {

    @Override
    public String getType() {
        return MessageReqTypeEnum.BUSINESS.name();
    }

    @Override
    public UserMessageData search(MessageReqBody messageReqBody, MessageHelper messageHelper) {
        // 租户id
        String tenantId = messageReqBody.getTenantId();
        // 业务消息id
        String businessId = messageReqBody.getBusinessId();
        return messageHelper.getBusinessMessage(tenantId, businessId);
    }
}
