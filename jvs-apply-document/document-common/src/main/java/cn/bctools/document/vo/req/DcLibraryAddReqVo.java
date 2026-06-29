package cn.bctools.document.vo.req;

import cn.bctools.document.dto.AddAuthConfigDto;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.DocumentFileStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 添加知识库、目录、文档
 */
@Data
@Accessors(chain = true)
@ApiModel("添加知识库、目录、文档入参")
public class DcLibraryAddReqVo {

    @ApiModelProperty(value = "名称")
    @Length(min = 1, max = 125, message = "名称不能为空且不能过长")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "知识库封面颜色")
    private String color;

    @ApiModelProperty(value = "除创建知识库不传递，其余都要传递当前节点的id")
    private String id;

    @ApiModelProperty(value = "文档模板id")
    private String templateId;

    @ApiModelProperty(value = "创建的类型")
    private DcLibraryTypeEnum fileType;
    @ApiModelProperty("阅读权限")
    private DcLibraryReadEnum shareRole;
    @ApiModelProperty("上级ID")
    private String parentId;
    @ApiModelProperty(value = "查看提醒开关（true-开启,false-关闭）")
    private Boolean readNotify;

    @ApiModelProperty(value = "权限设置 key 角色id")
    private List<AddAuthConfigDto> addAuthConfigDto;

    @ApiModelProperty(value = "转移项目目标人员id")
    private String transfer;

    @ApiModelProperty(value = "是否支持搜索")
    private Boolean searchable;

    @ApiModelProperty(value = "状态")
    private DocumentFileStatusEnum fileStatus;

    @ApiModelProperty(value = "是否是帮助")
    private Boolean help;

    @ApiModelProperty(value = "评论开关（true-开启,false-关闭）")
    private Boolean commentable;

    @ApiModelProperty(value = "源文件id")
    private String sourceId;

    @ApiModelProperty(value = "url类型地址")
    private String urlAddress;

}
