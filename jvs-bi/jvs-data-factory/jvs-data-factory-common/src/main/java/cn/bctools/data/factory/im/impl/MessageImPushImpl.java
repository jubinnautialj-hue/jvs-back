package cn.bctools.data.factory.im.impl;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.enums.ImPushTypeEnums;
import cn.bctools.data.factory.im.MessageImPush;
import cn.bctools.im.api.ImServiceApi;
import cn.bctools.im.dto.NotifyDto;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author xiaohui
 */
@Component
@Slf4j
public class MessageImPushImpl implements MessageImPush {
    private static final String IM_GROUP_NAME = "data_factory_execution_schedule";
    @Autowired
    ImServiceApi imServiceApi;

    @Override
    public String getGroupId(String tenantId) {
        String groupName = IM_GROUP_NAME + tenantId;
        return SecureUtil.md5(groupName);

    }


    @Override
    public void notify(ImPushTypeEnums type, String value, String id) {
        String tenantId = TenantContextHolder.getTenantId();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("value", value);
        jsonObject.put("id", id);
        NotifyDto notifyDto = new NotifyDto()
                .setBusinessType(IM_GROUP_NAME)
                .setTenantId(tenantId)
                .setNotifyType(1)
                .setToGroupIds(Arrays.asList(getGroupId(tenantId)))
                .setFrom("0")
                .setContent(jsonObject)
                .setCreateTime(System.currentTimeMillis())
                .setTitle("执行进度通知");
        //im通知不应该影响整体流程
        try {
            imServiceApi.notify(notifyDto);
        } catch (Exception exception) {
            log.info("im发送消息失败", exception);
        }
    }

    @Override
    public void notify(Object value, String businessType) {
        String tenantId = TenantContextHolder.getTenantId();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("label", DateUtil.now());
        jsonObject.put("value", value);
        NotifyDto notifyDto = new NotifyDto()
                .setBusinessType(businessType)
                .setTenantId(tenantId)
                .setNotifyType(1)
                .setToGroupIds(Arrays.asList(businessType))
                .setFrom("0")
                .setContent(jsonObject)
                .setCreateTime(System.currentTimeMillis())
                .setTitle("mqtt数据集结果通知");
        //im通知不应该影响整体流程
        try {
            imServiceApi.notify(notifyDto);
            log.info("发送im消息成功:{}",JSONObject.toJSONString(notifyDto));
        } catch (Exception exception) {
            log.info("im发送消息失败", exception);
        }
    }
}
