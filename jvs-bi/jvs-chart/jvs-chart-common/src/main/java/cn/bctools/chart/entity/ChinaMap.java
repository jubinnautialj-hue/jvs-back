package cn.bctools.chart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 中国地图数据
 *
 * @author zqs
 */
@Data
@Accessors(chain = true)
@TableName(value = "china_map", autoResultMap = true)
@ApiModel
public class ChinaMap implements Serializable {
    private static final long serialVersionUID = -5623407547220439967L;
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("code")
    private String code;

}
