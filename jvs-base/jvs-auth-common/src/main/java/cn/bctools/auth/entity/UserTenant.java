package cn.bctools.auth.entity;

import cn.bctools.auth.entity.handler.DeptJsonTypeHandler;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.ArrayTypeHandler;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_user_tenant", autoResultMap = true)
public class UserTenant implements Serializable {


    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键ID")
    String id;
    @ApiModelProperty(value = "用户ID")
    String userId;
    @ApiModelProperty(value = "用户真名")
    String realName;
    @ApiModelProperty(value = "手机")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String phone;
    @ApiModelProperty(value = "职工编号")
    String employeeNo;
    @ApiModelProperty(value = "帐号等级")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String accountLevel;
    @ApiModelProperty(value = "帐号等级Id")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String accountLevelId;
    @ApiModelProperty(value = "岗位ID")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String jobId;
    @ApiModelProperty(value = "岗位名称")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    String jobName;
    @ApiModelProperty(value = "部门对象")
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = DeptJsonTypeHandler.class)
    List<String> deptId;
    @ApiModelProperty(value = "租户ID")
    String tenantId;
    @ApiModelProperty(value = "0-正常，1-注销  不要逻辑删除，删除后，业务找不到数据")
    Boolean cancelFlag;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createTime;


}
