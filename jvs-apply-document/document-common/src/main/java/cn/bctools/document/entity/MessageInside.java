package cn.bctools.document.entity;


import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("消息模板-站内信")
@EqualsAndHashCode(callSuper = false)
@TableName("message_inside")
public class MessageInside extends BasalPo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("服务商")
    @TableField("facilitator")
    private String facilitator;
    @ApiModelProperty("模板名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("变量绑定")
    @TableField(exist = false)
    private List<MessageVariableBinDing> binDingList;
}
