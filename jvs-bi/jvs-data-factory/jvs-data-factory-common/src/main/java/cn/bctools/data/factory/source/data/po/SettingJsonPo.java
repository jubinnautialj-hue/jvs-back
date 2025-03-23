package cn.bctools.data.factory.source.data.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@ApiModel("返回值设置")
@Accessors(chain = true)
public class SettingJsonPo implements Serializable {

    private static final long serialVersionUID = -8585838291229308595L;
    @ApiModelProperty("是否存在分页")
    private Boolean pageIs;
    @ApiModelProperty("当前总条数 如果是根节点直接就是key 如果不是跟节点直接用.分隔 例如result.total")
    private String totalKey;
    @ApiModelProperty("数据key 如果是根节点直接就是key 如果不是跟节点直接用.分隔 例如result.total")
    private String dataKey;
    @ApiModelProperty("如果时分页 每页最大条数时多少 必选填 如果无限制就直接-1")
    private Integer sizeMax;
    @ApiModelProperty("返回值中如果存在code  需要用户绑定")
    private String codeKey;
    @ApiModelProperty("返回值中如果存在code 当code 等于什么值才表示成功")
    private String codeValue;
    @ApiModelProperty("请求方式")
    private String requestMethod;
}
