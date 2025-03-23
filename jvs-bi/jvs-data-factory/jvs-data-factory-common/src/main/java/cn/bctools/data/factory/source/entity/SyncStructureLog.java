package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.enums.OperationStateEnums;
import cn.bctools.data.factory.source.enums.StateEnums;
import cn.bctools.data.factory.source.handler.ListTypeStructureLogHandler;
import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("数据源表结构同步日志")
@Accessors(chain = true)
@TableName(value = "sync_structure_log", autoResultMap = true)
public class SyncStructureLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("状态")
    private StateEnums state;
    @ApiModelProperty("报错信息")
    private String errorLog;
    @ApiModelProperty("是否存在变更")
    private Boolean alterationIs;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("数据源id")
    private String dataSourceId;
    @ApiModelProperty("具体变更结构")
    @TableField(typeHandler = ListTypeStructureLogHandler.class)
    private List<TableDetail> tableDetail;


    @Data
    @ApiModel("表详细")
    @Accessors(chain = true)
    public static class TableDetail implements Serializable {
        @ApiModelProperty("表名称")
        private String tableName;
        @ApiModelProperty("状态")
        private OperationStateEnums stateEnums;
        @ApiModelProperty("具体变更对比-字段详细")
        private List<FieldComparison> fieldComparisons;

    }

    @Data
    @ApiModel("字段对比")
    @Accessors(chain = true)
    public static class FieldComparison implements Serializable {
        @ApiModelProperty("历史字段详细")
        private FieldDetail oldFieldDetail;
        @ApiModelProperty("新的字段详细")
        private FieldDetail newFieldDetail;
        @ApiModelProperty("状态")
        private OperationStateEnums stateEnums;
    }

    @Data
    @ApiModel("字段详细")
    @Accessors(chain = true)
    public static class FieldDetail implements Serializable {
        @ApiModelProperty("原字段名称")
        private String columnName;
        @ApiModelProperty("长度-例如 varchar  datetime DECIMAL")
        private Integer length;
        @ApiModelProperty("精度-DECIMAL 类型")
        private Integer precision;
        @ApiModelProperty("格式 例如时间:YYYY-MM-DD HH:MM:SS")
        private String format;
        @ApiModelProperty("字段类型明细")
        private DataFieldTypeEnum dataFieldTypeEnum;
        @ApiModelProperty("字段类型分类")
        private DataFieldTypeClassifyEnum dataFieldTypeClassify;
        @ApiModelProperty("字段类型-数据库取出来的")
        private String dataType;
        @ApiModelProperty("字段解释")
        private String columnCount;
    }
}




