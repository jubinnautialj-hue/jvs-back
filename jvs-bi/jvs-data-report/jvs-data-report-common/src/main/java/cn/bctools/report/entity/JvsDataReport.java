package cn.bctools.report.entity;

import cn.bctools.report.enums.EReportType;
import cn.bctools.report.field.UWorkbookTypeHandler;
import cn.bctools.report.model.auth.BaseAuthPo;
import cn.bctools.report.model.univer.UWorkbook;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p>
 * 报表设计
 * </p>
 *
 * @author admin
 * @since 2024-12-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_data_report",autoResultMap = true)
@ApiModel(value = "报表信息", description = "报表设计")
public class JvsDataReport extends BaseAuthPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("报表名称")
    @TableField("report_name")
    @NotBlank(message = "未设置报表名称")
    @Size(max = 25,min = 1,message = "名称长度不符")
    private String reportName;

    @ApiModelProperty("报表描述")
    @TableField("report_desc")
    private String reportDesc;

    @ApiModelProperty("报表设计")
    @TableField(value = "report_design",typeHandler = UWorkbookTypeHandler.class)
    private UWorkbook reportDesign;

    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("目录id")
    @TableField("menu_id")
    private String menuId;

    @ApiModelProperty("逻辑删除")
    @TableField("del_flag")
    private Boolean delFlag;

    @ApiModelProperty("报表类型")
    @TableField("report_type")
    private EReportType reportType;

    @ApiModelProperty("是否开启引导模式")
    @TableField("guidance_mode")
    private Boolean guidanceMode;

    @ApiModelProperty("租户ID")
    @TableField("tenant_id")
    private String tenantId;


    public void clear(){
        this.setCreateById(null);
        this.setCreateTime(null);
        this.setUpdateBy(null);
        this.setUpdateTime(null);
        this.setTenantId(null);
        this.setCreateBy(null);
        this.setRoleType(false);
        this.setRole(new ArrayList<>());
        this.setMenuId(null);
    }

}
