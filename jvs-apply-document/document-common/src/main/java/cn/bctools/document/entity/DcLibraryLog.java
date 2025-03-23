package cn.bctools.document.entity;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("日志表")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_library_log")
public class DcLibraryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("文档的ID值")
    @TableField("dc_library_id")
    private String dcLibraryId;
    @ApiModelProperty("用户名称")
    @TableField("user_name")
    private String userName;
    @ApiModelProperty("创建时间")
    @TableField("create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty("操作类型")
    @TableField("operation_type")
    private String operationType;
    @ApiModelProperty("知识库id")
    @TableField("knowledge_id")
    private String knowledgeId;
    @ApiModelProperty("文档名称")
    private String dcName;
    @ApiModelProperty("上级ID")
    private String parentId;
    @ApiModelProperty("用户id")
    private String userId;
    @ApiModelProperty("文件后缀名")
    private String nameSuffix;
    @ApiModelProperty("类型/知识库、目录、文本文档、表格文档、脑图文档、流程文档。")
    private DcLibraryTypeEnum type;
    @ApiModelProperty("用户信息")
    @TableField(exist = false)
    private UserDto userDto;
    @ApiModelProperty("知识库名称")
    @TableField("knowledge_name")
    private String knowledgeName;
    @ApiModelProperty("文档对象")
    @TableField(exist = false)
    private DcLibrary dcLibrary;
}
