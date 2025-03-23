package cn.bctools.remote.po;

import cn.bctools.remote.dto.DataSourceDetail;
import cn.bctools.remote.enums.CredentialTypeEnum;
import cn.bctools.remote.handler.DataSourceDetailTypeHandler;
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

/**
 * <p>
 * api服务
 * </p>
 *
 * @author admin
 * @since 2023-03-20
 */
@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "jvs_data_remote_server", autoResultMap = true)
@ApiModel(value = "JvsDataRemote对象", description = "api服务")
@Accessors(chain = true)
public class JvsDataRemoteServer extends RolePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("服务名称")
    @TableField("name")
    @NotBlank(message = "服务名称不能为空")
    @Size(max = 50, message = "服务名称过长,限制50字")
    private String name;

    @ApiModelProperty("服务说明")
    @TableField("remark")
    @Size(max = 500, message = "服务描述过长,限制500字")
    private String remark;

    @ApiModelProperty("白名单开启状态")
    @TableField("white_status")
    private Boolean whiteStatus;

    @ApiModelProperty("白名单")
    @TableField("white_list")
    private String whiteList;

    @ApiModelProperty("凭证类型")
    @TableField("credential_type")
    private CredentialTypeEnum credentialType;

    @ApiModelProperty("是否启用")
    @TableField("enable_is")
    private Boolean enableIs;

    @ApiModelProperty("是否异步")
    @TableField("async_is")
    private Boolean asyncIs;

    @ApiModelProperty("回调地址")
    //@TableField("callback_addr")
    @TableField(exist = false)
    private String callbackAddr;

    @ApiModelProperty("是否使用缓存")
    @TableField("cache_is")
    private Boolean cacheIs;

    @ApiModelProperty("响应示例")
    @TableField("example_resp")
    private String exampleResp;

    @ApiModelProperty("服务入参")
    @TableField("in_parameter")
    private String inParameter;

    @ApiModelProperty("服务出参")
    @TableField("out_parameter")
    private String outParameter;

    @ApiModelProperty("入参")
    @TableField("source_in_parameter")
    private String sourceInParameter;

    @ApiModelProperty("目录id")
    @TableField("type")
    @NotBlank(message = "未选择目录")
    private String type;

    @ApiModelProperty("排序")
    @TableField("sort")
    private int sort;

    @ApiModelProperty("凭证组")
    @TableField("secret_json")
    private String secretJson;

    @ApiModelProperty("数据源信息")
    @TableField(value = "data_source_detail", typeHandler = DataSourceDetailTypeHandler.class)
    private DataSourceDetail dataSourceDetail;

    @ApiModelProperty("租户id")
    @TableField("tenant_id")
    private String tenantId;

}
