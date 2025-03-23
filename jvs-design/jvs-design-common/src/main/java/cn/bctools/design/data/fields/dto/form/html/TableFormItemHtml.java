package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 表单组件: 表格
 */

@Data
@Accessors(chain = true)
@ApiModel(value = "表格")
@EqualsAndHashCode(callSuper = true)
public class TableFormItemHtml extends FieldBasicsHtml {

    @ApiModelProperty(value = "表格字段")
    private List<FieldBasicsHtml> tableColumn;

    private String editBtnFormCode;
    private String addBtnText;
    private String dataModelId;
    private boolean editBtn;
    private int editPopupWidth;
    private List rules;
    private String align;
    private Object tips;
    private boolean addBtn;
    private String defaultOrigin;
    private boolean menuFix;
    private String prop;
    private boolean stripe;
    private boolean disabled;
    private boolean dataFilterEnable;
    private boolean border;
    private String formId;
    private boolean viewBtn;
    private boolean editable;
    private boolean display;
    private String label;
    private int viewPopupWidth;
    private List dataFilterList;
    private String delBtnText;
    private int addPopupWidth;
    private String sqlType;
    private boolean delBtn;
    private String addBtnOrigin;
    private String name;
    private String editBtnText;
    private boolean page;
    private String addBtnFormCode;
    private String viewBtnText;
    private int span;
    private String status;
    private int columWidth;

}
