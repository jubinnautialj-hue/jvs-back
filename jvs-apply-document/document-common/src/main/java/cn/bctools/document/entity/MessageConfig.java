package cn.bctools.document.entity;


import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.MessageTypeEnum;
import cn.bctools.document.enums.MessagePushTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("消息绑定")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "message_config", autoResultMap = true)
public class MessageConfig extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("消息类型")
    @TableField("message_type")
    private MessageTypeEnum messageType;
    @ApiModelProperty("消息服务商与模板中的对应")
    @TableField("facilitator")
    private String facilitator;
    @ApiModelProperty("消息id")
    @TableField("message_id")
    private String messageId;
    @ApiModelProperty("操作类型")
    @TableField("operation_type")
    private MessagePushTypeEnum operationType;
    @ApiModelProperty("业务id 文档id")
    @TableField("business_id")
    private String businessId;
    @ApiModelProperty("用户id，系统角色id，部门id，群组id，岗位id")
    @TableField("user_id")
    private String userId;
    @ApiModelProperty("数据权限类型")
    @TableField("data_auth_type")
    private DataAuthTypeEnum dataAuthType;
    @ApiModelProperty("上级ID")
    private String parentId;
    @ApiModelProperty("类型/知识库、目录、文本文档、表格文档、脑图文档、流程文档。")
    private DcLibraryTypeEnum type;
    @ApiModelProperty("知识库id")
    private String knowledgeId;
    @ApiModelProperty("路径id")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> pathId;
    @ApiModelProperty("用户id")
    @TableField(exist = false)
    private List<String> userIds = new ArrayList<>();


}
