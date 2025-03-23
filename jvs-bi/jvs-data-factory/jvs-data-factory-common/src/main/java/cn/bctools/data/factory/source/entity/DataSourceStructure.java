package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.data.po.InParameterJsonPo;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.bctools.data.factory.source.data.po.SettingJsonPo;
import cn.bctools.data.factory.source.dto.RealTimeSettingDto;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.handler.ListTypeInParameterHandler;
import cn.bctools.data.factory.source.handler.ListTypeJsonAnalysisHandler;
import cn.bctools.data.factory.source.handler.ListTypeStructureHandler;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : admin
 */
@Data
@ApiModel("数据源结构")
@Accessors(chain = true)
@TableName(value = "jvs_data_source_structure", autoResultMap = true)
public class DataSourceStructure implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("执行时的入参 例如表名称或者url 如果是智仓或者低代码的数据就自己把id 赋值到这个字段,这个名字应该改为 execute_obj好点")
    @TableField("execute_name")
    private String executeName;

    @ApiModelProperty("表-解释")
    @TableField("table_name_desc")
    private String tableNameDesc;


    @ApiModelProperty("数据源id")
    @TableField("data_source_id")
    private String dataSourceId;

    @ApiModelProperty("实时同步是否开启")
    @TableField("real_time_is_open")
    private Boolean realTimeIsOpen;
    @ApiModelProperty("doris表名称")
    @TableField("ods_table_name")
    private String odsTableName;
    @ApiModelProperty("实时同步配置")
    @TableField(value = "real_time_setting", typeHandler = FastjsonTypeHandler.class)
    private RealTimeSettingDto realTimeSetting;
    @ApiModelProperty("实时同步配置是否配置")
    @TableField("real_time_setting_is_open")
    private Boolean realTimeSettingIsOpen;
    @ApiModelProperty("实时同步开启时的seaTunnel系统的任务Id")
    private String seaTunnelId;
    @ApiModelProperty("源数据是否存在唯一key")
    private Boolean primaryIs;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    /**
     * 租户id
     */
    private String tenantId;
    @ApiModelProperty("api数据是否测试通过只有通过的数据才能使用")
    private Boolean checkIs;

    @ApiModelProperty("具体结构数据")
    @TableField(typeHandler = ListTypeStructureHandler.class)
    private List<Structure> structure;

    @ApiModelProperty("入参")
    @TableField(typeHandler = ListTypeInParameterHandler.class)
    private List<InParameterJsonPo> inParameterJson;

    @ApiModelProperty("自定义json")
    private String customJson;

    @ApiModelProperty("认证json")
    private String otherJson;

    @ApiModelProperty("具体的json结构-前置解析的")
    @TableField(typeHandler = ListTypeJsonAnalysisHandler.class)
    private List<JsonAnalysisPo> jsonAnalysis;

    @ApiModelProperty("具体的json结构-平铺的数据")
    @TableField(typeHandler = ListTypeJsonAnalysisHandler.class)
    private List<JsonAnalysisPo> carveBuiltJson;


    @ApiModelProperty("返回值设置")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private SettingJsonPo settingJson;

    @ApiModelProperty("字段总量")
    private Integer fieldCount;


    @ApiModelProperty(value = "是否包含子节点")
    @TableField(exist = false)
    private boolean hasChildren = true;

    @ApiModelProperty(value = "数据源名称-库名称")
    @TableField(exist = false)
    private String libraryName;

    @ApiModelProperty(value = "数据源类型")
    @TableField(exist = false)
    private DataSourceTypeEnum dataSourceTypeEnum;


    @Data
    @ApiModel("结构")
    @Accessors(chain = true)
    public static class Structure implements Serializable {
        @ApiModelProperty("别名 统一通过id生成器生成")
        private String columnName;

        @ApiModelProperty("用于判断是否需要重新生成别名")
        private Boolean newColumnNameIs = true;

        @ApiModelProperty("字段名称 原来的名称")
        private String originalColumnName;

        @ApiModelProperty("来源id-可以是表名称 可以是数据源id根据不同的数据源需要的入参决定-注意 这里是返回才会存在")
        private String from;
        @ApiModelProperty("长度-例如 varchar  datetime DECIMAL")
        private Integer length;
        @ApiModelProperty("精度-DECIMAL 类型")
        private Integer precision;
        @ApiModelProperty("格式 例如时间:YYYY-MM-DD HH:MM:SS")
        private String format;

        @ApiModelProperty("默认format范围")
        private String formatDefault;
        @ApiModelProperty("是否为主键")
        private Boolean primaryIs;

        @ApiModelProperty("字段类型明细")
        private DataFieldTypeEnum dataFieldTypeEnum;

        @ApiModelProperty("字段类型分类")
        private DataFieldTypeClassifyEnum dataFieldTypeClassify;

        @ApiModelProperty("字段类型-数据库取出来的")
        private String dataType;
        @ApiModelProperty("dataX 对应的字段属性方便导入配置文件的编写")
        private String dataXType;

        @ApiModelProperty("对应doris的字段类型")
        private String dorisType;

        @ApiModelProperty("datax同步时数据转换--因为可能存在datax不支持的类型就可以通过函数转换 格式如下:cast({columnName} as text ) as {columnName}")
        private String function;

        @ApiModelProperty("是否为主键")
        private Boolean primaryKey;

        @ApiModelProperty("子集数据")
        private List<Structure> items;

        @ApiModelProperty("字段解释")
        private String columnCount;
    }
}


