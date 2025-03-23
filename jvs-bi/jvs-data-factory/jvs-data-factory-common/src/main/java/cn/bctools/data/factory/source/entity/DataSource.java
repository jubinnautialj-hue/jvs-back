package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.enums.OperationEnum;
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
 * @author : admin
 */
@Data
@ApiModel("数据源配置信息")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_data_source", autoResultMap = true)
public class DataSource extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("来源名称")
    @TableField("source_name")
    private String sourceName;

    @ApiModelProperty("来源类型 mongo mysql 这里不使用枚举 直接在代码层面限制")
    @TableField("source_type")
    private DataSourceTypeEnum sourceType;
    /**
     * 租户id
     */
    private String tenantId;
    @ApiModelProperty(value = "数据源的链接信息 不同的数据源链接信息不一样")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject customJson;

    @ApiModelProperty("同步状态 0失败 1成功2同步中")
    private Integer syncStructure;

    @ApiModelProperty("验证是否通过")
    private Boolean checkIs;

    @ApiModelProperty("是否开启实时日志")
    private Boolean realTimeOpen;

    @ApiModelProperty("权限设置")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<JSONObject> role;

    @ApiModelProperty("数据源表结构")
    @TableField(exist = false)
    private List<DataSourceStructure> children;
    @ApiModelProperty("数据用于导出时的扩展记录")
    @TableField(exist = false)
    private JSONObject exportData;

    @TableField(exist = false)
    @ApiModelProperty("操作权限")
    List<OperationEnum> operationList;

    @ApiModelProperty("jar包名称永远前端回显")
    @TableField(exist = false)
    private String sysJarName;
    @ApiModelProperty("权限类型,true 应用 权限，false 自定义权限")
    Boolean roleType;

}


