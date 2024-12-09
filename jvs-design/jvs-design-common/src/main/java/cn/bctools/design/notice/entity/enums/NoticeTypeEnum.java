package cn.bctools.design.notice.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知方式
 */
@Getter
public enum NoticeTypeEnum {
    /**
     * 站内信
     */
    SYSTEM("SYSTEM", "站内信", 1),
    /**
     * 公众号
     */
    WECHAT_MP("WECHAT_MP", "公众号", 2),
    /**
     * 企业微信
     */
    WECHAT_ENTERPRISE("WECHAT_ENTERPRISE", "企业微信", 3),
    /**
     * 钉钉
     */
    DING("DING", "钉钉", 4),
    /**
     * 邮件
     */
    EMAIL("EMAIL", "邮件", 5),
    /**
     * 短信
     */
    SMS("SMS", "短信", 6),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;
    /**
     * 排序
     */
    private int sort;

    NoticeTypeEnum(String value, String desc, int sort) {
        this.value = value;
        this.desc = desc;
        this.sort = sort;
    }

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
