package cn.bctools.rule.idcard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class AliIdCardReturnDto {
    private Object code;
    private String msg;
    private String desc;
    private Object data;
}
