package cn.bctools.design.data.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import com.alibaba.fastjson2.JSONArray;
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
 * 数据字段
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "jvs_data_field", autoResultMap = true)
public class DataFieldPo extends BasalPo implements Serializable {

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

    @ApiModelProperty("套件设计类型(表单,列表页)")
    private DesignType designType;

    @ApiModelProperty("字段名称")
    private String fieldKey;

    @ApiModelProperty("是否可导出")
    private Boolean isExport;

    @ApiModelProperty("显示名称")
    private String fieldName;

    @ApiModelProperty("字段类型")
    private DataFieldType fieldType;

    /**
     * 数据类型为: {@link cn.bctools.design.data.fields.enums.DataQueryType}
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("支持的查询类型")
    private JSONArray enabledQueryTypes;
    @ApiModelProperty("数据联动条件")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private JSONArray dataLinkageList;
    @ApiModelProperty("数据联动模型id")
    private String dataLinkageModelId;
    @ApiModelProperty("数据联动显示的字段值")
    private String linkageFieldKey;
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("字段设计数据(Json格式)")
    private Map<String, Object> designJson;
    @ApiModelProperty("关联表单的设计id值")
    private String dataModelId;

    @ApiModelProperty("正则")
    private String encryptionExpress;

    @ApiModelProperty("是否开启正则")
    private Boolean encryption;
    @ApiModelProperty("轻应用版本号")
    private String appVersion;
}
