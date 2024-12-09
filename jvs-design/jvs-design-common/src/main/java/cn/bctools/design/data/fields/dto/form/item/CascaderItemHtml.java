package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 表单组件: 级联选择
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "级联选择")
@EqualsAndHashCode(callSuper = true)
public class CascaderItemHtml extends MultipleHtml {

    @ApiModelProperty("系统字典-树形字典")
    private String dictName;

}
