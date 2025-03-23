package cn.bctools.chart.entity;

import cn.bctools.chart.enums.ChartPageSource;
import cn.bctools.common.enums.SupportedClientType;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 页面配置
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_page", autoResultMap = true)
@ApiModel
public class ChartPage extends RolePo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("支持的客户端类型")
    private SupportedClientType supportedClientType;
    @ApiModelProperty("是否校验登录, 默认为true")
    private Boolean checkLogin;
    @ApiModelProperty("发布状态")
    private Boolean isDeploy;
    @NotBlank(message = "类型不能为空")
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("组件json")
    private String dataJson;
    @ApiModelProperty("筛选条件")
    private String filterJson;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("定时请求")
    private String timerRequest;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("固定搜索栏")
    private Boolean isFixed;
    @ApiModelProperty("是否公开链接")
    private Boolean shareIs;
    @ApiModelProperty("来源")
    private ChartPageSource source;
    @ApiModelProperty("长链接")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String longLink;
    @ApiModelProperty("短链接")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String shortLink;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("短链接值")
    private String shortLinkValue;
    @ApiModelProperty("其他json")
    private String otherSettingJson;
    @ApiModelProperty("到期时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime expirationTime;
    @ApiModelProperty("密码")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String password;
    @ApiModelProperty("菜单排序")
    private Long sort;
}
