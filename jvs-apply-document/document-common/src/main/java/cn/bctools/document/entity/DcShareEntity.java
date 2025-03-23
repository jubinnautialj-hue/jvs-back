package cn.bctools.document.entity;

import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.ShareTimeTypeEnums;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档分享")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_share")
public class DcShareEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty(" 分享/不分享，分享")
    private Boolean share;
    @ApiModelProperty("是否开启分享下载")
    private Boolean downStatus;
    @ApiModelProperty("密码模式")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String sharePassword;
    @ApiModelProperty("分享结束时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String shareEndTime;
    @ApiModelProperty("分享的时间快捷方式")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private ShareTimeTypeEnums shareTimeType;
    @ApiModelProperty("分享的文档短链接,二维码可通过链接生成即可")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String shareLink;
    @ApiModelProperty("文档id")
    @TableField("dc_id")
    private String dcId;
    @ApiModelProperty("分享的文档长链接链接")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String shareLinkOriginal;
    @ApiModelProperty("分享key")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String shareKey;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT)
    private String createById;
    @ApiModelProperty("知识库id")
    @TableField(exist = false)
    private String knowledgeId;
    @ApiModelProperty("知识库id")
    @TableField(exist = false)
    private String nameSuffix;
    @ApiModelProperty("知识库id")
    @TableField(exist = false)
    private DcLibraryTypeEnum type;
    @ApiModelProperty("是否为只修改密码")
    @TableField(exist = false)
    private Boolean updatePassWord;
}
