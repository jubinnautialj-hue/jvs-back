package cn.bctools.rule.tools.http;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.BooleanSelected;
import cn.hutool.http.Method;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * get方法的参数实体类
 *
 * @author guojing
 */
@Accessors(chain = true)
@Data
public class HttpFunctionDto {

    @ParameterValue(info = "请求地址", type = InputType.http, necessity = false, explain = "支持http://xxx/xxx或lb:demo/xx/xx", defaultValue = "http://www.bctools.cn")
    @NotNull(message = "请求地址不能为空")
    public String url;

    @ParameterValue(info = "请求类型", type = InputType.selected, cls = HttpTypeSelected.class)
    @NotNull(message = "请求类型不能为空")
    public Method type;

    @ParameterValue(info = "MediaType", type = InputType.selected, defaultValue = "application/json", cls = MediaTypeSelected.class)
    @NotNull(message = "MediaType不能为空")
    public String mediaType;

    @ParameterValue(info = "header请求参数", explain = "请求头不支持覆盖content-type", necessity = false, type = InputType.map)
    public Map<String, String> header;

    @ParameterValue(info = "是否添加系统请求头", type = InputType.selected, defaultValue = "true", cls = BooleanSelected.class)
    public Boolean systemHeader;

    @ParameterValue(info = "body请求参数，或Param", necessity = false, type = InputType.map)
    public Object body;

    @ParameterValue(info = "不填写地址时默认返回mock", necessity = false, type = InputType.json)
    public String json;
}
