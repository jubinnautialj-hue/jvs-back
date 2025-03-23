package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.DcUtilOperationTypeEnum;
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

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("工具类")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_util")
public class DcUtil  extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("是否删除 0未删除  1已删除")
    @TableField("del_flag")
    private String delFlag;
    @ApiModelProperty("租户ID")
    @TableField("tenant_id")
    private String tenantId;
    @ApiModelProperty("名称-此名称用于文件生成时的名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("文件操作类型")
    @TableField("operation_type")
    private DcUtilOperationTypeEnum operationType;
    @ApiModelProperty("扩展参数-不同的操作类型可能会存在不同的参数")
    @TableField("extend_json")
    private String extendJson;
}
