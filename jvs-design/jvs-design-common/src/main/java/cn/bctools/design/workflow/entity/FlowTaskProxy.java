package cn.bctools.design.workflow.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.workflow.entity.enums.ProxyTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zhuxiaokang
 * 工作流代理
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流代理")
@TableName("jvs_flow_task_proxy")
public class FlowTaskProxy extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "被代理用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "被代理用户姓名")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty(value = "代理用户id")
    @TableField("proxy_user_id")
    private String proxyUserId;

    @ApiModelProperty(value = "代理用户姓名")
    @TableField("proxy_user_name")
    private String proxyUserName;

    @ApiModelProperty(value = "代理开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("begin_time")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "代理结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;


    @ApiModelProperty(value = "是否已撤销 0未撤销  1已撤销")
    @TableField("revoke_flag")
    private Boolean revokeFlag;

    @ApiModelProperty(value = "说明")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "代理类型")
    @TableField("proxy_type")
    private ProxyTypeEnum proxyType;

    @ApiModelProperty(value = "是否删除 0未删除  1已删除")
    @TableField("del_flag")
    @TableLogic
    private Boolean delFlag;

    @TableField(exist = false)
    @ApiModelProperty(value = "代理用户头像")
    private String proxyUserHeadImg;
}
