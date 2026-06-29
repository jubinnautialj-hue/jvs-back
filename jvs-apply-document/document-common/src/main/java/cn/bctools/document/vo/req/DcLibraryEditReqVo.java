package cn.bctools.document.vo.req;

import cn.bctools.document.dto.AddAuthConfigDto;
import cn.bctools.document.entity.DcLibrary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 重命名知识库/目录/文档，或设置知识库
 */
@Data
@Accessors(chain = true)
@ApiModel("重命名知识库/目录/文档，或设置知识库入参")
public class DcLibraryEditReqVo extends DcLibrary {

    @ApiModelProperty(value = "权限设置 key就是角色id")
    private List<AddAuthConfigDto> addAuthConfigDto;

    @ApiModelProperty(value = "转移项目目标人员id")
    private String transfer;

}
