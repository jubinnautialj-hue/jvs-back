package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档管理")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_library_administration")
public class DcLibraryAdministration extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("数据权限类型")
    @TableField("user_type")
    private DataAuthTypeEnum userType;
    @ApiModelProperty("用户id，系统角色id，部门id，群组id，岗位id")
    @TableField("user_id")
    private String userId;
    @ApiModelProperty("权限类型")
    private DcLibraryReadEnum libraryType;
}
