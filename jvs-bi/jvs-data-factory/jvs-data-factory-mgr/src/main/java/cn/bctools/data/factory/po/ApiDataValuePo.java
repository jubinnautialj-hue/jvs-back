package cn.bctools.data.factory.po;

import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 *
 * api 数据结构获取
 *
 * @author Administrator
 */
@ApiOperation("执行sql")
@Data
public class ApiDataValuePo {
    @ApiModelProperty("结果")
    private String value;
    @ApiModelProperty("结构")
    private List<JsonAnalysisPo> structure;
}
