package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @description im服务信息
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@TableName(value = "im_server_info")
public class ServerInfo{

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "服务id")
    private String serverId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "心跳时间")
    private LocalDateTime heartbeatTime;
}
