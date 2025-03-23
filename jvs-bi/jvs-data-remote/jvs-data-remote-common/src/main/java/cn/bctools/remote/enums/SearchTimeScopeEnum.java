package cn.bctools.remote.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * es搜索时间范围
 */
@Getter
public enum SearchTimeScopeEnum {

    /**
     * expression ： es支持的日期表达式
     * 因为es有时差，所以要先+8小时再计算
     */

    /**
     * 最近一天
     */
    ONE_DAY("ONE_DAY","now+8h-1d"),
    /**
     * 最近七天
     */
    SEVEN_DAY("SEVEN_DAY","now+8h-7d"),
    /**
     * 最近一月
     */
    ONE_MONTH("ONE_MONTH","now+8h-1M"),
    ;

    @EnumValue
    @JsonValue
    public final String value;
    public final String expression;

    SearchTimeScopeEnum(String value, String expression) {
        this.value = value;
        this.expression = expression;
    }

    public static SearchTimeScopeEnum getByValue(String value) {
        for(SearchTimeScopeEnum currentEnum : SearchTimeScopeEnum.values()){
            if(currentEnum.value.equals(value)){
                return currentEnum;
            }
        }
        return null;
    }
}
