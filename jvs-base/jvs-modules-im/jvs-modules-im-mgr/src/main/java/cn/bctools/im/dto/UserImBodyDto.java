package cn.bctools.im.dto;

import cn.bctools.common.entity.dto.UserDto;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jim.core.packets.Message;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class UserImBodyDto extends Message {

    UserDto userDto;


}
