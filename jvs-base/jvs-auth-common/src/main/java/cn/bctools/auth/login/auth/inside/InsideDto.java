package cn.bctools.auth.login.auth.inside;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 源token和目标服务
 * @author czy
 */
@Data
@Accessors(chain = true)
public class InsideDto {

    String token;
    String source;

}
