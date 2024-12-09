package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表单组件: 关联表单
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "关联表单")
@EqualsAndHashCode(callSuper = true)
public class ConnectFormItemHtml extends BaseItemHtml {

    /**
     * 关联表单的属性
     */
    @ApiModelProperty("数据模型id")
    private String dataModelId;
    /**
     * 案例: /mgr/jvs-design/dynamic/data/query/list/{数据模型id}?fieldKey={字段key}
     */
    @ApiModelProperty("前端请求数据的地址")
    private String url;

    @ApiModelProperty("关联的字段key")
    private FormValueHtml props;

    @ApiModelProperty("关联表单扩展显示字段key")
    private List<String> others;

}
