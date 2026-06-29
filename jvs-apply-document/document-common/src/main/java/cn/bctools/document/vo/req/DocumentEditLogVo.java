package cn.bctools.document.vo.req;

import cn.bctools.document.po.enums.DocumentLogTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库-查询文档编辑记录入参
 */

@Data
@Accessors(chain = true)
@ApiModel("知识库-查询文档编辑记录入参")
public class DocumentEditLogVo {

    @ApiModelProperty(value = "文档id")
    private String id;

    @ApiModelProperty(value = "查询类型")
    private DocumentLogTypeEnum type;

    @ApiModelProperty(value = "用户id 可以不传 后端赋值")
    private String userId;
}
