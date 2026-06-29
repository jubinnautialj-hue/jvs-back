package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.TemplateTypeEnum;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
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
import java.util.List;

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
@TableName(value = "dc_template", autoResultMap = true)
public class DcTemplate extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("文件存储路径")
    private String filePath;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("封面文件存储路径")
    private String coverFilePath;
    @ApiModelProperty("封面桶名称")
    private String coverBucketName;
    @ApiModelProperty("文档名称")
    @NotBlank(message = "标题不能为空")
    @Length(max = 64, min = 1, message = "标题长度需控制在1-64位")
    private String name;
    @ApiModelProperty("类型/知识库、目录、文本文档、表格文档、脑图文档、流程文档。")
    private DcLibraryTypeEnum type;
    @ApiModelProperty("模板分类")
    @TableField("type_id")
    private String typeId;
    @ApiModelProperty("文件后缀名 新建的时候必须传 xlsx docx")
    @TableField("name_suffix")
    private String nameSuffix;
    @ApiModelProperty(value = "前端用于存储其他字段(例如:脑图需要一个type类型存储）")
    private String otherJson;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("模板类型 动态还是静态")
    private TemplateTypeEnum templateType;
    @ApiModelProperty("模板替换时生成的新文档保存的父级")
    private String savePathId;
    @ApiModelProperty(value = "模板内容")
    @TableField(exist = false)
    private String content;
    @ApiModelProperty("封面url")
    @TableField(exist = false)
    private String coverUrl;
    @ApiModelProperty(value = "文档url")
    @TableField(exist = false)
    private String fileLink;
    @ApiModelProperty("模板替换时生成的新文档保存的父级名称")
    @TableField(exist = false)
    private String savePathName;
    @ApiModelProperty("唯一key")
    @TableField(exist = false)
    private String key;

}
