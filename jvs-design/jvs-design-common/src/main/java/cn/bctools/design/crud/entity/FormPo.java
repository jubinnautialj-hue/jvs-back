package cn.bctools.design.crud.entity;

import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.database.entity.po.BasePo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.fields.enums.FormTypeEnum;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.project.handler.Design;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 表单配置项
 *
 * @author auto
 */
@Design(DesignType.form)
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_crud_form", autoResultMap = true)
public class FormPo extends BasePo implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("表单名称")
    private String name;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("表单类型不能为空")
    private FormTypeEnum classify;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计json对象")
    private String viewJson;
    @ApiModelProperty("是否已发布")
    private Boolean isDeploy;
    @ApiModelProperty("支持的客户端类型")
    private SupportedClientType supportedClientType;
    @ApiModelProperty("是否校验登录, 默认为true")
    private Boolean checkLogin;
    @ApiModelProperty("回调地址")
    private String callbackUrl;
    @ApiModelProperty("移动端二维码")
    private String mobileCode;
    @ApiModelProperty("回调方式(true:异步; false:同步)")
    private Boolean callbackAsync;
    @ApiModelProperty("数据模型ID")
    private String dataModelId;
    /**
     * 此关联字段用作表单联动，公式触发时使用的时候优化的数据
     */
    @ApiModelProperty("设置此表单字段的关联字段")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Set<String> associationSettingsFields;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("二维码标签设置")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private JSONObject tagSetting;

    @ApiModelProperty("设计字段和数据字段的结构关系图")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, String> fieldPathMap;

    @TableField(exist = false)
    @ApiModelProperty("归类")
    private String type;

    @TableField(exist = false)
    @ApiModelProperty("菜单")
    private AppMenu appMenu;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;
}
