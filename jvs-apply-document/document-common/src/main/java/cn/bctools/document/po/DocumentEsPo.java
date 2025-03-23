package cn.bctools.document.po;

import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库基本信息(包含所有知识库类型) ES实体类
 */
@Data
@ApiModel("知识库基本信息")
@Document(indexName = "document_base_info")
public class DocumentEsPo {

    @ApiModelProperty(value = "索引id", notes = "数据格式：租户id_文档id")
    @Id
    private String id;

    @ApiModelProperty(value = "文档id")
    @Field(type = FieldType.Keyword)
    private String docId;

    @ApiModelProperty("阅读权限")
    @Field(type = FieldType.Keyword)
    private DcLibraryReadEnum shareRole;

    @ApiModelProperty(value = "类型")
    @Field(type = FieldType.Keyword)
    private DcLibraryTypeEnum type;

    @ApiModelProperty(value = "租户id")
    @Field(type = FieldType.Keyword)
    private String tenantId;

    @ApiModelProperty(value = "文档名称")
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String name;

    @ApiModelProperty(value = "文档内容")
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String content;

    @ApiModelProperty(value = "知识库名称")
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String knowledgeName;

    @ApiModelProperty(value = "标签")
    @Field(type = FieldType.Text, analyzer = "whitespace", searchAnalyzer = "whitespace")
    private String tagName;

    @ApiModelProperty(value = "知识库id")
    @Field(type = FieldType.Keyword)
    private String knowledgeId;

    @ApiModelProperty(value = "文档创建时间")
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "文档修改时间")
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "上级id")
    @Field(type = FieldType.Keyword)
    private String parentId;

    @ApiModelProperty(value = "是否在回收站")
    @Field(type = FieldType.Boolean)
    private Boolean isDelete;

    @ApiModelProperty(value = "作者id")
    @Field(type = FieldType.Text)
    private String authorId;

    @ApiModelProperty(value = "上级id结合不包含当前层")
    @Field(type = FieldType.Keyword)
    private String pathId;

    @ApiModelProperty(value = "作者名称")
    @Field(type = FieldType.Text)
    private String authorName;


    @ApiModelProperty(value = "sim第一段hash值")
    @Field(type = FieldType.Short)
    private Short simHashA;
    @ApiModelProperty(value = "sim第二段hash值")
    @Field(type = FieldType.Short)
    private Short simHashB;
    @ApiModelProperty(value = "sim第三段hash值")
    @Field(type = FieldType.Short)
    private Short simHashC;
    @ApiModelProperty(value = "sim第四段hash值")
    @Field(type = FieldType.Short)
    private Short simHashD;
}
