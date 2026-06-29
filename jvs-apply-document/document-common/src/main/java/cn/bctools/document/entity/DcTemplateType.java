package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.TemplateTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 文档模板
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档模板")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_template_type")
public class DcTemplateType extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("文件存储路径")
    private String typeName;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("模板类型 动态还是静态")
    private TemplateTypeEnum templateType;


}
