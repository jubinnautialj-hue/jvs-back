package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.enums.DataRoleTypeEnum;
import cn.bctools.design.data.fields.dto.DataConditionDto;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <列表页设计>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class PageDesignHtml {

    @ApiModelProperty(value = "列表页id")
    private String pageId;

    @ApiModelProperty(value = "列表名称")
    private String name;
    @ApiModelProperty(value = "布局类型")
    private LayoutEnum displayType;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "应用Id,只有通过xmind方式创建才有这个值")
    private String appId;
    @ApiModelProperty(value = "类型，只有通过xmind方式创建才有这个值， ")
    private String type;

    @ApiModelProperty(value = "列表配置", required = true)
    private DataPageDesignHtml dataPage;
    @ApiModelProperty(value = "列表过滤", required = true, notes = "只有分页查询才会生效， 而且只针对某一个设计才会有效")
    private List<DataConditionDto> parameters;
    @ApiModelProperty(value = "按钮配置", required = true)
    private List<ButtonDesignHtml> buttons;
    @ApiModelProperty(value = "左侧按钮配置", required = false)
    private List<ButtonDesignHtml> leftTreeButton;
    @ApiModelProperty(value = "设计相关的 id", example = "新增修改，或删除表单", required = false)
    private List<String> relationDesignIds;
    @ApiModelProperty(value = "权限信息")
    private List<DesignRole> role;
    @ApiModelProperty(value = "权限类型")
    private Boolean roleType;
    @ApiModelProperty(value = "是否跳过数据权限")
    private Boolean stepDataPermission;

    @ApiModelProperty("数据权限")
    private List<JSONObject> dataRole;
    @ApiModelProperty(value = "指定使用的数据权限类型")
    private DataRoleTypeEnum dataRoleType;
    @ApiModelProperty(value = "排序")
    private List<PageSortDesignHtml> sorts;

    @ApiModelProperty("是否固定操作栏")
    private Boolean menuFixed;
    @ApiModelProperty("隐藏刷新")
    private Boolean hiddenRefresh;
    @ApiModelProperty("隐藏搜索")
    private Boolean hiddenSearch;
    @ApiModelProperty("隐藏分享")
    private Boolean hiddenShare;
    @ApiModelProperty("隐藏排序")
    private Boolean hiddenSort;
    @ApiModelProperty("隐藏序号")
    private Boolean hiddenIndex;
    @ApiModelProperty("操作栏宽度")
    private Integer menuWidth;

    @ApiModelProperty("列表页的表头")
    private List<JSONObject> pageTableTitle;
    @ApiModelProperty("是否开启甘特图")
    private Boolean gantt = false;
    @ApiModelProperty("是否开启斑马纹")
    private Boolean openZebra = false;
    @ApiModelProperty("甘特图配置")
    private GanttFormHtml ganttForm;

}
