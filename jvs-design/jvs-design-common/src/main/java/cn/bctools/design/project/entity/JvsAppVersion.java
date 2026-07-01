package cn.bctools.design.project.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
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
@ApiModel("应用版本")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("jvs_app_version")
public class JvsAppVersion extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("应用版本号")
    @TableField("app_version_code")
    private String appVersion;

    @ApiModelProperty("备注")
    @TableField("description")
    private String description;

    @ApiModelProperty("版本类型")
    @TableField("version_type")
    private AppVersionTypeEnum versionType;

    @ApiModelProperty("版本状态")
    @TableField("version_status")
    private AppVersionStatusEnum versionStatus;

    @ApiModelProperty("版本模板id")
    @TableField("template_id")
    private String templateId;

    @ApiModelProperty("应用id")
    @TableField("jvs_app_id")
    private String jvsAppId;

    @TableField("tenant_id")
    @ApiModelProperty("租户id")
    private String tenantId;

    @ApiModelProperty("所属应用唯一标识")
    private String affiliationApp;

    @ApiModelProperty(value = "模板")
    @TableField(exist = false)
    private JvsAppTemplate jvsAppTemplate;
}
