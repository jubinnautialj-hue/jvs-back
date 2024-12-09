package cn.bctools.design.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 */
@Getter
@Setter
@ApiModel("应用版本id映射")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("jvs_app_version_mapping")
public class JvsAppVersionMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("源设计id(开发版设计id)")
    @TableField("source_design_id")
    private String sourceDesignId;

    @ApiModelProperty("测试版设计id")
    @TableField("beta_design_id")
    private String betaDesignId;

    @ApiModelProperty("正式版设计id")
    @TableField("ga_design_id")
    private String gaDesignId;

    @ApiModelProperty("所属应用唯一标识")
    private String affiliationApp;
}
