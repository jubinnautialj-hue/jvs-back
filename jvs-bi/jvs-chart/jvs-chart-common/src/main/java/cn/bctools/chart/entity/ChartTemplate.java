package cn.bctools.chart.entity;

import cn.bctools.chart.enums.ChartPageSource;
import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.database.entity.po.BasePo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 页面配置
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_template", autoResultMap = true)
@ApiModel
public class ChartTemplate extends BasePo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("组件json")
    private String dataJson;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("封面桶名称")
    private String coverFilePath;
    @ApiModelProperty("封面文件路径")
    private String coverBucketName;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("封面url")
    @TableField(exist = false)
    private String coverUrl;
}
