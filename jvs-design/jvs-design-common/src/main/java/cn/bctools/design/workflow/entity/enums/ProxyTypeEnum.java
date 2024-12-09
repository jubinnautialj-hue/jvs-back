package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 代理类型
 */
@Getter
@AllArgsConstructor
public enum ProxyTypeEnum {
    /**普通代理*/
    DEFAULT("DEFAULT", "普通代理"),
    /**离职代理*/
    DEPART("DEPART", "离职代理"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;
}
