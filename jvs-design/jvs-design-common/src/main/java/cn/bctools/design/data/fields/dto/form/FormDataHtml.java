package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@ApiModel("表单内容")
@Data
@Accessors(chain = true)
public class FormDataHtml {
    @ApiModelProperty(value = "表单设置")
    private FormSettingHtml formsetting;
    @ApiModelProperty(value = "表单提交和渲染JSON格式")
    private Object formJson;
    @ApiModelProperty(value = "jsqlJson")
    private String jsqlJson;
    @ApiModelProperty(value = "customizeJsqlJson")
    private String customizeJsqlJson;
    @ApiModelProperty("属性集合")
    private List<DataTableFieldDesignHtml> autoTableFields;
    @ApiModelProperty(value = "表单配置内容")
    private List<Map<String, Object>> forms;

}
