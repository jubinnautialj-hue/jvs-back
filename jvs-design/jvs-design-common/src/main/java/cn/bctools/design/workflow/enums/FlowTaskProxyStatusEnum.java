package cn.bctools.design.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 代理状态
 */
@Getter
@AllArgsConstructor
public enum FlowTaskProxyStatusEnum implements IEnum {

    /**
     * 待生效
     */
    PENDING(1, "待生效"),
    /**
     * 代理中
     */
    EFFECTIVE(2, "代理中"),
    /**
     * 已过期
     */
    EXPIRED(3, "已过期"),
    /**
     * 已撤销
     */
    REVOKED(4, "已撤销"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;
    private String desc;
}
