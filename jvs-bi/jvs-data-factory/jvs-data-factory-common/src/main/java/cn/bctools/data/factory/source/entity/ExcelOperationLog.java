package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.enums.ExcelClassifyType;
import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * excel数据源操作日志
 * </p>
 *
 * @author admin
 * @since 2024-06-17
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@TableName("excel_operation_log")
@ApiModel(value = "ExcelOperationLog对象", description = "excel数据源操作日志")
public class ExcelOperationLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("数据源id")
    @TableField("data_source_id")
    private String dataSourceId;

    @ApiModelProperty("租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("操作类型")
    @TableField("operation_type")
    private String operationType;

    @ApiModelProperty("操作状态")
    @TableField("status")
    private Boolean status;

    @ApiModelProperty("文件名称")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty("操作类型-子类")
    @TableField("operation_classify")
    private ExcelClassifyType operationClassify;


}
