package cn.bctools.data.factory.notice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知方式
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {
    /**
     * 消息类型
     */
    SYSTEM("SYSTEM", "站内信", 1, "systemSendNotifyImpl"),
    WECHAT_MP("WECHAT_MP", "公众号", 2, "wechatMpSendNotifyImpl"),
    WECHAT_ENTERPRISE("WECHAT_ENTERPRISE", "企业微信", 3, "wechatEnterpriseSendNotifyImpl"),
    DING("DING", "钉钉", 4, "dingSendNotifyImpl"),
    EMAIL("EMAIL", "邮件", 5, "emailSendNotifyImpl"),
    SMS("SMS", "短信", 6, "smsSendNotifyImpl"),
    ;

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;
    /**
     * 排序
     */
    private final int sort;
    private final String implName;

    /**
     * 获取所有已启用的消息通知方式
     *
     * @return
     */
    public static List<Map<String, String>> getEnableAll(Predicate<NoticeTypeEnum> predicate) {
        return Arrays.stream(NoticeTypeEnum.values())
                .filter(predicate)
                .sorted(Comparator.comparing(NoticeTypeEnum::getSort))
                .map(type -> {
                    Map<String, String> t = new HashMap<>(1);
                    t.put(type.getValue(), type.getDesc());
                    return t;
                })
                .collect(Collectors.toList());
    }
}
