package cn.bctools.document.entity;

import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.handler.DcAuthConfigAuthSignHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档参与人与权限绑定关系")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "dc_auth_config", autoResultMap = true)
public class DcAuthConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("权限标识")
    @TableField(value = "auth_sign", typeHandler = DcAuthConfigAuthSignHandler.class)
    private List<IdentifyingAuthDto> authSign;
    @ApiModelProperty("用户id，系统角色id，部门id，群组id，岗位id")
    @TableField("user_id")
    private String userId;
    @ApiModelProperty("数据权限类型")
    @TableField("data_auth_type")
    private DataAuthTypeEnum dataAuthType;
    @ApiModelProperty("文档id")
    @TableField("dc_id")
    private String dcId;
    @ApiModelProperty("分组key")
    @TableField("group_key")
    private String groupKey;
    @ApiModelProperty("类型/知识库、目录、文本文档、表格文档、脑图文档、流程文档。")
    private DcLibraryTypeEnum type;
    @ApiModelProperty("上级ID")
    private String parentId;
    @ApiModelProperty("知识库id")
    private String knowledgeId;
    @ApiModelProperty("路径id")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> pathId;
    @ApiModelProperty("头像")
    private String headImg;
    @ApiModelProperty("名称")
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DcAuthConfig that = (DcAuthConfig) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
