package cn.bctools.design.data.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.data.fields.enums.DataFieldType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;

/**
 * 数据字段,通过业务数据创建或修改表单或列表的组件设计
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "jvs_data_field_dynamic", autoResultMap = true)
public class DataFieldDynamicPo extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("数据字段id")
    private String id;

    @ApiModelProperty("数据模型id")
    private String modelId;

    @ApiModelProperty("套件设计id")
    private String designId;

    @ApiModelProperty("应用ID")
    private String jvsAppId;

    @ApiModelProperty("字段名称")
    private String prop;

    @ApiModelProperty("显示名称")
    private String label;

    @ApiModelProperty("字段类型")
    private DataFieldType type;

    @ApiModelProperty("字典")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map fieldJson;

}
