package cn.bctools.message.push.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author admin
 * @since 2024-09-05
 */
@Getter
@Setter
@TableName("message_push_log")
@ApiModel(value = "MessagePushLog对象", description = "")
public class MessagePushLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @ApiModelProperty("客户端")
    @TableField("client_code")
    private String clientCode;

    @ApiModelProperty("接收人")
    @TableField("defined_receivers")
    private String definedReceivers;

    @ApiModelProperty("大类")
    @TableField("large_categories")
    private String largeCategories;

    @ApiModelProperty("发送人id")
    @TableField("create_by_id")
    private String createById;

    @ApiModelProperty("租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("发送人名称")
    @TableField("create_by")
    private String createBy;

    @ApiModelProperty("拓展数据")
    @TableField("expand_data")
    private String expandData;


}
