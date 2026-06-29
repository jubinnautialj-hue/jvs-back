package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Map;

import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @description IM服务连接相关的数据(服务关闭后，需要处理)
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@TableName(value = "im_channel_data", autoResultMap = true)
public class ChannelData {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "服务id")
    private String serverId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "存储方式")
    private String storeType;

    @ApiModelProperty(value = "存储唯一编号")
    private String storeCode;

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "0-未处理，1-已处理")
    private Boolean processStatus;

    @ApiModelProperty(value = "扩展字段")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private Map<String, Object> expand;


}
