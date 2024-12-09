package cn.bctools.design.rule.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.List;
/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@ApiModel("api调用逻辑校验规则")
public class RuleParameterInDto implements Serializable {

    @ApiModelProperty("请求体信息信息")
    List<BodyInDto> bodyList;

    @ApiModelProperty("请求头信息")
    List<ParameterMap> headerList;

    @ApiModelProperty("参数信息")
    List<ParameterMap> queryList;

    @ApiModelProperty("路径参数信息")
    List<ParameterMap> pathList;

    @ApiModelProperty("请求方法")
    HttpMethod method;

    @ApiModelProperty("url地址信息，与逻辑标识相同")
    String url;
    @ApiModelProperty("返回的 body 数据 ")
    String body;

}
