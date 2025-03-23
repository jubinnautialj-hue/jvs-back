package cn.bctools.data.factory.notice.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConsecutiveFailuresDto {

    private int frequency;
}
