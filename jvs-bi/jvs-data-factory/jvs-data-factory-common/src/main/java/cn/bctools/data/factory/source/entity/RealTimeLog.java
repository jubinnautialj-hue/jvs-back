package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.source.enums.StateEnums;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : admin
 */
@Data
@ApiModel("实时任务同步日志")
@Accessors(chain = true)
@TableName(value = "real_time_log", autoResultMap = true)
public class RealTimeLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("表结构id")
    @TableField("data_structure_id")
    private String dataStructureId;

    @ApiModelProperty("表名称")
    @TableField("table_name")
    private String tableName;
    @ApiModelProperty("doris表名称")
    @TableField("ods_table_name")
    private String odsTableName;

    @ApiModelProperty("上一次验证的时间")
    @TableField("check_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    @ApiModelProperty("任务状态")
    @TableField("task_status")
    private StateEnums taskStatus;

    @ApiModelProperty("实时同步开启时的seaTunnel系统的任务Id")
    private String seaTunnelId;

    @ApiModelProperty("报错内容")
    private String errorMsg;

    @ApiModelProperty("创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
