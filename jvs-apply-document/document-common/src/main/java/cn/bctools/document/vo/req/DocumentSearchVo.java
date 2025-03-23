package cn.bctools.document.vo.req;

import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.SearchTimeScopeEnum;
import cn.bctools.document.vo.BaseReqVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库文档搜索入参
 */

@Data
@Accessors(chain = true)
@ApiModel("知识库-搜索")
public class DocumentSearchVo extends BaseReqVo {

    @ApiModelProperty(value = "知识库id")
    private String knowledgeId;

    @ApiModelProperty(value = "搜索类型 1模糊搜索 2精准搜索")
    private Integer searchType;

    @ApiModelProperty(value = "标签名称,多个时空格分隔")
    private String tagName;

    @ApiModelProperty(value = "关键词相似度查询时就是整个文本内容")
    private String keyword;

    @ApiModelProperty(value = "类型")
    private DcLibraryTypeEnum type;

    @ApiModelProperty("阅读权限")
    private DcLibraryReadEnum shareRole;

    @ApiModelProperty("是否需要保留搜索记录")
    private Boolean search;

    @ApiModelProperty("需要查询的主体 1标题 2内容 不传表示标题与内容")
    private Integer searchContentType;

    @ApiModelProperty(value = "最近修改时间")
    private SearchTimeScopeEnum timeScope;

    @ApiModelProperty(value = "是否去除为空的数据")
    private Boolean isNotNull;

}
