package cn.bctools.common.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xh
 */
@Data
@Accessors(chain = true)
public class TenantSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文件存储大小")
    Long fileSumSize;
    @ApiModelProperty("数据存储大小")
    Long dataSumSize;
    @ApiModelProperty(value = "分布", notes = "根据不同的类型, 放入数据进行显示")
    List<Map<String, String>> list;

}
