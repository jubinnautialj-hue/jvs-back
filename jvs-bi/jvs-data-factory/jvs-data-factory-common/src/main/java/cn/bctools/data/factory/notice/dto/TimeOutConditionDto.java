package cn.bctools.data.factory.notice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TimeOutConditionDto {

    private TimeType type;

    private Long value;

    public enum TimeType{
        millisecond,
        second,
        minute,
        ;
    }
}
