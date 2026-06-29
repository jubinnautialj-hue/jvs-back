package cn.bctools.document.auth.dto;

import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.DcIdentifying;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("数据权限")
public class DocumentAuthDto {
    @ApiModelProperty("当前目录的id")
    private String parentId;
    @ApiModelProperty("当前访问的数据id")
    private String id;
    @ApiModelProperty("当前目录下可以查看的所有文件id")
    private Set<String> ids;
    @ApiModelProperty("知识库对应的权限标识")
    private Map<String, List<IdentifyingAuthDto>> identifyingRoleMap = new HashMap<>();
}

