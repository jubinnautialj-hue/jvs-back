package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.dto.form.FormRuleHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.item.FilterHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 数据字段基本信息
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldPublicHtml extends FieldJsonHtml {

    @ApiModelProperty("字段唯一id")
    private String id;

    @ApiModelProperty("字段名称")
    private String fieldKey;

    @ApiModelProperty("显示名称")
    private String fieldName;

    @ApiModelProperty("显示名称")
    private String label;

    @ApiModelProperty(value = "显示来源字段id")
    private String sourceFieldId;

    @ApiModelProperty(value = "弹框,是否跳过数据权限")
    private Boolean nopermission;
    @ApiModelProperty(value = "默认值")
    private Object defaultValue;

    @ApiModelProperty("字段类型")
    private DataFieldType fieldType;

    public DataFieldType getFieldType() {
        return getType();
    }

    @ApiModelProperty("是否是容器组件,如果是解析容器组件里面的字段,主要用于公式处理")
    private Boolean next = false;
    @ApiModelProperty("开启数据联动条件")
    private Boolean dataLinkageEnable;
    @ApiModelProperty("数据联动条件")
    private List<QueryConditionDto> dataLinkageList;

    @ApiModelProperty(value = "数据筛选", notes = "兼容2.1.6以前的配置")
    private List<FilterHtml> dataFilterList;
    @ApiModelProperty(value = "数据筛选", notes = "与/或")
    private List<List<FilterHtml>> dataFilterGroupList;
    @ApiModelProperty(value = "是否开启了关联模型筛选")
    private Boolean dataFilterEnable = false;

    @ApiModelProperty("数据联动模型id")
    private String dataLinkageModelId;

    @ApiModelProperty("数据联动显示的字段值")
    private String linkageFieldKey;
    @ApiModelProperty("上级 key")
    private String parentKey;
    @ApiModelProperty("上级 类型")
    private String parentType;
    private String formId;
    private Integer width;
    private Boolean disabled;
    private Object status;
    private String prop;
    private int span;
    private boolean display;
    @ApiModelProperty("[仅前端使用]该组件所用到的字段名")
    private List<String> showFrom;
    @ApiModelProperty("接口数据-显示值与传递值")
    private FormValueHtml props;
    private Object linkbind;
    private int minlength;
    private int maxlength;
    private boolean showwordlimit;
    private String placeholder;
    private boolean clearable;
    private boolean showpassword;
    private Object prefixicon;
    private Object suffixicon;
    private Object prepend;
    private Object append;
    private Object regularExpression;
    private Object regularMessage;
    private Object defaultUrl;
    private Object sqlType;
    private Object defaultOrigin;
    private boolean searchable;
    @ApiModelProperty("校验设置")
    private List<FormRuleHtml> rules;
    private boolean editable;
    private int searchPopupWidth;
    private List othersQuery;
    private boolean dataFilterable;
    private List datafilterlist;
    /**
     * 关联表单的属性
     */
    @Getter
    @Setter
    @ApiModelProperty("数据模型id")
    private String dataModelId;
    /**
     * 案例: /mgr/jvs-design/dynamic/data/query/list/{数据模型id}?fieldKey={字段key}
     */
    @ApiModelProperty("前端请求数据的地址")
    @Getter
    @Setter
    private String url;


    @Getter
    @Setter
    @ApiModelProperty("关联表单扩展显示字段key")
    private List<String> others;

    @ApiModelProperty(value = "允许为空", notes = "默认都可以为空")
    private Boolean emptyEnable = true;


    @Override
    public String getDataModelId() {
        //只有打开了配置后才能获取模型 id,否则获取为空
        if (dataFilterEnable) {
            return dataModelId;
        } else {
            return null;
        }
    }

}
