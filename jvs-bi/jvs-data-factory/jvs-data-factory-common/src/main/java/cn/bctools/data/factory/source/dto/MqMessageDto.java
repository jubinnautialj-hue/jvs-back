package cn.bctools.data.factory.source.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.oss.dto.BaseFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 队列消息实体类
 *
 * @author xiaohui
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MqMessageDto {
    /**
     * 数据源id
     */
    private String id;
    /**
     * 是否为追加
     */
    private Boolean addIs;
    /**
     * 文件信息
     */
    private BaseFile baseFile;

    /**
     * 租户id
     */
    private String tenantId;
    /**
     * 此操作的用户信息
     */
    private UserDto userDto;
}
