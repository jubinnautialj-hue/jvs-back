package cn.bctools.rule.encryption.desensitization;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author zhuxiaokang
 * 脱敏请求入参
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DesensitizationDto {

    @ParameterValue(info = "数据", explain = "数据支持键值对的形式或数组键值对形式", type = InputType.map, necessity = false)
    public Object body;

    @ParameterValue(info = "脱敏规则", explain = "脱敏规则,key为数据名，value为数据脱敏规则", type = InputType.mapValueSelected, cls = DesensitizationSelected.class)
    public Map<String, DesensitizedUtil.DesensitizedType> map;

}
