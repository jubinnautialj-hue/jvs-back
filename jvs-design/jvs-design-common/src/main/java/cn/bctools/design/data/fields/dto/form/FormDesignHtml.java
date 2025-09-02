package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.data.fields.enums.FormTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <表单设计>
 *
 * @author auto
 **/
@ApiModel(value = "表单设计")
@Data
@Accessors(chain = true)
public class FormDesignHtml {

    @ApiModelProperty(value = "表单id")
    private String formId;
    @ApiModelProperty(value = "关联的表单id")
    private String linkFormId;
    @ApiModelProperty(value = "表单名称")
    private String formName;
    @ApiModelProperty(value = "备注")
    private String formRemark;
    @ApiModelProperty(value = "表单类型")
    private FormTypeEnum formType;
    @ApiModelProperty(value = "表单数据集")
    private List<FormDataHtml> formdata;
    @ApiModelProperty(value = "是否关联流程")
    private Boolean isFlowable;
    @ApiModelProperty(value = "是否有权限")
    private Boolean role;
    @ApiModelProperty(value = "公式集,用于前端优化接口请求次数")
    private List<String> execs;
    @ApiModelProperty(value = "所有表格的数据，用于公式的数据返回优化")
    private List<String> tablePath = new ArrayList<>();
    @ApiModelProperty(value = "是否开启嵌套选项卡")
    private Boolean isRecursionTabs;


}
