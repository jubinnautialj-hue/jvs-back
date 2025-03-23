package cn.bctools.document.dto;


import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("首页统计数据")
public class IndexStatisticsDto {
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("文库分类统计")
    private Map<DcLibraryReadEnum, Long> knowledgeCount;
    @ApiModelProperty("文章总数量")
    private Integer articleCount;
    @ApiModelProperty("文章分组统计")
    private Map<DcLibraryTypeEnum, Long> articleGroupCount;

}
