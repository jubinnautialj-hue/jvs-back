package cn.bctools.design.data.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.IndexFields;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.data.handler.IndexFieldsHandler;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据模型
 *
 * @Author: GuoZi
 */
@Design(DesignType.data)
@Slf4j
@Data
@Accessors(chain = true)
@TableName(value = "jvs_data_model", autoResultMap = true)
public class DataModelPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("数据模型id")
    private String id;

    @ApiModelProperty("应用id")
    private String appId;

    @ApiModelProperty("首次创建的设计套件id")
    private String designId;

    @ApiModelProperty("首次创建的设计套件类型")
    private DesignType designType;

    @ApiModelProperty("模型名称")
    private String name;
    @TableField("workflow_id")
    private String workflowId;
    @ApiModelProperty("是否启用工作流")
    private Boolean enableWorkflow;

    /**
     * 数据类型见: {@link FieldPublicHtml}
     */
    @TableField(typeHandler = IndexFieldsHandler.class, updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty("索引信息")
    private List<IndexFields> indexFields;
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("设置信息")
    private DataSettingBo setting;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

    @ApiModelProperty("权限设置")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<JSONObject> role;

    public List<DesignRole> getRoles() {
        return JSONArray.parseArray(JSONObject.toJSONString(this.getRole()), DesignRole.class);
    }

    @ApiModelProperty("模型数据大小 单位:字节")
    @TableField("data_size")
    private Long size;

    @ApiModelProperty(value = "数据集名", notes = "默认与模型id相同")
    private String collectionName;

    @ApiModelProperty(value = "使用模型字段", notes = "启用后,模型字段与模型的设计字段分开保存")
    private Boolean enableModelField;

    @ApiModelProperty(value = "租户Id")
    private String tenantId;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;
    @ApiModelProperty("所属模式")
    private AppVersionTypeEnum belongMode;

    @ApiModelProperty("模型编码标识")
    private String tableCode;
}
