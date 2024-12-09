package cn.bctools.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_permission", autoResultMap = true)
public class Permission implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 该值唯一, 默认使用uuid
     */
    @ApiModelProperty(value = "资源标识()")
    private String permission;
    @ApiModelProperty(value = "资源名称")
    private String name;
    @ApiModelProperty(value = "请求地址")
    private String api;
    @ApiModelProperty(value = "请求请求方式")
    private String methodType;
    @ApiModelProperty(value = "分类")
    private TypeEnum type;
    @ApiModelProperty(value = "功能分组")
    private String groupName;
    @ApiModelProperty(value = "客户端或服务端名称")
    private Integer sort;
    @ApiModelProperty(value = "服务端名称")
    private String clientName;
    @ApiModelProperty(value = "解释说明")
    private String remark;

}
