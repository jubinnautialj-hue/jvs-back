package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author : admin
 */
@Data
@ApiModel("doris字段映射")
@Accessors(chain = true)
@TableName(value = "jvs_source_to_doris", autoResultMap = true)
public class JvsSourceToDoris implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("来源名称")
    @TableField("source_type")
    private DataSourceTypeEnum sourceType;
    @ApiModelProperty("字段类型")
    private String fieldType;
    @ApiModelProperty("doris字段类型")
    private String dorisFieldType;
    @ApiModelProperty("有效位数或者长度")
    private int length;
    @ApiModelProperty("精度")
    private int fieldPrecision;
    @ApiModelProperty("字段类型-明细")
    private DataFieldTypeEnum dataFieldType;
    @ApiModelProperty("字段类型-分类")
    private DataFieldTypeClassifyEnum dataFieldTypeClassify;
    @ApiModelProperty("datax同步时数据转换--因为可能存在datax不支持的类型就可以通过函数转换 格式如下:cast({columnName} as text ) as {columnName}")
    private String functionStr;

}


