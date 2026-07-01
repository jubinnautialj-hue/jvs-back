package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author jvs
 * 字段设计的 Json 对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class FieldJsonHtml extends FieldHtml {
    /**
     * 具体结构如下:
     * <p>
     * 列表页: {@link cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml}
     */
    @ApiModelProperty("设计数据(Json格式)")
    private Map<String, Object> designJson;

    public abstract String getDataModelId();
}
