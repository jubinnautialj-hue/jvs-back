package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
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
import java.util.List;

/**
 * <p>
 * 字段设置
 * 函数对应关系
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("字段设置-函数对应关系")
@Accessors(chain = true)
@TableName(value = "field_setting_function", autoResultMap = true)
public class FieldSettingFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("原字段类型-例如 int  date char")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> oldFieldType;
    @ApiModelProperty("原字段类型 分类-例如 字符串 时间 数字")
    private DataFieldTypeClassifyEnum oldFieldTypeClassify;
    @ApiModelProperty("子级")
    private String children;


}
