package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.enums.SysSettingDataTypeEnums;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 数据etl
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("数据etl")
@Accessors(chain = true)
@TableName(value = "sys_setting", autoResultMap = true)
public class SysSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("数据json")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject dataJson;
    @ApiModelProperty("数据类型")
    private SysSettingDataTypeEnums dataType;
    @ApiModelProperty("explain_txt")
    private String explainTxt;

}
