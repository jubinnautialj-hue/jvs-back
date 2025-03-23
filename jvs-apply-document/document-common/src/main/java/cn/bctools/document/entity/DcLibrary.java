package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.DocumentFileStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author auto
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("知识库")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "dc_library", autoResultMap = true)
public class DcLibrary extends BasalPo implements Serializable, Comparable<DcLibrary> {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("文件存储路径")
    private String filePath;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("名称")
    @NotBlank(message = "标题不能为空")
    @Length(max = 64, min = 1, message = "标题长度需控制在1-64位")
    private String name;
    @ApiModelProperty("阅读权限")
    private DcLibraryReadEnum shareRole;
    @ApiModelProperty("文档大小M")
    private Long size;
    @ApiModelProperty("上级ID")
    private String parentId;
    @ApiModelProperty("所有者id")
    private String possessor;
    @ApiModelProperty("类型/知识库、目录、文本文档、表格文档、脑图文档、流程文档。")
    private DcLibraryTypeEnum type;
    @ApiModelProperty("排序")
    private Integer orderId;
    @ApiModelProperty("是否删除 0未删除  1已删除")
    @TableLogic
    private Boolean delFlag;

    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("置顶 时间 根据时间大小排序")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime topTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("子集")
    @TableField(exist = false)
    private List<DcLibrary> children;

    @ApiModelProperty("路径id")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> pathId;

    @ApiModelProperty("内容")
    @TableField(exist = false)
    private String content;
    @ApiModelProperty("知识库id")
    private String knowledgeId;

    @ApiModelProperty("知识库封面颜色")
    private String color;

    @ApiModelProperty(value = "租户")
    private String tenantId;

    @ApiModelProperty(value = "是否可以评论")
    private Boolean commentable;

    @ApiModelProperty(value = "自动发送查看提醒开关")
    private Boolean readNotify;

    @ApiModelProperty(value = "标签")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> label;

    @ApiModelProperty(value = "转换的报错信息")
    private String errorMessage;

    @ApiModelProperty(value = "源文件id")
    private String sourceId;
    @ApiModelProperty(value = "源文件id")
    private String urlAddress;

    @ApiModelProperty(value = "前端用于存储其他字段(例如:脑图需要一个type类型存储）")
    private String otherJson;

    @ApiModelProperty(value = "排序字段名称")
    private String sortFieldName;
    @ApiModelProperty(value = "排序规则")
    private Boolean sortDescIs;

    @ApiModelProperty(value = "后缀名 文件类型")
    private String nameSuffix;

    @ApiModelProperty(value = "状态")
    private DocumentFileStatusEnum fileStatus;

    @ApiModelProperty("权限标识")
    @TableField(exist = false)
    private List<IdentifyingAuthDto> dcIdentifying;

    @ApiModelProperty(value = "年月用于统计")
    @TableField(exist = false)
    private String month;

    @ApiModelProperty(value = "是否分享")
    @TableField(exist = false)
    private Boolean share;

    @ApiModelProperty(value = "文档url")
    @TableField(exist = false)
    private String fileLink;

    @ApiModelProperty("是否置顶")
    @TableField(exist = false)
    private Boolean isTopping;
    @ApiModelProperty("是否收藏")
    @TableField(exist = false)
    private Boolean collectIs;

    @ApiModelProperty("置顶顺序值")
    @TableField(exist = false)
    private Integer topping;

    @ApiModelProperty("路径")
    @TableField(exist = false)
    private String path;
    @ApiModelProperty("所有者名称")
    @TableField(exist = false)
    private String possessorName;

    //注意这里是降序排序
    @Override
    public int compareTo(DcLibrary other) {
        return Integer.compare(other.getOrderId(), this.orderId);
    }

}
