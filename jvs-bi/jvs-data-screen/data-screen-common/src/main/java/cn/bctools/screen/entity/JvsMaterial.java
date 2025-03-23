package cn.bctools.screen.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 素材管理
 * </p>
 *
 * @author admin
 * @since 2023-09-14
 */
@Data
@TableName("jvs_material")
@ApiModel(value = "JvsMaterial对象", description = "素材管理")
public class JvsMaterial extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @ApiModelProperty("文件名称")
    @TableField("name")
    @NotBlank(message = "未设置素材名称")
    private String name;

    @ApiModelProperty("文件地址 minio")
    @TableField("file_name")
    @NotBlank(message = "文件地址为空")
    private String fileName;

    @ApiModelProperty("桶名称")
    @TableField("bucket_name")
    @NotBlank(message = "文件桶名称为空")
    private String bucketName;

    @ApiModelProperty("所属租户")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("文件连接")
    @TableField(exist = false)
    private String fileLink;


}
