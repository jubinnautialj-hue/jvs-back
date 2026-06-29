package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 文件同步日志
 * </p>
 *
 * @author admin
 * @since 2023-04-20
 */
@Data
@Accessors(chain = true)
@TableName("dc_sync_log")
@ApiModel(value = "DcSyncLog对象", description = "文件同步日志")
public class DcSyncLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("文库id")
    @TableField("dc_id")
    private String dcId;

    @ApiModelProperty("文库名称")
    @TableField("dc_name")
    private String dcName;

    @ApiModelProperty("同步文件成功的数量")
    @TableField("succeed_number")
    private Integer succeedNumber;

    @ApiModelProperty("同步消耗时间（毫秒）")
    @TableField("sync_time")
    private Long syncTime;

    @ApiModelProperty("同步标识（前端决定）")
    @TableField("sync_indicator")
    private String syncIndicator;

}
