package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author ZhuXiaoKang
 * @Description 公用数据库字段
 */
@Data
@Accessors(chain = true)
public class BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;
    @ApiModelProperty(value = "租户")
    private String tenantId;
    @ApiModelProperty(value = "0-正常，1-删除")
    @TableLogic
    private Boolean delFlag;
}
