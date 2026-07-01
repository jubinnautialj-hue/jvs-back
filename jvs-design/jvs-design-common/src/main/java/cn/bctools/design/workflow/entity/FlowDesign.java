package cn.bctools.design.workflow.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流设计
 */
@Design(DesignType.workflow)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("工作流设计")
@TableName(value = "jvs_flow_design", autoResultMap = true)
public class FlowDesign extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流名称")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "工作流流程设计JSON(发布的版本)", notes = "升级到2.1.9后存储到版本表，此字段会清空。后续可能会删除此字段")
    @TableField(value = "design", updateStrategy = FieldStrategy.IGNORED)
    private String design;

    @ApiModelProperty(value = "工作流流程设计JSON(设计中的版本)", notes = "升级到2.1.9后存储到版本表，此字段会清空。后续可能会删除此字段")
    @TableField(value = "designing", updateStrategy = FieldStrategy.IGNORED)
    private String designing;

    @ApiModelProperty("数据模型id")
    @TableField("data_model_id")
    private String dataModelId;

    @ApiModelProperty("分类")
    @TableField("design_group")
    private String designGroup;

    @ApiModelProperty("图标")
    @TableField("icon")
    private String icon;

    @ApiModelProperty("扩展")
    @TableField(value = "extend", typeHandler = Fastjson2TypeHandler.class)
    private FlowExtendDto extend;

    @ApiModelProperty("描述")
    @TableField("description")
    private String description;

    @ApiModelProperty("应用ID")
    private String jvsAppId;

    @ApiModelProperty("是否已发布：0-未发布，1-已发布")
    @TableField("published")
    private Boolean published;

    @ApiModelProperty("流程设计是否已变更：0-未变更，1-已变更")
    @TableField("design_changed")
    private Boolean designChanged;

    @ApiModelProperty(value = "发起人表单id")
    @TableField(value = "form_id", updateStrategy = FieldStrategy.IGNORED)
    private String formId;

    @ApiModelProperty(value = "发起人表单版本")
    @TableField(value = "form_version", updateStrategy = FieldStrategy.IGNORED)
    private String formVersion;

    @ApiModelProperty("关联的逻辑设计id集合")
    @TableField(value = "rule_keys", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<String> ruleKeys;

    @ApiModelProperty("是否删除 0未删除  1已删除")
    @TableField("del_flag")
    @TableLogic
    private Boolean delFlag;

    @ApiModelProperty("列表设计iid")
    @TableField(value = "page_id", updateStrategy = FieldStrategy.IGNORED)
    private String pageId;

    @ApiModelProperty(value = "工作流设计", notes = "工作流设计相关功能统一使用此字段")
    @TableField(exist = false)
    private String designBody;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;
}
