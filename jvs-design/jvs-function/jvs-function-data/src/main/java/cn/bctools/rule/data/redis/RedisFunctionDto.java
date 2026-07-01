package cn.bctools.rule.data.redis;


import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author st
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RedisFunctionDto {


    @NotNull(message = "数据源不能为空")
    @ParameterValue(info = "目标数据源", type = InputType.selected,  cls = RedisDataSourceSelected.class)
    public String dbName;

    @ParameterValue(info = "操作类型", type = InputType.selected,  cls = RedisOperationTypeSelected.class)
    public RedisOperationTypeEnum operationType;


    @ParameterValue(info = "缓存key", explain = "设值，缓存key统一前缀\"rule:module:\"", type = InputType.input, defaultValue = "rule:module:")
    public String key;

    @ParameterValue(info = "缓存key后缀", type = InputType.input, necessity = false)
    public String keySuffix;

    @ParameterValue(info = "缓存内容", type = InputType.longtext, necessity = false)
    public Object value;

    @ParameterValue(info = "缓存时长(秒)", explain = "设值时若不传缓存时长，则默认30天", type = InputType.number, necessity = false, defaultValue = "60")
    public Long ttl;

    private RedisDataSourceSelectedOption option;

}
