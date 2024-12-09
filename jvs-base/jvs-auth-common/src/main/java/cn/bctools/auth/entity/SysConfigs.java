//package cn.bctools.auth.entity;
//
//import cn.bctools.common.enums.ConfigsTypeEnum;
//import cn.bctools.gateway.entity.TypeEnum;
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.experimental.Accessors;
//
//import java.io.Serializable;
//
///**
// * @author Auto Generator
// */
//@Data
//@Accessors(chain = true)
//@AllArgsConstructor
//@NoArgsConstructor
//@EqualsAndHashCode(callSuper = false)
//@TableName(value = "sys_configs", autoResultMap = true)
//public class SysConfigs implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    @TableId(value = "id", type = IdType.ASSIGN_UUID)
//    @ApiModelProperty(value = "主键ID")
//    private String id;
//    @ApiModelProperty(value = "租户id")
//    private String tenantId;
//    @ApiModelProperty(value = "配置类型")
//    private ConfigsTypeEnum type;
//    @ApiModelProperty("保存对象")
//    private String content;
//    @ApiModelProperty("客户端信息")
//    private String clientId;
//    @ApiModelProperty("等级")
//    private TypeEnum level;
//
//
//}
