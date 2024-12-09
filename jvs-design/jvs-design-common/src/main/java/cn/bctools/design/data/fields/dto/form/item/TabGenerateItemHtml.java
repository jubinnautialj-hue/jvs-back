package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: GuoZi
 */
@Setter
@Getter
@Accessors(chain = true)
@ApiModel(value = "选项卡生成组件")
@EqualsAndHashCode(callSuper = true)
public class TabGenerateItemHtml extends BaseItemHtml {

    public TabGenerateItemHtml(String prop) {
        this.setProp(prop);
        this.setFieldType(DataFieldType.tabGenerate);
        this.setType(DataFieldType.tabGenerate);
    }

    @ApiModelProperty("生成的组件容器中包含的其它组件")
    List<FieldBasicsHtml> column;

    public TabGenerateItemHtml setColumn(List<FieldBasicsHtml> column) {
        this.column = column;
        if (ObjectNull.isNotNull(column)) {
            this.setDesignJson(BeanCopyUtil.beanToMap(this));
        }
        return this;
    }
}
