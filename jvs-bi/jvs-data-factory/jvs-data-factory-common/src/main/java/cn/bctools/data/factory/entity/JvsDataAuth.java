package cn.bctools.data.factory.entity;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.enums.JvsDataAuthTypeEnum;
import cn.bctools.data.factory.query.QueryDto;
import cn.bctools.database.entity.po.BasalPo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 数据权限
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("数据权限")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_data_auth", autoResultMap = true)
public class JvsDataAuth extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("数据集id")
    private String dataFactoryId;
    @ApiModelProperty("权限名称")
    private String authName;
    @ApiModelProperty("权限类型")
    private JvsDataAuthTypeEnum authType;
    @ApiModelProperty("权限数据")
    @TableField(value = "auth_data", typeHandler = FastjsonTypeHandler.class)
    private JSONObject authData;

    @Data
    @Accessors(chain = true)
    public static class RowAuthData {
        @ApiModelProperty("字段信息")
        private List<DataSourceField> queryDto;
        @ApiModelProperty("人员")
        private List<PersonnelDto> personnel;
    }

    @Data
    @Accessors(chain = true)
    public static class ColumnAuthData {
        @ApiModelProperty("过滤数据")
        private QueryDto queryDto;
        @ApiModelProperty("人员")
        private List<PersonnelDto> personnel;
    }

}
