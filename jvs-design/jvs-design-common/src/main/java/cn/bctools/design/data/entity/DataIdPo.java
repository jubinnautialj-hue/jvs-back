package cn.bctools.design.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据自增序号
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@Accessors(chain = true)
@TableName(value = "jvs_data_id", autoResultMap = true)
public class DataIdPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("数据字段id")
    private Integer id;

    @ApiModelProperty(value = "模型id")
    private String modelId;

    @ApiModelProperty(value = "流程设计id")
    private String flowDesignId;

    @ApiModelProperty("当前序号(不重置)")
    private Integer currentId;

    @ApiModelProperty("当前序号(按年重置)")
    private Integer currentYearId;

    @ApiModelProperty("当前序号(按月重置)")
    private Integer currentMonthId;

    @ApiModelProperty("当前序号(按天重置)")
    private Integer currentDayId;

    @ApiModelProperty("当前序号(按小时重置)")
    private Integer currentHourId;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    public void next() {
        this.currentId++;
        this.currentYearId++;
        this.currentMonthId++;
        this.currentDayId++;
        this.currentHourId++;
    }

}
