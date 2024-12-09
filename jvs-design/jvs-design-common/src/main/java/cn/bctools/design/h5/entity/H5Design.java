package cn.bctools.design.h5.entity;

import cn.bctools.database.entity.po.BasePo;
import com.baomidou.mybatisplus.annotation.*;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("H5设计")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_h5_design", autoResultMap = true)
public class H5Design extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("实例名称")
    private String name;
    @ApiModelProperty("设计的json")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    public Map<String, Object> designDrawingJson;
    @NotBlank(message = "类型不能为空")
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("状态")
    private Boolean enable;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

}
