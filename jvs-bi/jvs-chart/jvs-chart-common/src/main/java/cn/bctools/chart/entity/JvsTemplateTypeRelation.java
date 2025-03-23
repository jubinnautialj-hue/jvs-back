package cn.bctools.chart.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 模板分类与模板数据绑定
 *
 * @author zqs
 */
@Data
@Accessors(chain = true)
@TableName(value = "jvs_template_type_relation", autoResultMap = true)
@ApiModel
public class JvsTemplateTypeRelation implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;

    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("分类id")
    private String typeId;
}
