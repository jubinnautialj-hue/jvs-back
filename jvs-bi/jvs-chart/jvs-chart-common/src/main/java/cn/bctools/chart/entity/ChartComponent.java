package cn.bctools.chart.entity;

import cn.bctools.chart.data.ComponentJson;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_component", autoResultMap = true)
@ApiModel
public class ChartComponent {

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("页面ID")
    private String pageId;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("名称")
    private String type;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private ComponentJson componentJson;

}
