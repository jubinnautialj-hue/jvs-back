package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表单组件: 下拉框
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "下拉框")
@EqualsAndHashCode(callSuper = true)
public class SelectItemHtml extends BaseItemHtml {

    @ApiModelProperty("数据类型")
    private FormDataTypeEnum datatype;

    @ApiModelProperty("配置数据-配置字典")
    private List<FormValueHtml> dicData;


    @ApiModelProperty("接口数据-接口地址")
    private String url;
    @ApiModelProperty("逻辑引擎的key")
    private String optionHttp;


    @ApiModelProperty("系统字典-字典id")
    private String systemDict;
    @ApiModelProperty("模型id值")
    private String formId;

}
