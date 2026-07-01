package cn.bctools.design.crud.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * 数据操作规则
 *
 * @author auto
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_crud_association", autoResultMap = true)
public class CrudAssociationPo implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("按钮名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("按钮名称")
    @TableField("permissionFlag")
    private String permissionFlag;
    @ApiModelProperty("设计id、表单、列表页")
    @TableField("design_id")
    private String designId;
    @ApiModelProperty("数据处理集合")
    @TableField(value = "data", typeHandler = Fastjson2TypeHandler.class)
    private List data;
    @ApiModelProperty("应用")
    @TableField("jvs_app_id")
    private String jvsAppId;
    @ApiModelProperty("数据模型ID")
    @TableField("data_model_id")
    private String dataModelId;
    @ApiModelProperty("轻应用版本号")
    private String appVersion;
}
